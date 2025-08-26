import os
import httpx
import logging
from typing import Optional, List
from app.models.user_reservation_model import RevInfo, Shop, ShopAndMenuCategory
from cachetools import TTLCache

# 샵 및 메뉴 카테고리 캐싱 처리
# 1개 아이템을 1시간(3600초) 동안 캐시 유지
category_cache = TTLCache(maxsize=1, ttl=3600)

def detect_url():
    """ Docker / local 환경 감지 -> url 반환 함수 """
    if "HOSTNAME" in os.environ:
        logging.info("🐳 도커 환경이 감지되었습니다. [llm:8000]")
        return "http://llm:8000"
    else:
        logging.info("💻 로컬 환경으로 추정됩니다. [localhost:8080]")
        return "http://localhost:8080"

BASE_API_URL = detect_url()

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
                if rev_info_data: # rev-info의 값이 None이 아닌지 확인
                    return RevInfo(**rev_info_data)
                else:
                    # 예약 내역이 없는 경우
                    return None
            else:
                logging.warning("올바른 JSON 응답이 아닙니다: %s", data)
                return None
        except httpx.RequestError as e:
            logging.error(f"예약 내역 요청 중 통신 오류 발생: {e}")
            return None
        except Exception as e:
            logging.error(f"예약 내역 처리 중 예외 발생: {e}")
            return None
    
# 메뉴 키워드 혹은 카테고리 코드를 기반으로 샵 검색 URL 생성
async def search_shops(keyword: Optional[str] = None, category_code: Optional[int] = None) -> List[Shop]:
    if not keyword and not category_code:
        raise ValueError("카테고리 코드 혹은 키워드를 반환해야 합니다.")
    if keyword and category_code:
        raise ValueError("카테고리 코드 혹은 키워드 중 하나만 반환해야 합니다.")

    all_shops = []
    base_search_url = f'{BASE_API_URL}/api/v1/shops'
    
    # 동적으로 URL 파라미터 설정
    if keyword:
        query_param = f"keyword={keyword}"
    else:
        query_param = f"category={category_code}"

    async with httpx.AsyncClient() as client:
        for page in range(3): # 0, 1, 2 페이지 검색
            try:
                url = f'{base_search_url}?{query_param}&page={page}'
                res = await client.get(url)
                res.raise_for_status()

                data = res.json()
                shops_data = data.get('results', {}).get('shops', [])

                if not shops_data:
                    break 

                for shop_data in shops_data:
                    all_shops.append(Shop(**shop_data))
            except httpx.HTTPStatusError as e:
                logging.error(f'샵 검색 중 HTTP 에러가 발생했습니다. URL: {url}: {e}')
                break
            except Exception as e:
                logging.error(f'샵 검색 중 예외 발생: {e}')
                break
    return all_shops

# llm 모델 학습을 위해 샵 및 메뉴 카테고리 로딩
async def get_shop_and_menu_category() -> Optional[ShopAndMenuCategory]:
    # 1. 캐시에서 먼저 조회
    if 'categories' in category_cache:
        return category_cache['categories']

    # 2. 캐시에 없으면 API 호출
    async with httpx.AsyncClient() as client:
        try:
            url = f'{BASE_API_URL}/api/v1/shops/shop-menu-categories'
            res = await client.get(url)
            res.raise_for_status() # 버그 수정: () 추가

            data = res.json()
            category_data = data.get('results', {}).get('categories', {})

            if category_data:
                result = ShopAndMenuCategory(**category_data)
                # 3. 결과를 캐시에 저장
                category_cache['categories'] = result
                return result

        except httpx.HTTPStatusError as e:
            logging.error(f'메뉴 및 샵 카테고리를 불러오는 중 HTTP 에러가 발생했습니다: {e}')
        except Exception as e:
            logging.error(f'카테고리 처리 중 예외 발생: {e}')
    
    return None
