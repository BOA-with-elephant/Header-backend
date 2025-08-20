# 관리자 예약 관리 챗봇
from fastapi import APIRouter, Path
from app.models.chat_request import ChatRequest
from app.models.chat_response import ChatResponse
from app.services.reservation_chatbot_service import ChatBotService
from app.core.db import database
import traceback
import uuid
from fastapi import HTTPException

router = APIRouter(prefix="/api/v1/my-shops/{shop_id}/chatbot/reservation",tags=["reservation"])
service = ChatBotService("BossReservationBot")

@router.post("/init_conversation")
async def init_conversation(shop_id: int = Path(..., description="샵 코드")):
    session_id = str(uuid.uuid4())
    service.init_session(session_id, shop_id)
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
        print(f"!!!!!! ask_chatbot 함수 내부에서 예외 발생: {e} !!!!!!")
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"ask_chatbot 함수 내부 오류: {str(e)}")
