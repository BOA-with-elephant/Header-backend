from fastapi import APIRouter, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from dotenv import load_dotenv
from app.services.user_reservation_service import get_user_reservation_history, get_shop_and_menu_category
from app.core.user_reservation_client import generate_re_reservation_message, extract_intent_and_keyword, determine_reservation_intent
from app.models.user_reservation_model import Request

load_dotenv()

router = APIRouter(prefix='/reservation', tags=['user-reservation'])

security_scheme = HTTPBearer()

@router.post("/chat")
async def reservation_assistant(
    request: Request,
    credentials: HTTPAuthorizationCredentials = Depends(security_scheme)
):
    token = credentials.credentials
    query = request.query

    intent_data = await determine_reservation_intent(query)
    intent = intent_data.get("intent", "unknown")

    if intent == "view_reservations":
        return {
            "intent": "view_reservations",
            "message": "예약 내역 조회 페이지로 이동하시겠어요?",
            "url": "/shops/reservation"
        }
    
    elif intent == "cancel_reservation":
        return {
            "intent": "cancel_reservation",
            "message": "예약 취소는 예약 조회 페이지에서 가능합니다.",
            "url": "/shops/reservation"
        }

    elif intent == "search_shops":
        user_history = await get_user_reservation_history(token)
        
        if user_history:
            # 예약 내역이 있으면 -> 재예약 추천
            bot_message = await generate_re_reservation_message(user_history)
            return {
                "intent": "search_shops",
                "sub_intent": "re_recommendation",
                "message": bot_message,
                "recommend_url": f'/shops/{user_history.shopCode}' # TODO. 프론트엔드에 클릭 액션으로 샵 상세조회 페이지를 구현해놔서 url이 없음.. 어캄
            }
        else:
            # 예약 내역이 없으면 -> 키워드 기반 검색
            categories = await get_shop_and_menu_category()
            extracted_json = await extract_intent_and_keyword(query=query, shop_and_menu_category=categories)
            return {
                "intent": "search_shops",
                "sub_intent": "new_search",
                "message": "", # TODO. bot_message에 ' 샵 이름 '은 어떠세요? 같은 메시지 담아 넘기기
                "result": extracted_json
            }

        # TODO. 예약 내역이 있는 고객이라도 새로운 예약 추천(키워드 기반 검색) 가능해야 함, 그러나 예약 내역이 없으면 재예약 추천을 못 하는 문제는 어떻게 해결할 건지?
            
    else: # unknown
        return {
            "intent": "unknown",
            "message": "죄송합니다. 요청하신 내용을 이해하지 못했습니다. '예약 확인', '샵 검색'과 같이 말씀해주시겠어요?"
        }

"""
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsInJvbGUiOiJST0xFX0FETUlOIiwiZXhwIjoxNzU1NzgxNDIzLCJzaG9wQ29kZSI6MTF9.nIkseF5WMfNWfLqKuRZA8iTfsXf-xpfs2LUSHj-xZSTzKzcFfZh_1TTDdL0bA5L5Tk5PAPMPquURWXpY3mJfeg
"""
