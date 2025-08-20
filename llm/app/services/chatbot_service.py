from app.core.config import ChatBotConfig

class ChatBotService:
    def __init__(self, bot_name: str):
        self.config = ChatBotConfig(bot_name)

    def generate_response(self, user_question: str):
        system_prompt = self.config.get("system_prompt")
        max_tokens = self.config.get("max_tokens")

        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_question}
        ]

        # POC용 예시: 실제 LLM API 호출 시 messages 전달
        answer = self._call_llm(messages, max_tokens)
        return answer

    def _call_llm(self, messages, max_tokens):
        user_content = [m["content"] for m in messages if m["role"] == "user"][0]
        return f"[{self.config.get('name')}] {user_content} 에 대한 답변입니다."