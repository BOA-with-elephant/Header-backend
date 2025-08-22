from typing import Dict, Any, Optional, Literal
from pydantic import BaseModel
from datetime import datetime

class DataRequest(BaseModel):
    """Redis Stream으로 전송되는 데이터 요청"""
    request_type: Literal[
        "customer_search",
        "customer_detail", 
        "visit_history",
        "today_reservations",
        "memo_update"
    ]
    shop_id: int
    parameters: Dict[str, Any] = {}
    timestamp: datetime = datetime.now()

class DataResponse(BaseModel):
    """Redis Stream으로 수신되는 데이터 응답"""
    correlation_id: str
    status: Literal["success", "error", "not_found"]
    data: Dict[str, Any] = {}
    error: Optional[str] = None
    timestamp: datetime = datetime.now()

# 각 요청 타입별 파라미터 스키마
class CustomerSearchParams(BaseModel):
    customer_name: str

class CustomerDetailParams(BaseModel):
    client_code: str

class VisitHistoryParams(BaseModel):
    client_code: str

class TodayReservationsParams(BaseModel):
    pass  # shop_id만 필요

class MemoUpdateParams(BaseModel):
    client_code: str
    memo_content: str