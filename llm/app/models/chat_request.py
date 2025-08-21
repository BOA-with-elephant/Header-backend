from typing import Optional
from pydantic import BaseModel

class ChatRequest(BaseModel):
    session_id: Optional[str] = None
    question: str
