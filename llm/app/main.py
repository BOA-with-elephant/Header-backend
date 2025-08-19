from fastapi import FastAPI
from app.api import bot1_chat
from app.api.reservation.reservation_recommend import router as reservation_recommend_router

app = FastAPI(title="LLM Chat Service")

# 헬스체크
@app.get("/health")
def health_check():
    return {"status": "ok"}

# 챗봇 라우터 등록
app.include_router(bot1_chat.router)
# 유저 예약 챗봇 - 기존 예약 내역 기반 추천 메시지 출력
app.include_router(reservation_recommend_router) 