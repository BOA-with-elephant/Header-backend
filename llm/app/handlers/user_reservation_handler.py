import logging
from typing import Dict, Any, Optional, List
from app.models.user_reservation_model import Shop, ShopAndMenuCategory
from app.services.user_reservation_service import get_user_reservation_history, get_shop_and_menu_category, search_shops
from app.core.user_reservation_client import generate_re_reservation_message, extract_intent_and_keyword, generate_chit_chat_response


### 응답 형식 정립 함수 ###
def format_response(intent: str, message: str, actions: Optional[List[Dict]] = None, data: Optional[Dict] = None) -> Dict[str, Any]:
    """일관된 API 응답 객체 생성"""
    return {
        "intent": intent,
        "message": {"text": message},
        "actions": actions or [],
        "data": data
    }

def format_navigate_action(label: str, url: str) -> Dict[str, Any]:
    """프론트엔드의 페이지 이동을 위한 action 객체"""
    return {"type": "NAVIGATE", "label": label, "payload": {"url": url}}

def format_shop_details_action(shop_code: int, label: str = "샵 예약하러 가기") -> Dict[str, Any]:
    """프론트엔드에서 샵 상세 정보를 표시하기 위한 action 객체"""
    return {"type": "SHOW_SHOP_DETAILS", "label": label, "payload": {"shopCode": shop_code}}

def handle_error(message: str) -> Dict[str, Any]:
    """일관된 에러 응답 객체"""
    logging.error(f"Error response sent to user: {message}")
    return format_response(intent="error", message=message)


### 정의된 사용자의 의도를 기반으로 메시지 출력 ###
async def handle_greeting(context: Dict[str, Any]) -> Dict[str, Any]:
    """초기 접속 시 환영 메시지"""
    config = context["config"]
    greeting_message = config["response_templates"]["greeting"]
    return format_response(intent="greeting", message=greeting_message)

async def handle_view_reservations(context: Dict[str, Any]) -> Dict[str, Any]:
    """예약 확인에 대한 응답"""
    message = "예약 내역 조회 페이지로 이동하시겠어요?"
    action = format_navigate_action(label="예약 내역 조회하기", url="/shops/reservation")
    return format_response(intent="view_reservations", message=message, actions=[action])

async def handle_cancel_reservation(context: Dict[str, Any]) -> Dict[str, Any]:
    """예약 취소에 대한 응답"""
    message = "예약 취소는 예약 조회 페이지에서 하실 수 있습니다."
    action = format_navigate_action(label="예약 내역 조회하기", url="/shops/reservation")
    return format_response(intent="cancel_reservation", message=message, actions=[action])

async def handle_unknown(context: Dict[str, Any]) -> Dict[str, Any]:
    """알 수 없는(잡담 등)에 대한 응답"""
    query = context["query"]
    response_message = await generate_chit_chat_response(query)
    return format_response(intent="unknown", message=response_message)

async def handle_search_shops(context: Dict[str, Any]) -> Dict[str, Any]:
    """샵 검색 비즈니스 로직"""
    token = context["token"]
    query = context["query"]

    user_history = await get_user_reservation_history(token)
    
    if user_history:
        return await _handle_re_recommendation(user_history)
    else:
        return await _handle_new_search(query)
    

### handle_search_shops 내부 로직 ### 
async def _handle_re_recommendation(user_history) -> Dict[str, Any]:
    """예약 내역이 있는 사용자를 위한 재예약 추천 로직"""
    bot_message = await generate_re_reservation_message(user_history)
    action = format_shop_details_action(shop_code=user_history.shopCode)
    
    shops = await search_shops(keyword=user_history.shopName)
    shop_data = shops[0] if shops else None

    return format_response(
        intent="search_shops",
        message=bot_message,
        actions=[action],
        data={
            "sub_intent": "re_recommendation",
            "recommendation": shop_data.dict() if shop_data else None
        }
    )

async def _handle_new_search(query: str) -> Dict[str, Any]:
    """예약 내역이 없는 사용자를 위한 신규 검색 로직"""
    categories = await get_shop_and_menu_category()
    if not categories:
        return handle_error(message="샵과 메뉴 카테고리 정보를 불러오는 데 실패했습니다. 잠시 후 다시 시도해주세요.")

    extracted_json = await extract_intent_and_keyword(query=query, shop_and_menu_category=categories)
    keyword = extracted_json.get("keyword")
    category_code = extracted_json.get("categoryCode")

    if not keyword and not category_code:
        bot_message = "어떤 스타일을 원하세요? '염색' '네일' 등 원하시는 뷰티 시술을 말씀해주세요."
        return format_response(
            intent="search_shops",
            message=bot_message,
            data={"sub_intent": "clarification_needed"}
        )

    shops = []
    if keyword:
        shops = await search_shops(keyword=keyword)
    elif category_code:
        shops = await search_shops(category_code=category_code)

    if shops:
        recommended_shop = shops[0]
        bot_message = _generate_dynamic_recommend_message(keyword, category_code, recommended_shop, categories)
        action = format_shop_details_action(shop_code=recommended_shop.shopCode)
        return format_response(
            intent="search_shops",
            message=bot_message,
            actions=[action],
            data={
                "sub_intent": "new_recommendation",
                "recommendation": recommended_shop.dict()
            }
        )
    else:
        search_term = keyword if keyword else next((c.categoryName for c in categories.shopCategories if c.categoryCode == category_code), "")
        bot_message = f"아쉽지만 '{search_term}' 관련 샵을 찾지 못했어요. 다른 키워드로 다시 시도해 주시겠어요?"
        return format_response(
            intent="search_shops",
            message=bot_message,
            data={"sub_intent": "no_results"}
        )

def _generate_dynamic_recommend_message(keyword: Optional[str], category_code: Optional[int], recommended_shop: Shop, categories: ShopAndMenuCategory) -> str:
    """검색 조건과 결과에 따라 동적인 추천 메시지하는 내부 함수."""
    if keyword:
        relevant_menus = [menu for menu in recommended_shop.menus if keyword in menu.menuName]
        popular_menu = max(relevant_menus, key=lambda m: m.menuRevCount) if relevant_menus else None
        if popular_menu and popular_menu.menuRevCount > 0:
            return f"'{keyword}' 관련 메뉴 중 주변의 '{recommended_shop.shopName}'은(는) 어떠세요? 이 샵의 인기 메뉴 '{popular_menu.menuName}'를 예약해 보세요!"
        else:
            return f"'{keyword}' 관련 샵 중 주변의 '{recommended_shop.shopName}'을(를) 추천드려요."
    
    elif category_code:
        category_name = next((cat.categoryName for cat in categories.shopCategories if cat.categoryCode == category_code), "")
        popular_menu = max(recommended_shop.menus, key=lambda m: m.menuRevCount) if recommended_shop.menus else None
        if popular_menu and popular_menu.menuRevCount > 0:
            return f"주변의 인기 샵 '{recommended_shop.shopName}'을(를) 추천드려요. 이 샵의 인기 메뉴 '{popular_menu.menuName}'를 예약해 보세요!"
        else:
            return f"'{category_name}' 카테고리 중 주변의 '{recommended_shop.shopName}'을(를) 추천드려요."
    
    return f"'{recommended_shop.shopName}'을(를) 추천드려요. 한번 둘러보시겠어요?" # Fallback
