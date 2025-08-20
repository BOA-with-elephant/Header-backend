# app/models/user.py
from sqlalchemy import Table, Column, Integer, String, Boolean, Date
from app.core.db import metadata

User = Table(
    'tbl_user',
    metadata,
    Column('user_code', Integer, primary_key=True, autoincrement=True),
    Column('user_id', String(20), nullable=True, comment='아이디'),
    Column('user_pwd', String(255), nullable=True, comment='비밀번호'),
    Column('is_admin', Boolean, nullable=False, default=False, comment='관리자여부'),
    Column('user_name', String(255), nullable=False, comment='이름'),
    Column('user_phone', String(20), nullable=False, comment='전화번호'),
    Column('birthday', Date, nullable=True, comment='고객생일'),
    Column('is_leave', Boolean, nullable=False, default=False, comment='탈퇴여부')
)
