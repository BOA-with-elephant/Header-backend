from fastapi import FastAPI, HTTPException, APIRouter, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import openai
from dotenv import load_dotenv
from llm.app.api.reservation.services.user_reservation_service import get_user_reservation_history

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
    print(data)

    if not data:
        raise HTTPException(status_code=404, detail='예약 정보 찾을 수 없음') # TODO. 예약 정보가 없을 경우 새로운 샵 예약 권장 메시지로 넘어가게 할 것
    
    # data에서 필요한 정보 추출
    shop_name = data.shopName
    shop_code = data.shopCode
    menu_name = data.menuName
    rev_count = data.revCount

    # 정보 기반 프롬프트 생성
    prompt = f"""
    당신은 친절하고 똑똑한 뷰티샵 예약 담당자입니다.
    당신의 임무는 짧고, 강렬하고, 설득력있는 메시지를 생성하는 것입니다.

    규칙:
    - 메시지는 자연스럽고 친근한 한국어여야 합니다.
    - 반드시 샵 이름을 언급하세요
    - 마지막에는 반드시 다시 예약을 권장하는 메시지를 출력해야 해. 예를 들어 "다시 예약하시겠어요?" 같은 메시지 말이야.
    - 다른 인사말이나 설명 등을 추가하지 말고, 짧은 문장으로 말해.
    - 고객은 이 샵을 **다시** 예약할 것인지 추천받는 것이라는 점을 명심해줘.

    여기 고객의 예약 정보야.
    ---
    샵 이름: {shop_name}
    메뉴 이름: {menu_name}
    메뉴 예약 횟수: {rev_count}
    ---
    """

    # AI와 통신이 실패했을 경우 사용될 기본 메시지
    bot_message = f"{shop_name}에서 {menu_name}을 {rev_count}회 예약하셨네요! 다시 예약하시겠어요?" 

    try:
        completion = await client.chat.completions.create(
            model='gpt-4o-mini',
            messages=[
                {
                    'role': 'system',
                    'content': '당신은 도움 주기를 좋아하는 마케팅 담당자입니다.'
                },
                {
                    'role': 'user',
                    'content': prompt
                }
            ],
            temperature=0.7,
            max_completion_tokens=100
        )
        print(completion)
        message_content = completion.choices[0].message.content
        if message_content:
            bot_message = message_content.strip()
    except Exception as e:
        print(f"AI와의 통신에 실패하였습니다: {e}")

    res_data = {
        'message': bot_message, # AI가 생성한 추천 문구
        'recommend_url': f'/shops/{shop_code}' # 만약 사용자가 '예'를 클릭한다면 로딩할 샵 상세정보 프론트 화면
    }

    return res_data    