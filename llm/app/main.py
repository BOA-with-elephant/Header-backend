import os
from dotenv import load_dotenv
import multiprocessing
from fastapi import FastAPI
from app.api import bot1_chat
from app.api import bossReservation_chat
from app.api import visitorsbot_chat
from app.core.db import database
from app.core.redis_manager import redis_manager
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

# 환경 변수 로그
load_dotenv()

@asynccontextmanager
async def lifespan(app: FastAPI):
    # 데이터베이스 연결
    await database.connect()
    
    # Redis 매니저 연결
    await redis_manager.connect()
    
    yield
    
    # 정리 작업
    await redis_manager.disconnect()
    await database.disconnect()

# FastAPI 앱 생성
app = FastAPI(
    title="shop chatbot API",
    description= "이미용 샵 관리를 위한 AI 챗봇 서비스",
    version="1.0.0",
    lifespan=lifespan
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

# CORS 미들웨어 추가
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow all origins for development
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 챗봇 라우터 등록
app.include_router(bot1_chat.router)
app.include_router(bossReservation_chat.router)  # Comment out problematic router
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