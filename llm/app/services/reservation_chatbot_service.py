import os
import time
from pinecone import Pinecone
import logging
from datetime import date, timedelta
from app.core.config import ChatBotConfig
from app.repositories.reservation_repo import ReservationRepo
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.chat_history import BaseChatMessageHistory, InMemoryChatMessageHistory
from langchain_core.runnables import RunnableWithMessageHistory
from langchain.docstore.document import Document
from langchain_pinecone import PineconeVectorStore
from fastapi import HTTPException

logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO)

class ChatBotService:
    # def __init__(self, bot_name: str):
    #     self.config = ChatBotConfig(bot_name)
    #
    #     # LLM, Embedding 모델 로드
    #     self.model = ChatOpenAI(
    #         model = self.config.get("model_name", "gpt-4o"),
    #         temperature = self.config.get("temperature", 0.5)
    #     )
    #     self.embedding_model = OpenAIEmbeddings()
    #
    #     # Pinecone 클라이언트 초기화
    #     pinecone.Pinecone(api_key=os.getenv("PINECONE_API_KEY"))
    #
    #     # VectorStore 초기화
    #     self.vector_store = PineconeVectorStore(
    #         index_name=os.getenv("PINECONE_INDEX_NAME"),
    #         embedding=self.embedding_model,
    #     )
    def __init__(self, bot_name: str):
        self.config = ChatBotConfig(bot_name)

        # LLM, Embedding 모델 로드
        self.model = ChatOpenAI(
            model=self.config.get("model_name", "gpt-4o"),
            temperature=self.config.get("temperature", 0.5)
        )
        self.embedding_model = OpenAIEmbeddings()

        # Pinecone 클라이언트 초기화
        self.pc = Pinecone(api_key=os.getenv("PINECONE_API_KEY"))

        # VectorStore 초기화
        self.vector_store = PineconeVectorStore(
            index_name=os.getenv("PINECONE_INDEX_NAME"),
            embedding=self.embedding_model,
        )

    # ------------------ 날짜 치환 함수 ------------------ #
    @staticmethod
    def replace_relative_date_with_actual(user_question: str) -> str:
        today = date.today()

        # 오늘, 내일, 어제
        if "오늘" in user_question:
            user_question = user_question.replace("오늘", today.strftime("%Y-%m-%d"))
        if "내일" in user_question:
            tomorrow = today + timedelta(days=1)
            user_question = user_question.replace("내일", tomorrow.strftime("%Y-%m-%d"))
        if "어제" in user_question:
            yesterday = today - timedelta(days=1)
            user_question = user_question.replace("어제", yesterday.strftime("%Y-%m-%d"))

        # 이번 주
        if "이번 주" in user_question or "이번주" in user_question:
            start_of_week = today - timedelta(days=today.weekday())
            end_of_week = start_of_week + timedelta(days=6)
            user_question = user_question.replace("이번 주", f"{start_of_week.strftime('%Y-%m-%d')}~{end_of_week.strftime('%Y-%m-%d')}")
            user_question = user_question.replace("이번주", f"{start_of_week.strftime('%Y-%m-%d')}~{end_of_week.strftime('%Y-%m-%d')}")

        # 이번 달
        if "이번 달" in user_question or "이번달" in user_question:
            start_of_month = today.replace(day=1)
            next_month = start_of_month.replace(month=start_of_month.month % 12 + 1, day=1)
            end_of_month = next_month - timedelta(days=1)
            user_question = user_question.replace("이번 달", f"{start_of_month.strftime('%Y-%m-%d')}~{end_of_month.strftime('%Y-%m-%d')}")
            user_question = user_question.replace("이번달", f"{start_of_month.strftime('%Y-%m-%d')}~{end_of_month.strftime('%Y-%m-%d')}")
        return user_question

    # ------------------ 세션 기록 조회 ------------------ #
    def get_session_history(self, session_id: str, shop_id: int) -> BaseChatMessageHistory:
        namespace = self._get_namespace(session_id, shop_id)

        try:
            response_gen = self.vector_store.index.list(namespace=namespace, limit=100)
            response_list = list(response_gen)
        except Exception as e:
            logger.warning(f"Pinecone list error: {e}")
            return InMemoryChatMessageHistory()

        if not response_list or 'matches' not in response_list[0]:
            return InMemoryChatMessageHistory()

        matches = response_list[0]['matches']
        vector_ids = [v['id'] for v in matches]

        fetch_response = self.vector_store.index.fetch(ids=vector_ids, namespace=namespace)
        vectors_dict = fetch_response.get('vectors', {})

        sorted_vectors = sorted(vectors_dict.values(), key=lambda v: v.get('metadata', {}).get('created_at', 0))

        history = InMemoryChatMessageHistory()
        for vec in sorted_vectors:
            msg_text = vec.get('metadata', {}).get('text','')
            if msg_text.startswith("Human:"):
                history.add_user_message(msg_text.replace("Human: ", "", 1))
            elif msg_text.startswith("AI:"):
                history.add_ai_message(msg_text.replace("AI: ", "", 1))
        return history

    # ------------------ 세션 초기화 ------------------ #
    async def init_session(self, session_id: str, shop_id: int):
        namespace = self._get_namespace(session_id, shop_id)
        system_prompt = await self._get_system_prompt(user_question="", shop_id=shop_id)
        self.vector_store.add_documents(
            documents=[Document(page_content=f"System: {system_prompt}", metadata={"created_at": time.time()})],
            namespace=namespace
        )

    # ------------------ LLM 응답 생성 ------------------ #
    async def generate_response(self, session_id: str, user_question: str, shop_id: int) -> str:
        namespace = self._get_namespace(session_id, shop_id)

        # 🔹 질문의 상대 날짜를 실제 날짜로 변환
        user_question_for_llm = self.replace_relative_date_with_actual(user_question)

        # 시스템 프롬프트 생성
        system_prompt = await self._get_system_prompt(user_question, shop_id)

        # LangChain 프롬프트 체인 설정
        prompt = ChatPromptTemplate.from_messages([
            ("system", system_prompt),
            MessagesPlaceholder(variable_name="history"),
            ("human", "{query}")
        ])
        chain = prompt | self.model

        # 체인에 대화 기록 연결
        chain_with_history = RunnableWithMessageHistory(
            chain,
            lambda s_id: self.get_session_history(s_id, shop_id=shop_id),
            input_messages_key="query",
            history_messages_key="history",
        )

        # 사용자 질문 Pinecone에 저장 (원본 질문)
        self.vector_store.add_documents(
            documents=[Document(page_content=f"Human: {user_question}", metadata={"created_at": time.time()})],
            namespace=namespace
        )

        # LLM 호출 (날짜 치환된 질문 사용)
        ai_message = await chain_with_history.ainvoke(
            {"query": user_question_for_llm},
            config={"configurable": {"session_id": session_id}}
        )
        bot_answer = ai_message.content

        # AI 답변 Pinecone에 저장
        self.vector_store.add_documents(
            documents=[Document(page_content=f"AI: {bot_answer}", metadata={"created_at": time.time()})],
            namespace=namespace
        )

        return bot_answer

    # ------------------ 시스템 프롬프트 생성 ------------------ #
    async def _get_system_prompt(self, user_question: str, shop_id: int) -> str:
        today = date.today()
        start_date, end_date = None, None

        if "오늘" in user_question:
            start_date, end_date = today, today
        elif "이번 주" in user_question or "이번주" in user_question:
            start_of_week = today - timedelta(days=today.weekday())
            end_of_week = start_of_week + timedelta(days=6)
            start_date, end_date = start_of_week, end_of_week
        elif "이번 달" in user_question or "이번달" in user_question:
            start_of_month = today.replace(day=1)
            next_month = start_of_month.replace(month=start_of_month.month % 12 + 1, day=1)
            end_of_month = next_month - timedelta(days=1)
            start_date, end_date = start_of_month, end_of_month

        # DB 조회
        reservations = await ReservationRepo.get_by_shop(shop_id, start_date, end_date)

        resv_texts = []
        for r in reservations:
            resv_texts.append(
                f"{r['user_name']} / {r['menu_name']} / "
                f"{r['resv_date'].strftime('%Y-%m-%d')} {r['resv_time'].strftime('%H:%M')} / "
                f"상태: {r['resv_state']}"
            )

        # 디버깅용
        print("📌 processed reservations:", resv_texts)

        system_prompt = self.config.get("system_prompt", "")
        if resv_texts:
            system_prompt += "\n예약 현황:\n" + "\n".join(resv_texts)
        else:
            system_prompt += "\n해당 기간의 예약 내역이 없습니다."

        return system_prompt

    # ------------------ Pinecone 네임스페이스 ------------------ #
    def _get_namespace(self, session_id: str, shop_id: int) -> str:
        return f"{self.config.get('name')}-{shop_id}-{session_id}"
