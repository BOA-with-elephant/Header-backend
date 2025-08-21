import os
from dotenv import load_dotenv
from databases import Database
from sqlalchemy import create_engine, MetaData

load_dotenv()

DATABASE_URL = os.getenv("CHATBOT_DATABASE_URL")

if not DATABASE_URL:
    raise RuntimeError(
        "Database URL is not set. Please configure CHATBOT_DATABASE_URL "
        "or DATABASE_URL in your environment/.env."
    )

database = Database(DATABASE_URL)
metadata = MetaData()

# 동기용 엔진(마이그레이션용)
engine = create_engine(DATABASE_URL)