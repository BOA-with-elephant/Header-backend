# app/models/menu.py
from sqlalchemy import Table, Column, Integer, String, Boolean
from app.core.db import metadata

Menu = Table(
    'tbl_menu',
    metadata,
    Column('menu_code', Integer, primary_key=True, autoincrement=True),
    Column('category_code', Integer, nullable=False, comment='카테고리 코드'),
    Column('shop_code', Integer, nullable=False, comment='샵 코드'),
    Column('menu_name', String(40), nullable=False, comment='시술명'),
    Column('menu_price', Integer, nullable=False, comment='시술가격'),
    Column('est_time', Integer, nullable=False, comment='예상소요시간'),
    Column('is_active', Boolean, nullable=False, default=True, comment='활성 여부')
)
