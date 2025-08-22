import asyncio
import json
import uuid
from typing import Dict, Any, Optional, List
import redis
import logging
from app.core.config import get_settings

logger = logging.getLogger(__name__)

class RedisStreamManager:
    def __init__(self):
        self.settings = get_settings()
        self.redis_client = None
        self.consumer_tasks = {}

    async def connect(self):
        """Redis 연결 초기화"""
        try:
            self.redis_client = redis.from_url(
                self.settings.redis_url,
                decode_responses=True,
                socket_connect_timeout=5,
                socket_timeout=5
            )
            # 연결 테스트
            self.redis_client.ping()
            logger.info("✅ Redis 연결 성공")
            
            # 필요한 스트림과 컨슈머 그룹 생성
            await self._initialize_streams()
            
        except Exception as e:
            logger.error(f"❌ Redis 연결 실패: {e}")
            raise

    async def _initialize_streams(self):
        """스트림과 컨슈머 그룹 초기화"""
        try:
            # 스트림이 존재하지 않으면 생성 (더미 메시지로)
            streams = [
                self.settings.redis_stream_data_requests,
                self.settings.redis_stream_data_results
            ]
            
            for stream_name in streams:
                try:
                    # 스트림 존재 확인
                    self.redis_client.xinfo_stream(stream_name)
                except redis.ResponseError:
                    # 스트림이 없으면 더미 메시지로 생성
                    self.redis_client.xadd(stream_name, {"init": "stream_created"})
                    logger.info(f"📝 스트림 생성: {stream_name}")

            # 컨슈머 그룹 생성 (data-results 스트림용)
            try:
                self.redis_client.xgroup_create(
                    self.settings.redis_stream_data_results,
                    self.settings.redis_consumer_group,
                    id="0",
                    mkstream=True
                )
                logger.info(f"👥 컨슈머 그룹 생성: {self.settings.redis_consumer_group}")
            except redis.ResponseError as e:
                if "BUSYGROUP" in str(e):
                    logger.info(f"👥 컨슈머 그룹 이미 존재: {self.settings.redis_consumer_group}")
                else:
                    logger.error(f"❌ 컨슈머 그룹 생성 실패: {e}")

        except Exception as e:
            logger.error(f"❌ 스트림 초기화 실패: {e}")

    async def disconnect(self):
        """Redis 연결 종료"""
        try:
            # 실행 중인 컨슈머 태스크 종료
            for task in self.consumer_tasks.values():
                task.cancel()
                
            if self.redis_client:
                self.redis_client.close()
                logger.info("✅ Redis 연결 종료")
        except Exception as e:
            logger.error(f"❌ Redis 연결 종료 중 오류: {e}")

    def publish_data_request(self, request_data: Dict[str, Any]) -> str:
        """데이터 요청을 Redis Stream에 발행"""
        try:
            correlation_id = str(uuid.uuid4())
            message = {
                "correlation_id": correlation_id,
                "request_type": request_data.get("request_type"),
                "shop_id": str(request_data.get("shop_id", "")),
                "parameters": json.dumps(request_data.get("parameters", {})),
                "timestamp": str(request_data.get("timestamp", ""))
            }
            
            message_id = self.redis_client.xadd(
                self.settings.redis_stream_data_requests,
                message
            )
            
            logger.info(f"📤 데이터 요청 발행: {correlation_id} -> {message_id}")
            return correlation_id
            
        except Exception as e:
            logger.error(f"❌ 데이터 요청 발행 실패: {e}")
            raise

    async def wait_for_data_result(self, correlation_id: str, timeout: int = 30) -> Optional[Dict[str, Any]]:
        """특정 correlation_id의 결과를 기다림"""
        try:
            start_time = asyncio.get_event_loop().time()
            
            while True:
                # 타임아웃 체크
                if asyncio.get_event_loop().time() - start_time > timeout:
                    logger.warning(f"⏰ 데이터 결과 대기 타임아웃: {correlation_id}")
                    return None

                # 스트림에서 메시지 읽기
                messages = self.redis_client.xreadgroup(
                    self.settings.redis_consumer_group,
                    self.settings.redis_consumer_name,
                    {self.settings.redis_stream_data_results: ">"},
                    count=10,
                    block=1000  # 1초 대기
                )

                for stream, stream_messages in messages:
                    for message_id, fields in stream_messages:
                        try:
                            if fields.get("correlation_id") == correlation_id:
                                # 메시지 확인 처리
                                self.redis_client.xack(
                                    self.settings.redis_stream_data_results,
                                    self.settings.redis_consumer_group,
                                    message_id
                                )
                                
                                # JSON 파싱
                                result = {
                                    "status": fields.get("status"),
                                    "data": json.loads(fields.get("data", "{}")),
                                    "error": fields.get("error"),
                                    "timestamp": fields.get("timestamp")
                                }
                                
                                logger.info(f"📥 데이터 결과 수신: {correlation_id}")
                                return result
                                
                        except Exception as parse_error:
                            logger.error(f"❌ 메시지 파싱 오류: {parse_error}")
                            # 파싱 실패한 메시지도 ACK 처리
                            self.redis_client.xack(
                                self.settings.redis_stream_data_results,
                                self.settings.redis_consumer_group,
                                message_id
                            )

                # 잠시 대기
                await asyncio.sleep(0.1)

        except Exception as e:
            logger.error(f"❌ 데이터 결과 대기 중 오류: {e}")
            return None

# 글로벌 Redis 매니저 인스턴스
redis_manager = RedisStreamManager()

async def get_redis_manager() -> RedisStreamManager:
    """Redis 매니저 의존성 주입"""
    if not redis_manager.redis_client:
        await redis_manager.connect()
    return redis_manager