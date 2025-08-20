import openai
from dotenv import load_dotenv
from app.models.user_reservation_model import Shop, Menu, RevInfo, ShopAndMenuCategory
from typing import Optional, Dict

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
    - 마지막에는 반드시 다시 예약을 권장하는 메시지를 출력해야 해. 예를 들어 "다시 예약하시겠어요?" 같은 메시지입니다.
    - 다른 인사말이나 설명 등을 추가하지 말고, 짧은 문장으로 말하세요.
    - 고객은 이 샵을 **다시** 예약할 것인지 추천받는 것이라는 점을 명심하세요.

    여기 고객의 예약 정보를 주겠습니다.
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
    다음 뷰티샵 시술 메뉴 이름에서 가장 핵심적인 검색 키워드 단어 하나만 추출하세요
    예를 들어, '프리미엄 여성 컷'에서는 '컷'을 추출하고, '고급 두피 마사지'에서는 '두피 마사지' 혹은 '마사지'를 추출하세요
    오직 키워드 하나만 응답해야 합니다.

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
    - 다른 인사말이나 설명 등을 추가하지 말고, 짧은 문장으로 말하세요.
    - 마지막에 고객의 의견을 묻는 뉘앙스의 질문을 덧붙여줘. 예를 들어 '어떠세요?' 같은 질문입니다.

    여기 관련된 정보입니다.
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
            messages=[
                {
                'role': 'user',
                'content': prompt
                }
            ],
            temperature=0.7,
            max_completion_tokens=100
        )
        message = completion.choices[0].message.content.strip()
        return message

    except Exception as e:
        print(f'추천 문구 생성 중 오류가 발생했습니다 : {e}')
        return f'{shop_name}의 인기 메뉴 {menu_name}을 추천드려요. 예약하시겠어요?'
    
async def extract_intent_and_keyword(query: str, shop_and_menu_category: ShopAndMenuCategory) -> Dict:

    prompt = f"""
    당신은 전문 API 라우팅 어시스턴트입니다.
    주요 역할은 한국어로 작성된 사용자 요청을 분석하고, 주어진 규칙과 데이터를 바탕으로 올바른 API 쿼리 파라미터를 결정하는 것입니다.
    응답은 반드시 다음 구조의 단일 유효한 JSON 객체여야 합니다:
    {{ "reasoning": "단계별 사고 과정이 여기에 들어갑니다.", "categoryCode": null_또는_정수, "keyword": null_또는_문자열 }}

    ## 따라야 할 규칙:
    1. 단계별 사고 (Reasoning):
        먼저 `reasoning` 필드에서 사고 과정을 설명하세요.
        사용자 쿼리를 분석하고 이것이 광범위한 **상점 카테고리**를 의미하는지 아니면 특정 **메뉴 키워드**를 의미하는지 결정하세요.

    2. 하나의 경로만 선택 (배타적 규칙): 이것이 가장 중요한 규칙입니다. 반드시 하나의 경로만 선택해야 합니다.
        - 쿼리가 `categoryCode`에 매핑되면, `keyword` 필드는 반드시 `null`이어야 합니다.
        - 쿼리가 `keyword`에 매핑되면, `categoryCode` 필드는 반드시 `null`이어야 합니다.
        - `categoryCode`와 `keyword` 모두에 값을 가지는 것은 절대 허용되지 않습니다.

    3. 데이터 조회: 
        - 아래 제공된 데이터를 사용하여 올바른 `categoryCode`를 찾거나 `keyword`의 맥락을 이해하세요.

    4. 모호함 처리:
        - 사용자의 요청이 너무 모호하거나 제공된 카테고리와 완전히 무관한 경우 (예: "오늘 저녁 약속 있는데 뭐하지?"), `categoryCode`와 `keyword` 모두 `null`이어야 합니다.

    ## 사용 가능한 데이터:
    {shop_and_menu_category}

    ## 예시:
    요청: "머리 자르고 싶은데 샵 추천해줘"
    응답 (반드시 정해진 JSON 객체 형태로 대답합니다):
    
    {{
    "reasoning": "사용자가 명시적으로 '자르고 싶다'고 언급했습니다. 이는 일반적인 상점 유형보다는 특정 행동(메뉴 키워드)입니다. 가장 관련성 높은 키워드는 '컷'입니다. 따라서 키워드 경로를 사용하고 categoryCode를 null로 설정합니다.",
    "categoryCode": null,
    "keyword": "컷"
    }}

    ---

    이제, 사용자 요청을 분석하여 JSON 객체를 반환하세요
    ---
    사용자 요청: {query}
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
            temperature=0.7,
            max_completion_tokens=100
        )
        message = completion.choices[0].message.content.strip()
        return message

    except Exception as e:
        print(f'사용자 요청 분석 중 에러 발생 : {e}')
        return '현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.'