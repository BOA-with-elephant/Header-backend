import json
import re
import requests
from typing import Dict, Any, List, Optional
from openai import OpenAI
from app.core.config import ChatBotConfig
from app.core.config import get_settings
import logging

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

        # 사용 가능한 API 엔드포인트 매핑
        self.available_apis = {
            "customer_search": "/api/v1/my-shops/{shop_id}/customers/search",
            "customer_detail": "/api/v1/my-shops/{shop_id}/customers/{client_code}",
            "today_reservations": "/api/v1/my-shops/{shop_id}/customers/today-reservations",
            "memo_update": "/api/v1/my-shops/{shop_id}/customers/{client_code}",
            "visit_history": "/api/v1/my-shops/{shop_id}/customers/{client_code}/history",
        }

    def generate_response(self, user_question: str, shop_id: int = None) -> str:
        """자연어 기반 응답 생성"""
        try:
            logger.info(f"🤖 자연어 처리 시작 - 질문: '{user_question}', Shop ID: {shop_id}")

            # 1단계: AI로 의도 분석 및 필요 데이터 판단
            analysis = self._analyze_intent_and_data_needs(user_question)

            # 2단계: 필요한 데이터 수집 계획 수립
            data_plan = self._create_data_collection_plan(analysis, shop_id)

            # 3단계: API 호출을 통한 데이터 수집
            collected_data = self._collect_required_data(data_plan, shop_id)

            # 4단계: 수집된 데이터로 자연스러운 답변 생성
            return self._generate_natural_response(user_question, analysis, collected_data)

        except Exception as e:
            logger.error(f"❌ 응답 생성 오류: {e}")
            return "죄송합니다. 일시적인 오류가 발생했어요. 다시 시도해주세요 🙏"

    def _analyze_intent_and_data_needs(self, user_question: str) -> Dict[str, Any]:
        """AI로 사용자 의도 및 필요 데이터 분석"""

        system_prompt = self.config.get("intent_analysis_prompt")

        response = self.client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_question}
            ],
            temperature=0.2,
            max_tokens=500
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

        return plan

    def _collect_required_data(self, plan: Dict[str, Any], shop_id: int) -> Dict[str, Any]:
        """계획에 따른 데이터 수집 (API 권한 처리 포함)"""

        collected_data = {
            "success": [],
            "failed": [],
            "errors": []
        }

        # 고객 관련 요청의 경우 2단계 프로세스 처리
        if plan["primary_intent"] in ["customer_inquiry", "memo_update"]:
            return self._collect_customer_data_with_lookup(plan, shop_id)
        
        # 일반적인 API 호출 (예약 브리핑 등)
        for api_name in plan["required_apis"]:
            try:
                if api_name not in self.available_apis:
                    collected_data["errors"].append({
                        "api": api_name,
                        "error": "API endpoint not available"
                    })
                    continue

                # API 호출 시도
                data = self._call_spring_api(api_name, plan["parameters"], shop_id)
                collected_data["success"].append({
                    "api": api_name,
                    "data": data
                })

            except APIException as e:
                collected_data["failed"].append({
                    "api": api_name,
                    "error": str(e)
                })

            except Exception as e:
                collected_data["errors"].append({
                    "api": api_name,
                    "error": str(e)
                })

        return collected_data

    def _collect_customer_data_with_lookup(self, plan: Dict[str, Any], shop_id: int) -> Dict[str, Any]:
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
            search_data = self._call_spring_api("customer_search", plan["parameters"], shop_id)
            
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
                    detail_data = self._call_spring_api("customer_detail", detail_params, shop_id)
                    
                    collected_data["success"].append({
                        "api": "customer_detail",
                        "data": detail_data
                    })
                    
                    # 방문 이력도 조회 (선택적)
                    try:
                        logger.info(f"🔍 3단계: 방문 이력 조회")
                        history_data = self._call_spring_api("visit_history", detail_params, shop_id)
                        collected_data["success"].append({
                            "api": "visit_history", 
                            "data": history_data
                        })
                    except APIException as e:
                        logger.warning(f"방문 이력 조회 실패: {e}")
                        # 방문 이력 실패는 치명적이지 않음
                        
                except APIException as e:
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
                    memo_data = self._call_spring_api("memo_update", memo_params, shop_id)
                    
                    collected_data["success"].append({
                        "api": "memo_update",
                        "data": memo_data
                    })
                    
                except APIException as e:
                    collected_data["failed"].append({
                        "api": "memo_update",
                        "error": str(e)
                    })
                    
        except APIException as e:
            collected_data["failed"].append({
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
                return f"'{customer_name}' 고객을 찾을 수 없어요. 이름을 다시 확인해주시거나 신규 고객일 수 있어요! 🔍"

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

위 실제 데이터를 바탕으로 헤어샵 직원에게 도움이 되는 응답을 작성해주세요.
"""

        response = self.client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": self.config.get("response_generation_prompt")},
                {"role": "user", "content": context_prompt}
            ],
            temperature=0.2,  # 창의성 낮춤 - 정확성 우선
            max_tokens=800
        )

        return response.choices[0].message.content.strip()

    def _handle_no_data_found(self, user_question: str, analysis: Dict[str, Any]) -> str:
        """데이터가 없을 때 적절한 응답"""
        intent = analysis.get("intent")
        
        if intent == "customer_inquiry":
            customer_name = analysis.get("parameters", {}).get("customer_name", "해당 고객")
            return f"'{customer_name}' 고객을 찾을 수 없어요. 이름을 다시 확인해주시거나 신규 고객일 수 있어요! 🔍"
        
        elif intent == "reservation_briefing":
            return "오늘 예약된 고객이 없어서 여유로운 하루네요! ☕️"
        
        else:
            return "요청하신 정보를 찾을 수 없어요. 다시 확인해주세요! 🤔"

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
    
    def _call_spring_api(self, api_name: str, parameters: Dict[str, Any], shop_id: int) -> Dict[str, Any]:
        """Spring Boot API 호출"""
        
        try:
            # API URL 구성
            base_url = self.config.get("spring_api", {}).get("base_url", "http://localhost:8080")
            endpoint_template = self.available_apis.get(api_name)
            
            if not endpoint_template:
                raise APIException(f"알 수 없는 API: {api_name}", None, api_name)
            
            # URL 파라미터 처리
            if api_name == "customer_detail" or api_name == "memo_update" or api_name == "visit_history":
                client_code = parameters.get("client_code")
                if not client_code:
                    raise APIException("client_code가 필요합니다", None, api_name)
                url = base_url + endpoint_template.format(shop_id=shop_id, client_code=client_code)
            else:
                url = base_url + endpoint_template.format(shop_id=shop_id)
            
            logger.info(f"📡 API 호출: {api_name} -> {url}")
            
            # HTTP 메서드 및 파라미터 처리
            if api_name == "memo_update":
                # 메모 업데이트는 PATCH 요청
                memo_content = parameters.get("memo_content", "")
                response = requests.patch(url, 
                                        params={"memo": memo_content}, 
                                        timeout=10)
            elif api_name == "customer_search":
                # 고객 검색은 GET 요청에 이름 파라미터 포함
                customer_name = parameters.get("customer_name", "")
                params = {"name": customer_name} if customer_name else {}
                response = requests.get(url, params=params, timeout=10)
            else:
                # 나머지는 GET 요청
                response = requests.get(url, timeout=10)
            
            # 응답 처리
            if response.status_code == 200:
                try:
                    data = response.json()
                    logger.info(f"✅ API 호출 성공: {api_name}")
                    return data.get("data", data)  # Spring Boot 응답 구조에 따라 조정
                except json.JSONDecodeError:
                    # JSON이 아닌 경우 (예: 메모 업데이트 성공 응답)
                    return {"success": True, "message": "성공"}
            
            elif response.status_code == 403:
                raise APIException("접근 권한이 없습니다", 403, api_name)
            elif response.status_code == 404:
                raise APIException("요청한 데이터를 찾을 수 없습니다", 404, api_name)
            elif response.status_code == 500:
                raise APIException("서버 내부 오류가 발생했습니다", 500, api_name)
            else:
                raise APIException(f"API 호출 실패: HTTP {response.status_code}", response.status_code, api_name)
                
        except requests.exceptions.Timeout:
            raise APIException("서버 응답 시간이 초과되었습니다", None, api_name)
        except requests.exceptions.ConnectionError:
            raise APIException("서버에 연결할 수 없습니다", None, api_name)
        except requests.exceptions.RequestException as e:
            raise APIException(f"네트워크 오류: {str(e)}", None, api_name)
        except Exception as e:
            logger.error(f"API 호출 중 예상치 못한 오류: {e}")
            raise APIException(f"API 호출 중 오류 발생: {str(e)}", None, api_name)
    
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
