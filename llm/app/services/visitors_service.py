import json
import re
from typing import Dict, Any, List, Optional
from openai import OpenAI
from app.core.config import ChatBotConfig
from app.core.config import get_settings
from app.core.redis_manager import get_redis_manager
from app.models.redis_schemas import DataRequest
import logging
from datetime import datetime

# 로깅 설정
logger = logging.getLogger(__name__)

class APIException(Exception):
    """API 호출 관련 예외"""
    def __init__(self, message: str, status_code: int = None, api_name: str = None):
        self.message = message
        self.status_code = status_code
        self.api_name = api_name
        super().__init__(self.message)

class VisitorsChatBotService:
    def __init__(self):
        self.config = ChatBotConfig("visitors_bot")
        self.settings = get_settings()
        self.client = OpenAI(api_key=self.settings.openai_api_key)
        self.redis_manager = None

    async def generate_response(self, user_question: str, shop_id: int = None) -> str:
        """자연어 기반 응답 생성"""
        try:
            logger.info(f"🤖 자연어 처리 시작 - 질문: '{user_question}', Shop ID: {shop_id}")

            # 1단계: AI로 의도 분석 및 필요 데이터 판단
            analysis = self._analyze_intent_and_data_needs(user_question)

            # 2단계: 필요한 데이터 수집 계획 수립
            data_plan = self._create_data_collection_plan(analysis, shop_id)

            # 3단계: Redis Stream을 통한 데이터 수집
            collected_data = await self._collect_required_data(data_plan, shop_id)

            # 4단계: 수집된 데이터로 자연스러운 답변 생성
            return self._generate_natural_response(user_question, analysis, collected_data)

        except Exception as e:
            logger.error(f"❌ 응답 생성 오류: {e}")
            return self.config.get("error_responses", {}).get("general_error", "죄송합니다. 일시적인 오류가 발생했어요. 다시 시도해주세요 🙏")

    def _analyze_intent_and_data_needs(self, user_question: str) -> Dict[str, Any]:
        """AI로 사용자 의도 및 필요 데이터 분석"""

        system_prompt = self.config.get("intent_analysis_prompt")

        response = self.client.chat.completions.create(
            model=self.config.get("model", "gpt-3.5-turbo"),
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_question}
            ],
            temperature=self.config.get("temperature", 0.2),
            max_tokens=self.config.get("max_tokens", 500)
        )

        content = response.choices[0].message.content.strip()
        return self._parse_analysis_result(content)

    def _create_data_collection_plan(self, analysis: Dict[str, Any], shop_id: int) -> Dict[str, Any]:
        """필요한 데이터 수집 계획 수립"""

        plan = {
            "primary_intent": analysis.get("intent"),
            "required_apis": [],
            "parameters": analysis.get("parameters", {}),
            "fallback_strategy": "general_response"
        }

        intent = analysis.get("intent")

        if intent == "customer_inquiry":
            if analysis.get("parameters", {}).get("customer_name"):
                plan["required_apis"].extend([
                    "customer_search"
                ])
                # customer_search 성공하면 나머지 API 호출은 동적으로 결정

        elif intent == "reservation_briefing":
            plan["required_apis"].extend([
                "today_reservations"
            ])

        elif intent == "memo_update":
            plan["required_apis"].extend([
                "customer_search",
                "memo_update"
            ])

        elif intent == "out_of_scope":
            # 범위 외 쿼리는 API 호출 없이 바로 응답 처리
            plan["required_apis"] = []
            plan["fallback_strategy"] = "out_of_scope_response"

        return plan

    async def _collect_required_data(self, plan: Dict[str, Any], shop_id: int) -> Dict[str, Any]:
        """Redis Stream을 통한 데이터 수집"""

        collected_data = {
            "success": [],
            "failed": [],
            "errors": []
        }

        # Redis 매니저 초기화
        if not self.redis_manager:
            self.redis_manager = await get_redis_manager()

        # 고객 관련 요청의 경우 2단계 프로세스 처리
        if plan["primary_intent"] in ["customer_inquiry", "memo_update"]:
            return await self._collect_customer_data_with_lookup(plan, shop_id)
        
        # 일반적인 데이터 요청 (예약 브리핑 등)
        for api_name in plan["required_apis"]:
            try:
                # Redis Stream으로 데이터 요청
                data = await self._request_data_via_redis(api_name, plan["parameters"], shop_id)
                
                if data and data.get("status") == "success":
                    collected_data["success"].append({
                        "api": api_name,
                        "data": data.get("data", {})
                    })
                else:
                    collected_data["failed"].append({
                        "api": api_name,
                        "error": data.get("error", "데이터 요청 실패") if data else "Redis 응답 없음"
                    })

            except Exception as e:
                logger.error(f"❌ Redis 데이터 요청 실패 - {api_name}: {e}")
                collected_data["errors"].append({
                    "api": api_name,
                    "error": str(e)
                })

        return collected_data

    async def _collect_customer_data_with_lookup(self, plan: Dict[str, Any], shop_id: int) -> Dict[str, Any]:
        """고객 관련 데이터 수집 - 2단계 프로세스 (검색 → 상세정보)"""
        
        collected_data = {
            "success": [],
            "failed": [],
            "errors": []
        }
        
        customer_name = plan["parameters"].get("customer_name", "")
        if not customer_name:
            collected_data["errors"].append({
                "api": "customer_search",
                "error": "고객명이 제공되지 않았습니다"
            })
            return collected_data
        
        try:
            # 1단계: 고객 검색으로 client_code 찾기
            logger.info(f"🔍 1단계: 고객 검색 - '{customer_name}'")
            search_result = await self._request_data_via_redis("customer_search", plan["parameters"], shop_id)
            
            if not search_result or search_result.get("status") != "success":
                collected_data["failed"].append({
                    "api": "customer_search",
                    "error": search_result.get("error", "고객 검색 실패") if search_result else "Redis 응답 없음"
                })
                return collected_data
                
            search_data = search_result.get("data", {})
            
            # 검색 결과 검증
            if not search_data or self._is_customer_not_found(search_data):
                collected_data["failed"].append({
                    "api": "customer_search", 
                    "error": f"'{customer_name}' 고객을 찾을 수 없습니다"
                })
                return collected_data
            
            # 검색 결과 저장
            collected_data["success"].append({
                "api": "customer_search",
                "data": search_data
            })
            
            # 다중 고객 처리 또는 단일 고객 선택
            selected_customer, multiple_customers = self._handle_multiple_customers(search_data, customer_name)
            
            if multiple_customers:
                # 여러 고객이 있는 경우 - AI가 선택하도록 메시지 생성
                logger.info(f"🔍 동일한 이름의 고객 {len(search_data)}명 발견")
                collected_data["success"].append({
                    "api": "customer_search_multiple",
                    "data": {
                        "message": f"'{customer_name}' 고객이 {len(search_data)}명 발견되었습니다",
                        "customers": search_data,
                        "count": len(search_data)
                    }
                })
                return collected_data
            
            if not selected_customer:
                collected_data["errors"].append({
                    "api": "customer_search",
                    "error": "고객 검색 결과에서 적합한 고객을 찾을 수 없습니다"
                })
                return collected_data
            
            client_code = selected_customer.get("clientCode")
            logger.info(f"✅ 고객 선택: client_code={client_code}, 이름={selected_customer.get('userName')}")
            
            # 2단계: 의도에 따른 추가 정보 수집
            intent = plan["primary_intent"]
            
            if intent == "customer_inquiry":
                # 고객 상세 정보 및 방문 이력 조회
                try:
                    logger.info(f"🔍 2단계: 고객 상세정보 조회")
                    detail_params = {"client_code": client_code}
                    detail_result = await self._request_data_via_redis("customer_detail", detail_params, shop_id)
                    
                    if detail_result and detail_result.get("status") == "success":
                        detail_data = detail_result.get("data", {})
                        collected_data["success"].append({
                            "api": "customer_detail",
                            "data": detail_data
                        })
                        
                        # 방문 이력도 조회 (선택적)
                        try:
                            logger.info(f"🔍 3단계: 방문 이력 조회")
                            history_result = await self._request_data_via_redis("visit_history", detail_params, shop_id)
                            if history_result and history_result.get("status") == "success":
                                collected_data["success"].append({
                                    "api": "visit_history", 
                                    "data": history_result.get("data", {})
                                })
                        except Exception as e:
                            logger.warning(f"방문 이력 조회 실패: {e}")
                            # 방문 이력 실패는 치명적이지 않음
                    else:
                        collected_data["failed"].append({
                            "api": "customer_detail",
                            "error": detail_result.get("error", "고객 상세정보 조회 실패") if detail_result else "Redis 응답 없음"
                        })
                        
                except Exception as e:
                    collected_data["failed"].append({
                        "api": "customer_detail",
                        "error": str(e)
                    })
                    
            elif intent == "memo_update":
                # 메모 업데이트 실행
                try:
                    logger.info(f"🔍 2단계: 메모 업데이트")
                    memo_params = {
                        "client_code": client_code,
                        "memo_content": plan["parameters"].get("memo_content", "")
                    }
                    memo_result = await self._request_data_via_redis("memo_update", memo_params, shop_id)
                    
                    if memo_result and memo_result.get("status") == "success":
                        collected_data["success"].append({
                            "api": "memo_update",
                            "data": memo_result.get("data", {})
                        })
                    else:
                        collected_data["failed"].append({
                            "api": "memo_update",
                            "error": memo_result.get("error", "메모 업데이트 실패") if memo_result else "Redis 응답 없음"
                        })
                        
                except Exception as e:
                    collected_data["failed"].append({
                        "api": "memo_update",
                        "error": str(e)
                    })
                    
        except Exception as e:
            logger.error(f"❌ 고객 데이터 수집 중 오류: {e}")
            collected_data["errors"].append({
                "api": "customer_search",
                "error": str(e)
            })
        except Exception as e:
            collected_data["errors"].append({
                "api": "customer_search", 
                "error": str(e)
            })
            
        return collected_data
    
    def _extract_client_code_from_search(self, search_data: Dict[str, Any]) -> Optional[str]:
        """고객 검색 결과에서 client_code 추출"""
        try:
            # 리스트 형태인 경우 (복수 고객)
            if isinstance(search_data, list) and len(search_data) > 0:
                first_customer = search_data[0]
                return first_customer.get("clientCode") or first_customer.get("client_code") or first_customer.get("id")
            
            # 딕셔너리 형태인 경우 (단일 고객)
            elif isinstance(search_data, dict):
                return search_data.get("clientCode") or search_data.get("client_code") or search_data.get("id")
                
            return None
            
        except Exception as e:
            logger.error(f"client_code 추출 중 오류: {e}")
            return None

    def _handle_multiple_customers(self, search_data: List[Dict], customer_name: str) -> tuple[Optional[Dict], bool]:
        """다중 고객 처리 로직
        
        Returns:
            tuple: (selected_customer, is_multiple)
            - selected_customer: 선택된 고객 정보 (단일 고객인 경우)
            - is_multiple: 여러 고객이 있는지 여부
        """
        try:
            if not isinstance(search_data, list):
                return search_data, False
                
            if len(search_data) == 0:
                return None, False
                
            elif len(search_data) == 1:
                # 단일 고객인 경우
                return search_data[0], False
                
            else:
                # 다중 고객인 경우 - 일단 여러 명이 있다고 표시
                # 향후 생년월일 등 추가 정보로 필터링 로직을 추가할 수 있음
                return None, True
                
        except Exception as e:
            logger.error(f"다중 고객 처리 중 오류: {e}")
            return None, False

    def _generate_natural_response(self, user_question: str, analysis: Dict[str, Any],
                                   collected_data: Dict[str, Any]) -> str:
        """수집된 데이터를 기반으로 자연스러운 응답 생성"""
        
        # 범위 외 쿼리 처리
        if analysis.get("intent") == "out_of_scope":
            return self._handle_out_of_scope_response(user_question, analysis)

        # 데이터 수집 실패 시 대안 제시
        if not collected_data["success"] and (collected_data["failed"] or collected_data["errors"]):
            return self._handle_data_collection_failure(user_question)

        # 성공적으로 수집된 데이터로 응답 생성
        return self._compose_data_driven_response(user_question, analysis, collected_data["success"])


    def _compose_data_driven_response(self, user_question: str, analysis: Dict[str, Any],
                                      success_data: List[Dict]) -> str:
        """수집된 데이터를 활용한 자연스러운 응답 구성"""

        # 데이터 유효성 검사
        if not success_data:
            return self._handle_no_data_found(user_question, analysis)
        
        # 고객 검색 실패 케이스 특별 처리
        intent = analysis.get("intent")
        if intent in ["customer_inquiry", "memo_update"]:
            customer_data = self._extract_customer_data(success_data)
            if not customer_data or self._is_customer_not_found(customer_data):
                customer_name = analysis.get("parameters", {}).get("customer_name", "해당 고객")
                customer_error = self.config.get("error_responses", {}).get("not_found", {}).get("customer", "'{customer_name}' 고객을 찾을 수 없어요. 이름을 다시 확인해주시거나 신규 고객일 수 있어요! 🔍")
                return customer_error.format(customer_name=customer_name)

        # 실제 데이터가 있는 경우에만 AI 응답 생성
        context_prompt = f"""
CRITICAL: 제공된 실제 데이터만을 사용하여 응답하세요. 데이터에 없는 정보는 절대 만들어내지 마세요.

사용자 질문: {user_question}
분석 결과: {analysis}
실제 수집된 데이터: {success_data}

**중요 규칙:**
1. 위 데이터에 실제로 존재하는 정보만 사용하세요
2. 고객명, 서비스 내역, 날짜 등을 임의로 생성하지 마세요
3. 데이터가 비어있거나 null이면 "정보가 없습니다"라고 명시하세요
4. 이모지를 적절히 사용하되 정확한 정보 전달이 우선입니다

위 실제 데이터를 바탕으로 뷰티샵 직원에게 도움이 되는 응답을 작성해주세요.
"""

        response = self.client.chat.completions.create(
            model=self.config.get("model", "gpt-3.5-turbo"),
            messages=[
                {"role": "system", "content": self.config.get("response_generation_prompt")},
                {"role": "user", "content": context_prompt}
            ],
            temperature=self.config.get("temperature", 0.2),
            max_tokens=self.config.get("max_tokens", 800)
        )

        return response.choices[0].message.content.strip()

    def _handle_no_data_found(self, user_question: str, analysis: Dict[str, Any]) -> str:
        """데이터가 없을 때 적절한 응답"""
        intent = analysis.get("intent")
        error_responses = self.config.get("error_responses", {})
        
        if intent == "customer_inquiry":
            customer_name = analysis.get("parameters", {}).get("customer_name", "해당 고객")
            customer_error = error_responses.get("not_found", {}).get("customer", "'{customer_name}' 고객을 찾을 수 없어요. 이름을 다시 확인해주시거나 신규 고객일 수 있어요! 🔍")
            return customer_error.format(customer_name=customer_name)
        
        elif intent == "reservation_briefing":
            return error_responses.get("not_found", {}).get("reservation", "오늘 예약된 고객이 없어서 여유로운 하루네요! ☕️")
        
        else:
            return error_responses.get("general_error", "요청하신 정보를 찾을 수 없어요. 다시 확인해주세요! 🤔")

    def _extract_customer_data(self, success_data: List[Dict]) -> Dict[str, Any]:
        """성공 데이터에서 고객 정보 추출"""
        for data_item in success_data:
            if data_item.get("api") in ["customer_search", "customer_detail"]:
                return data_item.get("data", {})
        return {}

    def _is_customer_not_found(self, customer_data: Dict[str, Any]) -> bool:
        """고객 데이터가 비어있거나 찾을 수 없는 상태인지 확인"""
        if not customer_data:
            return True
        
        # 빈 리스트이거나 None인 경우
        if customer_data == [] or customer_data is None:
            return True
            
        # 리스트인데 비어있는 경우 (customer_search 결과)
        if isinstance(customer_data, list) and len(customer_data) == 0:
            return True
            
        # 딕셔너리인데 핵심 정보가 없는 경우
        if isinstance(customer_data, dict):
            if not customer_data.get("name") and not customer_data.get("customerName") and not customer_data.get("clientCode"):
                return True
                
        return False

    def _parse_analysis_result(self, content: str) -> Dict[str, Any]:
        """AI 분석 결과 JSON 파싱"""
        
        try:
            # JSON 추출 시도
            content = content.strip()
            
            # 1. 전체가 JSON인지 확인
            if content.startswith("{") and content.endswith("}"):
                return json.loads(content)
            
            # 2. ```json 블록에서 추출
            json_match = re.search(r'```json\s*\n(.*?)\n```', content, re.DOTALL)
            if json_match:
                return json.loads(json_match.group(1).strip())
            
            # 3. 첫 번째 { 부터 마지막 } 까지 추출
            start_idx = content.find("{")
            end_idx = content.rfind("}") + 1
            if start_idx != -1 and end_idx > start_idx:
                json_str = content[start_idx:end_idx]
                return json.loads(json_str)
            
            # 4. JSON 파싱 실패시 기본값 반환
            logger.warning(f"JSON 파싱 실패, 기본값 반환: {content}")
            return self._create_fallback_analysis(content)
            
        except json.JSONDecodeError as e:
            logger.error(f"JSON 파싱 오류: {e}, content: {content}")
            return self._create_fallback_analysis(content)
        except Exception as e:
            logger.error(f"분석 결과 파싱 중 예상치 못한 오류: {e}")
            return self._create_fallback_analysis(content)
    
    def _create_fallback_analysis(self, content: str) -> Dict[str, Any]:
        """AI 파싱 실패시 규칙 기반 백업 분석"""
        content_lower = content.lower()
        
        # 기본 분석 결과
        fallback = {
            "intent": "general",
            "confidence": 0.3,
            "parameters": {},
            "required_apis": [],
            "reasoning": "AI 분석 실패로 인한 규칙 기반 분류"
        }
        
        # 간단한 키워드 기반 분류
        if any(keyword in content_lower for keyword in ["예약", "브리핑", "오늘", "스케줄"]):
            fallback["intent"] = "reservation_briefing"
            fallback["required_apis"] = ["today_reservations"]
        elif any(keyword in content_lower for keyword in ["님", "고객", "정보"]):
            fallback["intent"] = "customer_inquiry"
            fallback["required_apis"] = ["customer_search"]
        elif any(keyword in content_lower for keyword in ["메모", "기록", "했어", "받았어"]):
            fallback["intent"] = "memo_update"
            fallback["required_apis"] = ["customer_search", "memo_update"]
        
        return fallback
    
    async def _request_data_via_redis(self, request_type: str, parameters: Dict[str, Any], shop_id: int) -> Optional[Dict[str, Any]]:
        """Redis Stream을 통한 데이터 요청"""
        try:
            # 데이터 요청 객체 생성
            data_request = DataRequest(
                request_type=request_type,
                shop_id=shop_id,
                parameters=parameters,
                timestamp=datetime.now()
            )
            
            logger.info(f"📤 Redis 데이터 요청: {request_type} - Shop: {shop_id}")
            
            # Redis Stream에 요청 발행
            correlation_id = await self.redis_manager.publish_data_request(data_request.dict())
            
            # 결과 대기 (최대 30초)
            result = await self.redis_manager.wait_for_data_result(correlation_id, timeout=30)
            
            if result:
                logger.info(f"✅ Redis 데이터 수신: {request_type} - Status: {result.get('status')}")
                return result
            else:
                logger.warning(f"⏰ Redis 데이터 요청 타임아웃: {request_type}")
                return None
                
        except Exception as e:
            logger.error(f"❌ Redis 데이터 요청 실패 - {request_type}: {e}")
            return None
    
    def _handle_out_of_scope_response(self, user_question: str, analysis: Dict[str, Any]) -> str:
        """범위 외 쿼리에 대한 적절한 응답 생성"""
        logger.info(f"🚫 범위 외 쿼리 감지: {user_question}")
        
        question_lower = user_question.lower()
        error_responses = self.config.get("error_responses", {}).get("out_of_scope", {})
        
        # 미래 날짜 관련 쿼리
        future_keywords = ["내일", "tomorrow", "다음", "next"]
        if any(keyword in question_lower for keyword in future_keywords):
            return error_responses.get("future_date", error_responses.get("general", ""))
            
        # 결제/매출 관련
        payment_keywords = ["결제", "payment", "매출", "revenue", "돈", "money"]
        if any(keyword in question_lower for keyword in payment_keywords):
            return error_responses.get("payment_revenue", error_responses.get("general", ""))
            
        # 직원/근무 관련
        staff_keywords = ["직원", "staff", "근무", "출퇴근", "employee"]
        if any(keyword in question_lower for keyword in staff_keywords):
            return error_responses.get("staff_management", error_responses.get("general", ""))
            
        # 일반적인 범위 외 응답
        return error_responses.get("general", "죄송해요, 그 부분은 제가 도와드릴 수 없는 영역이에요.")

    def _handle_data_collection_failure(self, user_question: str) -> str:
        """데이터 수집 실패시 자연스러운 대안 응답"""
        
        # 사용자 질문 유형에 따른 맞춤형 대안 제시
        question_lower = user_question.lower()
        
        # 고객 정보 관련 질문인 경우
        if any(keyword in question_lower for keyword in ["고객", "님", "정보", "누구"]):
            return """
죄송해요, 현재 고객 정보 시스템에 접근할 수 없어서 정확한 정보를 가져올 수 없어요 😅

**대신 이렇게 도와드릴 수 있어요:**
• 고객명을 정확히 알려주시면 나중에 다시 조회해드릴게요
• 직접 고객 관리 시스템을 확인해보세요
• 네트워크 연결 상태를 점검해주세요

다시 시도해주시거나 시스템 관리자에게 문의해주세요! 🛠️
            """.strip()
        
        # 예약 관련 질문인 경우
        elif any(keyword in question_lower for keyword in ["예약", "브리핑", "오늘", "스케줄"]):
            return """
앗, 예약 시스템에서 데이터를 가져오는데 문제가 있어요 😔

**확인해볼 수 있는 방법들:**
• 예약 관리 시스템에 직접 접속해보세요
• 인터넷 연결 상태를 확인해주세요
• 잠시 후 다시 "오늘 예약 브리핑해줘"라고 물어보세요

불편을 드려서 죄송해요. 곧 해결될 거예요! 💪
            """.strip()
        
        # 메모 관련 질문인 경우
        elif any(keyword in question_lower for keyword in ["메모", "기록", "저장"]):
            return """
메모 저장 시스템에 일시적인 문제가 있어요 📝

**임시 해결 방법:**
• 메모 내용을 별도로 기록해두세요
• 시스템 복구 후 다시 입력해드릴게요
• 중요한 내용이라면 종이나 메모장에 적어두세요

시스템이 복구되면 바로 알려드릴게요! 🔧
            """.strip()
        
        # 일반적인 경우
        else:
            return """
죄송해요, 현재 시스템에서 요청하신 정보를 가져올 수 없어요 😓

**해결 방법:**
• 잠시 후 다시 시도해주세요
• 네트워크 연결을 확인해주세요
• 다른 방식으로 질문해보세요
• 시스템 관리자에게 문의해주세요

더 도움이 필요하시면 언제든 말씀해주세요! 🙂
            """.strip()
