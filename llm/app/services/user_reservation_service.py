import requests
import httpx
from typing import Optional, List
from app.models.user_reservation_model import RevInfo, Shop

BASE_API_URL = 'http://localhost:8080'

# 고객 예약 내역 요청 함수
async def get_user_reservation_history(token: str) -> Optional[RevInfo]:
    url = f"{BASE_API_URL}/api/v1/shops/reservation/recommendation"

    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    try: 
        res = requests.get(url, headers=headers)

        print(res.status_code)
        print(res.text)

        res.raise_for_status()

        data = res.json()

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

