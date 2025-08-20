import multiprocessing

workers = multiprocessing.cpu_count() * 2 + 1

worker_class = "uvicorn.workers.UvicornWorker"

wsgi_app = "app.main:app"

bind = "0.0.0.0:8000"

loglevel = "info"

max_requests = 500