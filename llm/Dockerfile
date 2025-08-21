FROM python:3.12 AS python-base

WORKDIR /app

# requirements.txt만 먼저 복사(캐시 최적화)
COPY requirements.txt .

# pip 업그레이드 & 필요한 패키지 복사
RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

# 나머지 소스 전체 복사
COPY . .

# 컨테이너 외부 포트
EXPOSE 8000

# uvicorn 단독 실행 : FastAPI에 최적화 및 단순한 설정
# --workers 4 : CPU 4코어를 활용한 멀티 워커 실행
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "4"]
