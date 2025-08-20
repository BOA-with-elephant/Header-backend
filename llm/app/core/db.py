import os
from dotenv import load_dotenv
from databases import Database
from sqlalchemy import create_engine, MetaData

load_dotenv()

DATABASE_URL = os.getenv("CHATBOT_DATABASE_URL")

database = Database(DATABASE_URL)
metadata = MetaData()

# 동기용 엔진(마이그레이션용)
engine = create_engine(DATABASE_URL)