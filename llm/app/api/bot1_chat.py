from fastapi import APIRouter
from app.models.chat_request import ChatRequest
from app.models.chat_response import ChatResponse
from app.services.chatbot_service import ChatBotService

router = APIRouter(prefix="/bot1", tags=["bot1"])
service = ChatBotService("bot1")

@router.post("/ask", response_model=ChatResponse)
async def ask_chatbot(request: ChatRequest):
    answer = service.generate_response(request.question)
    return ChatResponse(answer=answer)