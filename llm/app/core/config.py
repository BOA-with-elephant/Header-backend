import yaml
import os
from pathlib import Path
from functools import lru_cache
from pydantic_settings import BaseSettings

class ChatBotConfig:
    def __init__(self, bot_name: str):
        file_path = Path(__file__).parent / "settings" / f"{bot_name}.yaml"

        if not file_path.exists():
            raise FileNotFoundError(f"챗봇 설정 파일을 찾을 수 없습니다: {file_path}")

        with open(file_path, "r", encoding="utf-8") as f:
            self.config = yaml.safe_load(f)

    def get(self, key: str, default=None):
        """중첩된 키 지원 (예: 'spring_api.base_url')"""
        keys = key.split('.')
        value = self.config

        for k in keys:
            if isinstance(value, dict) and k in value:
                value = value[k]
            else:
                return default

        return value

class Settings(BaseSettings):
    # OpenAI 설정
    openai_api_key: str = os.getenv("OPENAI_API_KEY", "")

    # Spring Boot API 설정
    spring_api_base_url: str = os.getenv("SPRING_API_BASE_URL", "http://127.0.0.1:8080")

    # FastAPI 설정
    app_name: str = "헤어샵 챗봇 API"
    debug: bool = os.getenv("DEBUG", "false").lower() == "true"

    # 로깅 설정
    log_level: str = os.getenv("LOG_LEVEL", "INFO")

    class Config:
        env_file = ".env"

@lru_cache()
def get_settings():
    return Settings()