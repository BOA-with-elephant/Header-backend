from fastapi import HTTPException, APIRouter, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from dotenv import load_dotenv
from app.services.user_reservation_service import get_user_reservation_history, search_shop_by_menu_name, get_shop_and_menu_category
from app.core.user_reservation_client import generate_keyword_from_menu, generate_re_reservation_message, Menu, Shop, generate_recommend_message_by_menu_keyword, extract_intent_and_keyword

load_dotenv()

router = APIRouter(prefix='/reservation/chat', tags=['user-reservation'])

security_scheme = HTTPBearer()

# 재예약 권장 메시지 생성 챗봇 라우터
@router.get('/re-recommendation')
async def create_recommendation_by_llm(credentials: HTTPAuthorizationCredentials = Depends(security_scheme)):
    token = credentials.credentials
    # token의 유효성 검사는 HTTPAuthorizationCredentials 측에서 대신 실행

    # 유저 예약 정보 data
    data = await get_user_reservation_history(token)
    for d in data:
        print(data)
        shop_code = data.shopCode # url에 사용할 shopCode 추출

    if not data:
        raise HTTPException(status_code=404, detail='예약 정보 찾을 수 없음') # TODO. 예약 정보가 없을 경우 새로운 샵 예약 권장 메시지로 넘어가게 할 것

    bot_message = await generate_re_reservation_message(data)

    res_data = {
        'message': bot_message, # AI가 생성한 추천 문구
        'recommend_url': f'/shops/{shop_code}' # 만약 사용자가 '예'를 클릭한다면 로딩할 샵 상세정보 프론트 화면
    }

    return res_data

@router.get('/new-recommendation')
async def create_new_recommendation(credentials: HTTPAuthorizationCredentials = Depends(security_scheme)):
    token = credentials.credentials

    user_history = await get_user_reservation_history(token)
    if not user_history:
        raise HTTPException(status_code=404, detail='예약 정보 찾을 수 없음') # TODO. 예약 정보가 없을 경우 새로운 샵 예약 권장 메시지로 넘어가게 할 것
    
    print(f'user_history 확인 : {user_history}')

    keyword = await generate_keyword_from_menu(user_history.menuName)
    if not keyword:
        raise HTTPException(status_code=403, detail='추천 메뉴 키워드 생성에 오류가 발생했습니다')
    
    all_shops = await search_shop_by_menu_name(keyword)
    if not all_shops:
        raise HTTPException(status_code=404, detail=f"'{keyword}' 관련 가게를 찾지 못했습니다.")
    
    recommend_menu: Menu = None
    recommend_shop: Shop = None

    for shop in all_shops:
        for menu in shop.menus:
            print(f'for menu in shop.menus: {menu}')
            recommend_menu = menu
            recommend_shop = shop
            
    
    if not recommend_shop:
        raise HTTPException(status_code=404, detail='추천할 메뉴가 없습니다.')
    
    recommendation_message = await generate_recommend_message_by_menu_keyword(
        recommend_menu.menuName,
        recommend_shop,
        recommend_menu
    )

    return {
        'message': recommendation_message,
        'url': f'/shops/{recommend_shop.shopCode}'
    }

@router.get('/search-recommendation')
async def create_new_recommendation_by_needs(query: str):
    categories = await get_shop_and_menu_category()

    extracted_json = await extract_intent_and_keyword(query=query, shop_and_menu_category=categories)

    return extracted_json

"""
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsInJvbGUiOiJST0xFX1VTRVIiLCJleHAiOjE3NTU2OTU4NTZ9.It0LB7NMlOzsQqrhM3SX2g4MQgd4E6ndJ0Wlz2OOAbxai-6_7y21xaCsUoua7rJ8CwDFz5g0ieGcvY90_Ozmdg
"""
