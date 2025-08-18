from fastapi import FastAPI
from app.api import bot1_chat

app = FastAPI(title="LLM Chat Service")

# 헬스체크
@app.get("/health")
def health_check():
    return {"status": "ok"}

# 챗봇 라우터 등록
app.include_router(bot1_chat.router)