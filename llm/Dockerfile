FROM python:3.12 AS python-base

WORKDIR /app

COPY . /app

RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r llm/requirements.txt

EXPOSE 8000

CMD ["gunicorn", "-c", "llm/gunicorn.config.py"]
