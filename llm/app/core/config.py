import yaml
from pathlib import Path

class ChatBotConfig:
    def __init__(self, bot_name: str):
        file_path = Path(__file__).parent / "settings" / f"{bot_name}.yaml"
        with open(file_path, "r", encoding="utf-8") as f:
            self.config = yaml.safe_load(f)

    def get(self, key: str, default=None):
        return self.config.get(key, default)