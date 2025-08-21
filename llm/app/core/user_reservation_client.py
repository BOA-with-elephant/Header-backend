import openai
import logging
from dotenv import load_dotenv
from app.models.user_reservation_model import Shop, Menu, RevInfo, ShopAndMenuCategory
from typing import Optional, Dict
import yaml
import os
import json

load_dotenv()

client = openai.AsyncOpenAI()

# 유저 예약 챗봇 세팅 함수
def load_bot_settings():
    # 현재 스크립트 경로
    dir_path = os.path.dirname(os.path.realpath(__file__))
    # settings 경로로 이동
    settings_path = os.path.join(dir_path, 'settings', 'user_reservation_bot.yaml')
    with open(settings_path, 'r', encoding='utf-8') as f:
        return yaml.safe_load(f)

bot_settings = load_bot_settings()

# 예약 내역이 있는 회원에게 재예약을 추천
async def generate_re_reservation_message(data: Optional[RevInfo]):
    config = bot_settings['prompts']['re_reservation']
    model_config = bot_settings

    shop_name = data.shopName
    menu_name = data.menuName
    rev_count = data.revCount

    prompt = config['user_prompt'].format(
        shop_name=shop_name,
        menu_name=menu_name,
        rev_count=rev_count
    )
    
    bot_message = config['default_message'].format(
        shop_name=shop_name,
        menu_name=menu_name,
        rev_count=rev_count
    )

    try:
        completion = await client.chat.completions.create(
            model=model_config['model'],
            messages=[
                {
                    'role': 'system',
                    'content': config['system_prompt']
                },
                {
                    'role': 'user',
                    'content': prompt
                }
            ],
            temperature=model_config['temperature'],
            max_tokens=model_config['max_tokens']
        )
        message_content = completion.choices[0].message.content
        if message_content:
            bot_message = message_content.strip()
    except Exception as e:
        logging.error(f"챗봇과의 통신에 실패하였습니다: {e}")
    return bot_message

# 사용자가 특정 메뉴로 검색을 원할 경우 사용
async def generate_keyword_from_menu(menu_name: str) -> str:
    config = bot_settings['prompts']['keyword_generation']
    model_config = bot_settings

    prompt = config['user_prompt'].format(menu_name=menu_name)
    
    try:
        completion = await client.chat.completions.create(
            model=model_config['model'],
            messages=[
                {
                    'role': 'user',
                    'content': prompt
                }
            ],
            temperature=0,
            max_tokens=model_config['max_tokens']
        )

        keyword = completion.choices[0].message.content.strip()
        logging.info(f'keyword 확인: {keyword}')
        return keyword
    
    except Exception as e:
        logging.error(f'키워드 생성 중 오류가 발생했습니다: {e}')
        return config['default_message']

# 검색된 메뉴 기반 샵으로 추천 메시지 출력
async def generate_recommend_message_by_menu_keyword(menu: str, recommend_shop: Shop, recommend_menu: Menu) -> str:
    config = bot_settings['prompts']['new_recommendation']
    model_config = bot_settings

    shop_name = recommend_shop.shopName
    menu_name = recommend_menu.menuName

    prompt = config['user_prompt'].format(
        menu=menu,
        shop_name=shop_name,
        menu_name=menu_name
    )

    default_message = config['default_message'].format(
        shop_name=shop_name,
        menu_name=menu_name
    )

    try:
        completion = await client.chat.completions.create(
            model=model_config['model'],
            messages=[
                {
                    'role': 'user',
                    'content': prompt
                }
            ],
            temperature=model_config['temperature'],
            max_tokens=model_config['max_tokens']
        )
        message = completion.choices[0].message.content.strip()
        return message

    except Exception as e:
        logging.error(f'추천 문구 생성 중 오류가 발생했습니다 : {e}')
        return default_message

# 사용자의 의도를 파악하고 키워드를 추출
async def extract_intent_and_keyword(query: str, shop_and_menu_category: ShopAndMenuCategory) -> Dict:
    config = bot_settings['prompts']['intent_extraction']
    model_config = bot_settings

    # 1. 데이터를 LLM이 이해하기 쉬운 형태로 가공
    shop_categories_json = json.dumps([cat for cat in shop_and_menu_category.shopCategories], ensure_ascii=False, indent=2)
    menu_keywords_list = json.dumps([cat.menuCategoryName for cat in shop_and_menu_category.menuCategories], ensure_ascii=False)

    prompt = config['user_prompt'].format(
        shop_categories_json=shop_categories_json,
        menu_keywords_list=menu_keywords_list,
        query=query
    )

    try:
        completion = await client.chat.completions.create(
            model=model_config['model'],
            response_format={ "type": "json_object" },
            messages=[
                {
                    'role': 'user',
                    'content': prompt
                }
            ],
            temperature=model_config['temperature'],
            max_tokens=model_config['max_tokens']
        )
        message_content = completion.choices[0].message.content.strip()
        
        # 2. LLM 응답 후 검증 로직
        extracted_data = json.loads(message_content)
        
        valid_codes = {cat.categoryCode for cat in shop_and_menu_category.shopCategories}
        valid_keywords = {cat.menuCategoryName for cat in shop_and_menu_category.menuCategories}

        if extracted_data.get('categoryCode') and extracted_data['categoryCode'] not in valid_codes:
            logging.warning(f"존재하지 않는 카테고리 코드를 반환함: {extracted_data['categoryCode']}")
            extracted_data['categoryCode'] = None # 무효한 값이면 null 처리

        if extracted_data.get('keyword') and extracted_data['keyword'] not in valid_keywords:
            logging.warning(f"존재하지 않는 키워드를 반환함: {extracted_data['keyword']}")
            extracted_data['keyword'] = None # 무효한 값이면 null 처리

        return extracted_data

    except Exception as e:
        logging.error(f'사용자 요청 분석 중 에러 발생 : {e}')
        return {"error": config['default_message']}

# 사용자의 의도를 파악함 (예약 조회/샵 검색/일반 질문 등)
async def determine_reservation_intent(query: str) -> Dict:
    config = bot_settings['prompts']['reservation_assistant']
    model_config = bot_settings

    prompt = config['user_prompt'].format(query=query)

    try:
        completion = await client.chat.completions.create(
            model=model_config['model'],
            response_format={"type": "json_object"},
            messages=[
                {
                    'role': 'user',
                    'content': prompt
                }
            ],
            temperature=0,
            max_tokens=50
        )
        message_content = completion.choices[0].message.content.strip()
        return json.loads(message_content)

    except Exception as e:
        logging.error(f'사용자 의도 파악 중 에러 발생 : {e}')
        return {"intent": "unknown", "error": config['default_message']}

# 의도를 알 수 없는 사용자의 요청을 처리함
async def generate_chit_chat_response(query: str) -> str:
    config = bot_settings['prompts']['chit_chat']
    model_config = bot_settings

    prompt = config['user_prompt'].format(query=query)
    default_message = config['default_message']

    try:
        completion = await client.chat.completions.create(
            model=model_config['model'],
            messages=[
                {
                    'role': 'system',
                    'content': config['system_prompt']
                },
                {
                    'role': 'user',
                    'content': prompt
                }
            ],
            temperature=model_config['temperature'],
            max_tokens=model_config['max_tokens']
        )
        message_content = completion.choices[0].message.content
        if message_content:
            return message_content.strip()
        else:
            return default_message
    except Exception as e:
        logging.error(f"응답 생성 중 에러 발생: {e}")
        return default_message