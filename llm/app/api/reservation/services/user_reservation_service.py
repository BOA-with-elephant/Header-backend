from pydantic import BaseModel
import requests
from typing import Optional

# 고객 예약 내역 응답을 받을 BaseModel
class RevInfo(BaseModel):
    shopCode: int
    shopName: str
    menuCode: int
    menuCategoryCode: int
    menuName: str
    revCount: int

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
