import yaml
import logging
from pathlib import Path
from typing import Dict, Any
from fastapi import APIRouter, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from dotenv import load_dotenv
from app.models.user_reservation_model import Request
from app.core.user_reservation_client import determine_reservation_intent

# 핸들러 임포트
from app.handlers.user_reservation_handler import (
    handle_greeting,
    handle_view_reservations,
    handle_cancel_reservation,
    handle_search_shops,
    handle_unknown,
    handle_error
)

# 로깅 기본 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)

load_dotenv()

router = APIRouter(prefix='/reservation', tags=['user-reservation'])

# Bearer JWT 형식의 보안 형식을 처리
security_scheme = HTTPBearer()

# 챗봇 설정 파일 로드
def load_yaml_config():
    config_path = Path(__file__).parent.parent / "core/settings/user_reservation_bot.yaml"
    with open(config_path, 'r', encoding='utf-8') as f:
        return yaml.safe_load(f)

config = load_yaml_config()

# 의도(intent)와 핸들러 함수를 매핑
intent_handlers = {
    "greeting": handle_greeting,
    "view_reservations": handle_view_reservations,
    "cancel_reservation": handle_cancel_reservation,
    "search_shops": handle_search_shops,
    "unknown": handle_unknown,
}

@router.post("/chat")
async def reservation_assistant(
    request: Request,
    credentials: HTTPAuthorizationCredentials = Depends(security_scheme)
) -> Dict[str, Any]:
    """
    사용자 요청을 받아 의도를 파악하고, 적절한 핸들러에 전달하여 응답을 생성하는 API 엔드포인트
    """
    try:
        token = credentials.credentials
        query = request.query

        # 1. 의도 결정
        # 시작 메시지
        if query == "__INIT__":
            intent = "greeting"
        # 시작 메시지가 없는 경우 llm을 활용해 handler에 매핑
        else:
            intent_data = await determine_reservation_intent(query)
            intent = intent_data.get("intent", "unknown")

        # 2. 핸들러에 전달할 컨텍스트 정보 준비
        context = {
            "token": token,
            "query": query,
            "config": config,
        }

        # 3. 적절한 핸들러를 찾아 실행
        handler = intent_handlers.get(intent, handle_unknown)
        response = await handler(context)
        
        return response

    except Exception as e:
        # 예외 처리
        logging.critical(f"예상치 못한 에러가 발생했습니다: {e}", exc_info=True) # exc_info=True로 스택 트레이스 기록
        error_message = config["response_templates"]["error"]["general"]
        return handle_error(message=error_message)