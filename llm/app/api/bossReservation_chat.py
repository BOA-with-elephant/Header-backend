# 관리자 예약 관리 챗봇
from fastapi import APIRouter, Path
from app.models.chat_request import ChatRequest
from app.models.chat_response import ChatResponse
from app.services.reservation_chatbot_service import ChatBotService
from app.core.db import database
import logging
import uuid
from fastapi import HTTPException

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/v1/my-shops/{shop_id}/chatbot/reservation",tags=["reservation"])
service = ChatBotService("BossReservationBot")

@router.post("/init_conversation")
async def init_conversation(shop_id: int = Path(..., description="샵 코드")):
    session_id = str(uuid.uuid4())
    await service.init_session(session_id, shop_id)
    return {"session_id" : session_id}

@router.post("", response_model=ChatResponse)
async def ask_chatbot(request: ChatRequest, shop_id: int = Path(..., description="샵 코드")):
    try:
        session_id = request.session_id or str(uuid.uuid4()) # 없으면 새로 생성

        if request.session_id is None:
            await service.init_session(session_id, shop_id)  # 새 세션 초기화

        answer = await service.generate_response(session_id, request.question, shop_id)
        return ChatResponse(session_id=session_id, answer=answer)
    except Exception as e:
        logger.exception("ask_chatbot 함수 내부 오류")
        # 내부 예외 메시지는 로깅으로 남기고, 응답은 일반화된 메시지로 반환
        raise HTTPException(status_code=500, detail="서버 내부 오류가 발생했습니다.") from e
