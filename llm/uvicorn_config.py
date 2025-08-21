import multiprocessing

# 컨테이너 내부에서 멀티코어 활용
# CPU 코어 수 만큼 워커 생성
workers = multiprocessing.cpu_count()

host = "0.0.0.0"
port = 8000

log_level = "info"

timeout_keep_alive = 60