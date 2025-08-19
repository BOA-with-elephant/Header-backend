import openai
from dotenv import load_dotenv
from fastapi import HTTPException
from app.api.reservation.services.user_reservation_service import get_user_reservation_history, Shop, Menu, RevInfo
from typing import Optional, List

load_dotenv()

client = openai.AsyncOpenAI()

async def generate_re_reservation_message(data: Optional[RevInfo]):

    # data에서 필요한 정보 추출
    shop_name = data.shopName
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
    return bot_message

async def generate_keyword_from_menu(menu_name: str) -> str:
    prompt = f"""
    다음 뷰티샵 시술 메뉴 이름에서 가장 핵심적인 검색 키워드 단어 하나만 추출해줘.
    예를 들어, '프리미엄 여성 컷'에서는 '컷'을 추출하고, '고급 두피 마사지'에서는 '두피 마사지' 혹은 '마사지'를 추출해.
    오직 키워드 하나만 응답해야 해.

    메뉴 이름: '{menu_name}'
    """
    try:
        completion = await client.chat.completions.create(
            model='gpt-4o-mini',
            messages=[
                {
                    'role': 'user',
                    'content': prompt
                }
            ],
            temperature=0,
            max_completion_tokens=100
        )

        keyword = completion.choices[0].message.content.strip()

        print(f'keyword 확인: {keyword}')
        return keyword
    
    except Exception as e:
        print(f'키워드 생성 중 오류가 발생했습니다: {e}')
        return ''

async def generate_recommend_message_by_menu_keyword(menu: str, recommend_shop: Shop, recommend_menu: Menu) -> str:

    shop_name = recommend_shop.shopName
    menu_name = recommend_menu.menuName

    prompt = f"""
    당신은 친절하고 똑똑한 뷰티샵 예약 부서 추천 전문가입니다.
    당신의 임무는 짧고, 강렬하고, 설득력있는 메시지를 생성하는 것입니다.
    이를 위해 당신에게 사용자의 기존 예약 정보와, 우리가 찾은 인근 샵의 인기 메뉴 정보를 주겠습니다.

    규칙:
    - 메시지는 자연스럽고 친근한 한국어여야 합니다.
    - 반드시 샵 이름을 언급하세요
    - 다른 인사말이나 설명 등을 추가하지 말고, 짧은 문장으로 말해.
    - 마지막에 고객의 의견을 묻는 뉘앙스의 질문을 덧붙여줘. 예를 들어 '어떠세요?' 같은 질문 말이야.

    여기 관련된 정보야.
    ---
    사용자의 기존 예약 메뉴: '{menu}'
    추천할 샵 이름: '{shop_name}'
    추천할 인기 메뉴: '{menu_name}'
    ---

    예시: '{menu}를 예약하신 내역을 찾았습니다. 근처 {shop_name}에서 가장 인기있는 {menu_name}은 어떠세요?'
    """

    try:
        completion = await client.chat.completions.create(
            model='gpt-4o-mini',
            messages={
                'role': 'user',
                'content': prompt
            },
            temperature=0.7,
            max_completion_tokens=100
        )
        message = completion.choices[0].message.content.strip()
        return message

    except Exception as e:
        print(f'추천 문구 생성 중 오류가 발생했습니다 : {e}')
        return f'{shop_name}의 인기 메뉴 {menu_name}을 추천드려요. 예약하시겠어요?'