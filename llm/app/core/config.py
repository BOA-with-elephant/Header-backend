import os
import yaml
from pathlib import Path

class ChatBotConfig:
    def __init__(self, bot_name: str):
        file_path = Path(__file__).parent / "settings" / f"{bot_name}.yaml"
        with open(file_path, "r", encoding="utf-8") as f:
            self.config = yaml.safe_load(f)

    def get(self, key: str, default=None):
        # 환경 변수 우선, 없으면 config에서 가져오기
        return os.getenv(key) or self.config.get(key, default)