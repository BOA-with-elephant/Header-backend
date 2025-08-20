from dotenv import load_dotenv
from fastapi import FastAPI
from app.api import bot1_chat
from app.api.user_reservation_chat import router as reservation_recommend_router
from app.api import visitorsbot_chat

# 환경 변수 로그
load_dotenv()

# FastAPI 앱 생성
app = FastAPI(
    title="shop chatbot API",
    description= "이미용 샵 관리를 위한 AI 챗봇 서비스",
    version="1.0.0"
)

# 헬스 체크
@app.get("/health")
def health_check():
    return {"status": "ok",
            "service": "customer-management-chatbot",
            "available_bots": ["visitors"]  # 추후 다른 봇들 추가
    }

@app.get("/")
async def root():
    return {
        "message": "헤어샵 챗봇 API에 오신 것을 환영합니다! 🎨✂️",
        "docs": "/docs",
        "available_endpoints": {
            "고객 관리": "/api/v1/visitors/ask"
        }
    }

# 챗봇 라우터 등록
app.include_router(bot1_chat.router)
# 유저 예약 챗봇 - 기존 예약 내역 기반 추천 메시지 출력
app.include_router(reservation_recommend_router)
app.include_router(visitorsbot_chat.router, prefix="/api/v1")

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,  # 개발용
        log_level="info"
    )