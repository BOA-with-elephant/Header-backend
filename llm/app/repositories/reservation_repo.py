from app.core.db import database
from sqlalchemy import select, join, and_
from app.models.reservation import Reservation
from app.models.user import User
from app.models.menu import Menu
from fastapi import HTTPException
import logging
from datetime import date
from sqlalchemy.dialects import mysql

logger = logging.getLogger(__name__)

class ReservationRepo:
    @staticmethod
    async def get_by_shop(shop_id: int, start_date: date = None, end_date: date = None):
        # Join 테이블
        j = join(Reservation, User, Reservation.c.user_code == User.c.user_code).join(
            Menu, Reservation.c.menu_code == Menu.c.menu_code)

        # 기본 쿼리
        query = select(
            Reservation.c.resv_code,
            User.c.user_name,
            Menu.c.menu_name,
            Reservation.c.resv_date,
            Reservation.c.resv_time,
            Reservation.c.user_comment,
            Reservation.c.resv_state
        ).select_from(j).where(Reservation.c.shop_code == shop_id)

        # 날짜 필터링 조건 추가
        if start_date and end_date:
            query = query.where(and_(Reservation.c.resv_date >= start_date, Reservation.c.resv_date <= end_date))

        # 최신순으로 정렬
        query = query.order_by(Reservation.c.resv_date.desc(), Reservation.c.resv_time.desc())

        # ================== << SQL 쿼리 출력 코드 >> ==================
        compiled_query = query.compile(dialect=mysql.dialect())
        print("=" * 50)
        print("실행될 SQL 쿼리:")
        print(compiled_query.string)
        print("파라미터:", compiled_query.params)
        print("=" * 50)
        # ==========================================================

        try :
            result = await database.fetch_all(query)
            print(f"📝 DB 조회 결과({shop_id}): {result}")
            return result
        except Exception as e:
            # logger.error(f"DB 조회 실패 : {e}")
            raise HTTPException(status_code=500, detail=f"데이터베이스 조회 중 오류 발생: {str(e)}")