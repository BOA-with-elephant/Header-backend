# reservation.py
from sqlalchemy import Table, Column, Integer, String, Date, Time, ForeignKey, MetaData
from app.core.db import metadata  # 이미 metadata를 import했다고 가정

Reservation = Table(
    'tbl_reservation',  # 실제 테이블 이름과 맞춰야 함
    metadata,
    Column('resv_code', Integer, primary_key=True, autoincrement=True),
    Column('user_code', Integer, ForeignKey('tbl_user.user_code'), nullable=False),
    Column('shop_code', Integer, ForeignKey('tbl_shop.shop_code'), nullable=False),
    Column('menu_code', Integer, ForeignKey('tbl_menu.menu_code'), nullable=False),
    Column('resv_date', Date, nullable=False),
    Column('resv_time', Time, nullable=False),
    Column('user_comment', String(255), nullable=True),
    Column('resv_state', String(20), nullable=False, default='예약확정'),
)

