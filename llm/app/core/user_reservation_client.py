import openai
from dotenv import load_dotenv
from app.models.user_reservation_model import Shop, Menu, RevInfo, ShopAndMenuCategory
from typing import Optional, Dict
import yaml
import os

load_dotenv()

client = openai.AsyncOpenAI()

def load_bot_settings():
    # 현재 스크립트 경로
    dir_path = os.path.dirname(os.path.realpath(__file__))
    # settings 경로로 이동
    settings_path = os.path.join(dir_path, 'settings', 'user_reservation_bot.yaml')
    with open(settings_path, 'r', encoding='utf-8') as f:
        return yaml.safe_load(f)

bot_settings = load_bot_settings()

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
        print(f"챗봇과의 통신에 실패하였습니다: {e}")
    return bot_message

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
        print(f'keyword 확인: {keyword}')
        return keyword
    
    except Exception as e:
        print(f'키워드 생성 중 오류가 발생했습니다: {e}')
        return ''

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
        print(f'추천 문구 생성 중 오류가 발생했습니다 : {e}')
        return default_message
    
async def extract_intent_and_keyword(query: str, shop_and_menu_category: ShopAndMenuCategory) -> Dict:
    config = bot_settings['prompts']['intent_extraction']
    model_config = bot_settings

    prompt = config['user_prompt'].format(
        shop_and_menu_category=shop_and_menu_category,
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
        message = completion.choices[0].message.content.strip()
        return message

    except Exception as e:
        print(f'사용자 요청 분석 중 에러 발생 : {e}')
        return config['default_message']