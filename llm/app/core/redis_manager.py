import asyncio
import json
import uuid
from typing import Dict, Any, Optional, List
import redis.asyncio as redis
import logging
from app.core.config import get_settings

logger = logging.getLogger(__name__)

class RedisStreamManager:
    def __init__(self):
        self.settings = get_settings()
        self.redis_client = None
        self.consumer_tasks = {}
        self.consumer_name = f"python-consumer-{uuid.uuid4().hex[:8]}"

    async def connect(self):
        """Redis 연결 초기화"""
        try:
            self.redis_client = await redis.from_url(
                self.settings.redis_url,
                decode_responses=True,
                socket_connect_timeout=5,
                socket_timeout=5
            )
            # 연결 테스트
            await self.redis_client.ping()
            logger.info("✅ Redis 연결 성공")
            
            # 필요한 스트림과 컨슈머 그룹 생성
            await self._initialize_streams()
            
            # Pending 메시지 정리
            await self._cleanup_pending_messages()
            
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
                    await self.redis_client.xinfo_stream(stream_name)
                except redis.ResponseError:
                    # 스트림이 없으면 더미 메시지로 생성
                    await self.redis_client.xadd(stream_name, {"init": "stream_created"})
                    logger.info(f"📝 스트림 생성: {stream_name}")

            # 컨슈머 그룹 생성 (data-results 스트림용)
            await self._ensure_consumer_group(
                self.settings.redis_stream_data_results,
                self.settings.redis_consumer_group
            )

        except Exception as e:
            logger.error(f"❌ 스트림 초기화 실패: {e}")

    async def _ensure_consumer_group(self, stream_name: str, group_name: str):
        """컨슈머 그룹이 존재하는지 확인하고 없으면 생성"""
        try:
            # 기존 컨슈머 그룹 확인
            groups_info = await self.redis_client.xinfo_groups(stream_name)
            existing_groups = [group['name'] for group in groups_info]
            
            if group_name not in existing_groups:
                # 그룹이 없으면 생성
                await self.redis_client.xgroup_create(
                    stream_name,
                    group_name,
                    id="0",
                    mkstream=True
                )
                logger.info(f"👥 컨슈머 그룹 생성: {group_name} for {stream_name}")
            else:
                logger.info(f"👥 컨슈머 그룹 이미 존재: {group_name} for {stream_name}")
                
        except redis.ResponseError as e:
            if "BUSYGROUP" in str(e):
                logger.info(f"👥 컨슈머 그룹 이미 존재: {group_name}")
            else:
                logger.error(f"❌ 컨슈머 그룹 처리 실패: {e}")
        except Exception as e:
            logger.error(f"❌ 컨슈머 그룹 확인 중 오류: {e}")

    async def _cleanup_pending_messages(self):
        """누적된 pending 메시지들을 정리"""
        try:
            # 기존 consumer들의 pending 메시지 확인
            pending_info = await self.redis_client.xpending(
                self.settings.redis_stream_data_results,
                self.settings.redis_consumer_group
            )
            
            if pending_info and pending_info['pending'] > 0:
                logger.info(f"🧹 Pending 메시지 {pending_info['pending']}개 발견, 정리 시작")
                
                # 모든 consumer의 pending 메시지 조회
                pending_messages = await self.redis_client.xpending_range(
                    self.settings.redis_stream_data_results,
                    self.settings.redis_consumer_group,
                    min="-",
                    max="+",
                    count=100
                )
                
                # 오래된 pending 메시지들을 ACK 처리 (10분 이상 된 것들)
                current_time = asyncio.get_event_loop().time() * 1000  # milliseconds
                acked_count = 0
                
                for msg_info in pending_messages:
                    message_id, consumer, idle_time = msg_info[:3]
                    
                    # 10분(600000ms) 이상 idle 상태인 메시지는 ACK 처리
                    if idle_time > 600000:
                        await self.redis_client.xack(
                            self.settings.redis_stream_data_results,
                            self.settings.redis_consumer_group,
                            message_id
                        )
                        acked_count += 1
                
                if acked_count > 0:
                    logger.info(f"🧹 오래된 pending 메시지 {acked_count}개 정리 완료")
                else:
                    logger.info("🧹 정리할 오래된 pending 메시지 없음")
                    
        except Exception as e:
            logger.warning(f"⚠️ Pending 메시지 정리 중 오류 (무시하고 계속): {e}")

    async def disconnect(self):
        """Redis 연결 종료"""
        try:
            # 실행 중인 컨슈머 태스크 종료
            for task in self.consumer_tasks.values():
                task.cancel()
                
            if self.redis_client:
                await self.redis_client.close()
                logger.info("✅ Redis 연결 종료")
        except Exception as e:
            logger.error(f"❌ Redis 연결 종료 중 오류: {e}")

    async def publish_data_request(self, request_data: Dict[str, Any]) -> str:
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
            
            message_id = await self.redis_client.xadd(
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
        unacked_messages = []  # ACK 되지 않은 메시지들 추적
        
        try:
            start_time = asyncio.get_event_loop().time()
            
            while True:
                # 타임아웃 체크
                if asyncio.get_event_loop().time() - start_time > timeout:
                    logger.warning(f"⏰ 데이터 결과 대기 타임아웃: {correlation_id}")
                    # 타임아웃 시 읽었던 메시지들을 ACK 처리하여 pending 방지
                    await self._ack_unprocessed_messages(unacked_messages)
                    return None

                try:
                    # 스트림에서 메시지 읽기
                    messages = await self.redis_client.xreadgroup(
                        self.settings.redis_consumer_group,
                        self.consumer_name,  # 고유한 consumer name 사용
                        {self.settings.redis_stream_data_results: ">"},
                        count=10,
                        block=1000  # 1초 대기
                    )

                    # 읽어온 메시지들을 unacked_messages에 추가
                    for stream, stream_messages in messages:
                        for message_id, fields in stream_messages:
                            unacked_messages.append((message_id, fields))

                    # 메시지 처리
                    for message_id, fields in [msg for stream, msgs in messages for msg in msgs]:
                        try:
                            if fields.get("correlation_id") == correlation_id:
                                # 원하는 메시지 발견 - ACK 처리
                                await self.redis_client.xack(
                                    self.settings.redis_stream_data_results,
                                    self.settings.redis_consumer_group,
                                    message_id
                                )
                                
                                # unacked_messages에서 제거
                                unacked_messages = [(mid, mfields) for mid, mfields in unacked_messages 
                                                  if mid != message_id]
                                
                                # JSON 파싱
                                result = {
                                    "status": fields.get("status"),
                                    "data": json.loads(fields.get("data", "{}")),
                                    "error": fields.get("error"),
                                    "timestamp": fields.get("timestamp")
                                }
                                
                                logger.info(f"📥 데이터 결과 수신: {correlation_id}")
                                
                                # 다른 unacked 메시지들도 ACK 처리
                                await self._ack_unprocessed_messages(unacked_messages)
                                
                                return result
                                
                        except Exception as parse_error:
                            logger.error(f"❌ 메시지 파싱 오류: {parse_error}")
                            # 파싱 실패한 메시지도 ACK 처리
                            await self.redis_client.xack(
                                self.settings.redis_stream_data_results,
                                self.settings.redis_consumer_group,
                                message_id
                            )
                            # unacked_messages에서 제거
                            unacked_messages = [(mid, mfields) for mid, mfields in unacked_messages 
                                              if mid != message_id]

                except redis.ResponseError as redis_error:
                    if "NOGROUP" in str(redis_error):
                        logger.warning("⚠️ Consumer 그룹이 없음, 재생성 시도")
                        await self._ensure_consumer_group(
                            self.settings.redis_stream_data_results,
                            self.settings.redis_consumer_group
                        )
                    else:
                        logger.error(f"❌ Redis 에러: {redis_error}")
                        
                except Exception as read_error:
                    logger.error(f"❌ 메시지 읽기 중 오류: {read_error}")

                # 잠시 대기
                await asyncio.sleep(0.1)

        except Exception as e:
            logger.error(f"❌ 데이터 결과 대기 중 심각한 오류: {e}")
            # 에러 발생 시에도 unacked 메시지들을 ACK 처리
            await self._ack_unprocessed_messages(unacked_messages)
            return None

    async def _ack_unprocessed_messages(self, unacked_messages: list):
        """처리되지 않은 메시지들을 ACK 처리하여 pending 상태 방지"""
        if not unacked_messages:
            return
            
        try:
            acked_count = 0
            for message_id, fields in unacked_messages:
                try:
                    await self.redis_client.xack(
                        self.settings.redis_stream_data_results,
                        self.settings.redis_consumer_group,
                        message_id
                    )
                    acked_count += 1
                except Exception as ack_error:
                    logger.warning(f"⚠️ 메시지 ACK 실패 {message_id}: {ack_error}")
                    
            if acked_count > 0:
                logger.info(f"🧹 미처리 메시지 {acked_count}개 ACK 처리 완료 (pending 방지)")
                
        except Exception as e:
            logger.warning(f"⚠️ 미처리 메시지 ACK 처리 중 오류: {e}")

# 글로벌 Redis 매니저 인스턴스
redis_manager = RedisStreamManager()

async def get_redis_manager() -> RedisStreamManager:
    """Redis 매니저 의존성 주입"""
    if not redis_manager.redis_client:
        await redis_manager.connect()
    return redis_manager