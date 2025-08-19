from fastapi import FastAPI, HTTPException, APIRouter, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import openai
from dotenv import load_dotenv
from app.api.reservation.services.user_reservation_service import get_user_reservation_history
from app.api.reservation.services.ai_client import generate_keyword_from_menu, generate_re_reservation_message

load_dotenv()

app = FastAPI()

client = openai.AsyncOpenAI()

router = APIRouter(prefix='/reservation', tags=['user-reservation'])

security_scheme = HTTPBearer()

# 재예약 권장 메시지 생성 챗봇 라우터
@router.get('/reservation-recommendation-by-llm')
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