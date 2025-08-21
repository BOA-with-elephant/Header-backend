import yaml
from pathlib import Path
from fastapi import APIRouter, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from dotenv import load_dotenv
from app.services.user_reservation_service import get_user_reservation_history, get_shop_and_menu_category
from app.core.user_reservation_client import generate_re_reservation_message, extract_intent_and_keyword, determine_reservation_intent
from app.models.user_reservation_model import Request

load_dotenv()

router = APIRouter(prefix='/reservation', tags=['user-reservation'])

security_scheme = HTTPBearer()

# YAML 설정 파일 로드
def load_yaml_config():
    config_path = Path(__file__).parent.parent / "core/settings/user_reservation_bot.yaml"
    with open(config_path, 'r', encoding='utf-8') as f:
        return yaml.safe_load(f)

config = load_yaml_config()

@router.post("/chat")
async def reservation_assistant(
    request: Request,
    credentials: HTTPAuthorizationCredentials = Depends(security_scheme)
):
    token = credentials.credentials
    query = request.query

    # 1. 초기 접속 처리
    if query == "__INIT__":
        return {
            "intent": "greeting",
            "message": {"text": config["response_templates"]["greeting"]},
            "actions": [],
            "data": None
        }

    intent_data = await determine_reservation_intent(query)
    intent = intent_data.get("intent", "unknown")

    # 2. 의도별 응답 분기
    if intent == "view_reservations":
        return {
            "intent": "view_reservations",
            "message": {"text": "예약 내역 조회 페이지로 이동하시겠어요?"},
            "actions": [{
                "type": "NAVIGATE",
                "label": "예약 내역 보기",
                "payload": {"url": "/shops/reservation"}
            }],
            "data": None
        }
    
    elif intent == "cancel_reservation":
        return {
            "intent": "cancel_reservation",
            "message": {"text": "예약 취소는 예약 조회 페이지에서 가능합니다."},
            "actions": [{
                "type": "NAVIGATE",
                "label": "예약 조회로 이동",
                "payload": {"url": "/shops/reservation"}
            }],
            "data": None
        }

    elif intent == "search_shops":
        user_history = await get_user_reservation_history(token)
        
        if user_history:
            # 예약 내역이 있으면 -> 재예약 추천
            bot_message = await generate_re_reservation_message(user_history)
            return {
                "intent": "search_shops",
                "message": {"text": bot_message},
                "actions": [{
                    "type": "SHOW_SHOP_DETAILS",
                    "label": "샵 정보 보기",
                    "payload": {"shopCode": user_history.shopCode}
                }],
                "data": {"sub_intent": "re_recommendation"}
            }
        else:
            # 예약 내역이 없으면 -> 키워드 기반 검색
            categories = await get_shop_and_menu_category()
            extracted_json = await extract_intent_and_keyword(query=query, shop_and_menu_category=categories)
            
            shop_name = extracted_json.get("shopName", "추천 샵")
            bot_message = f"'{shop_name}'은(는) 어떠세요? 마음에 드시면 알려주세요."

            return {
                "intent": "search_shops",
                "message": {"text": bot_message},
                "actions": [],
                "data": {
                    "sub_intent": "new_search",
                    "search_result": extracted_json
                }
            }
            
    else: # unknown 또는 chit-chat
        chit_chat_map = {
            "누구야": "저는 예약을 도와드리는 챗봇입니다.",
            "뭐 할 수 있어": "샵 검색, 예약 확인 및 취소를 도와드릴 수 있어요."
        }
        
        response_message = "죄송합니다. 요청하신 내용을 이해하지 못했습니다. '예약 확인', '샵 검색'과 같이 말씀해주시겠어요?"
        for key, value in chit_chat_map.items():
            if key in query:
                response_message = value
                break
        
        return {
            "intent": "unknown",
            "message": {"text": response_message},
            "actions": [],
            "data": None
        }

"""
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsInJvbGUiOiJST0xFX0FETUlOIiwiZXhwIjoxNzU1NzgxNDIzLCJzaG9wQ29kZSI6MTF9.nIkseF5WMfNWfLqKuRZA8iTfsXf-xpfs2LUSHj-xZSTzKzcFfZh_1TTDdL0bA5L5Tk5PAPMPquURWXpY3mJfeg
"""