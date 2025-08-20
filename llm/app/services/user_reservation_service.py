import requests
import httpx
from typing import Optional, List
from app.models.user_reservation_model import RevInfo, Shop, ShopAndMenuCategory
from cachetools import cached, TTLCache

# 샵 및 메뉴 카테고리 캐싱 처리
# 1개 아이템을 1시간(3600초) 동안 캐시 유지
category_cache = TTLCache(maxsize=1, ttl=3600)

BASE_API_URL = 'http://localhost:8080'

# 고객 예약 내역 요청 함수
async def get_user_reservation_history(token: str) -> Optional[RevInfo]:

    async with httpx.AsyncClient() as client:
        try: 
            url = f"{BASE_API_URL}/api/v1/shops/reservation/recommendation"

            headers = {
                "Authorization": f"Bearer {token}",
                "Content-Type": "application/json"
            }

            response = await client.get(url, headers=headers)
            response.raise_for_status()

            data = response.json()

            if data and 'results' in data and 'rev-info' in data['results']:
                rev_info_data = data['results']['rev-info']
                return RevInfo(**rev_info_data)
            else:
                print("올바른 JSON 응답이 아닙니다")
                return None
        except requests.exceptions.RequestException as e:
            print(f"오류 발생: {e}")
            return None
    
# 주어진 키워드로 0~2 페이지까지 검색하여 모든 가게 목록 반환
async def search_shop_by_menu_name(keyword: str) -> List[Shop]:
    all_shops = []
    async with httpx.AsyncClient() as client:
        for page in range(3): # 0, 1, 2 페이지 검색
            try:
                url = f'{BASE_API_URL}/api/v1/shops?keyword={keyword}&page={page}'
                res = await client.get(url)
                res.raise_for_status()

                data = res.json()
                print(f'data 출력: {data}')
                shops_data = data.get('results', {}).get('shops', [])
                print(f'shops_data 출력{shops_data}')

                # 더이상 검색할 정보 없으면 검색 중단
                if not shops_data:
                    break 

                for shop_data in shops_data:
                    all_shops.append(Shop(**shop_data))
            except httpx.HTTPStatusError as e:
                print(f'HTTP 에러가 발생했습니다. 페이지: {page} 키워드: {keyword}: {e}')
                break
            except Exception as e:
                print(f'에러 발생: {e}')
                break
    return all_shops

# llm 모델 학습을 위해 샵 및 메뉴 카테고리 로딩
@cached(cache=category_cache)
async def get_shop_and_menu_category() -> ShopAndMenuCategory:
    
    async with httpx.AsyncClient() as client:
        try:
            url = f'{BASE_API_URL}/api/v1/shops/shop-menu-categories'
            res = await client.get(url)
            res.raise_for_status

            data = res.json()
            print(f'menu category data 출력: {data}')
            category_data = data.get('results', {}).get('categories', {})

            if category_data:
                return ShopAndMenuCategory(**category_data)

        except httpx.HTTPStatusError as e:
            print(f'메뉴 및 샵 카테고리를 불러오는 중 HTTP 에러가 발생했습니다: {e}')
        except Exception as e:
            print(f'에러 발생: {e}')
            return None