from dotenv import load_dotenv
import os

load_dotenv()
# load_dotenv(dotenv_path=os.path.join(os.path.dirname(os.path.dirname(__file__)), '.env'))
# print("✅ OPENAI_API_KEY:", os.environ.get("OPENAI_API_KEY"))

from fastapi import FastAPI
from app.api import bot1_chat
from app.api import bossReservation_chat
from fastapi.middleware.cors import CORSMiddleware
from app.core.db import database

app = FastAPI(title="LLM Chat Service")

# 헬스체크
@app.get("/health")
def health_check():
    return {"status": "ok"}

# CORS 미들웨어 추가
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:8080", "https://www.headercrm.site", "https://headercrm.site"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# DB 연결
@app.on_event("startup")
async def startup():
    await database.connect()

@app.on_event("shutdown")
async def shutdown():
    await database.disconnect()

# 챗봇 라우터 등록
app.include_router(bot1_chat.router)
app.include_router(bossReservation_chat.router)