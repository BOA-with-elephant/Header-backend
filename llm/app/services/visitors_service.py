import json
import re
import requests
from typing import Dict, Any, Optional, List
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

    def generate_response(self, user_question: str, shop_id: int = None) -> str:
        """사용자 질문에 대한 응답 생성"""
        try:
            logger.info(f"🤖 응답 생성 시작 - 질문: '{user_question}', Shop ID: {shop_id}")

            # 1. 고신뢰도 패턴 매칭 (API 호출 포함)
            quick_result = self._enhanced_pattern_match(user_question, shop_id)
            if quick_result["confidence"] > 0.8:
                logger.info(f"⚡ 패턴 매칭 성공: {quick_result}")
                return quick_result["response"]

            # 2. AI 의도 분석 (패턴 매칭 실패 시에만)
            analysis = self._analyze_user_intent(user_question)

            # 3. 분석 결과에 따른 처리
            return self._process_analysis_result(analysis, shop_id)

        except Exception as e:
            logger.error(f"❌ 응답 생성 오류: {e}")
            return "죄송합니다. 일시적인 오류가 발생했어요. 다시 시도해주세요 🙏"

    def _enhanced_pattern_match(self, question: str, shop_id: int) -> Dict[str, Any]:
        """강화된 패턴 매칭 (API 호출 포함)"""
        question_lower = question.lower()

        # ================================
        # 1. 메모 업데이트 패턴 (API 호출 필요)
        # ================================
        memo_patterns = [
            r'([가-힣]{2,4})\s*님.*?(염색|펌|커트|시술|파마|컬러링|탈색).*?(했|받았|하셨|받으셨)',
            r'([가-힣]{2,4})\s*님.*?([가-힣]*색.*?염색)',
            r'([가-힣]{2,4})\s*님.*?(핑크|빨강|파랑|노랑|검정|갈색|금색|보라|초록).*?(염색|컬러)',
        ]

        for pattern in memo_patterns:
            match = re.search(pattern, question)
            if match:
                customer_name = match.group(1)
                memo_content = re.sub(rf'{customer_name}\s*님\s*', '', question).strip()

                # API 호출하여 실제 메모 업데이트 수행
                if shop_id:
                    try:
                        response_msg = self._handle_memo_update_direct(customer_name, memo_content, shop_id)
                        return {
                            "action": "memo_update",
                            "confidence": 0.95,
                            "response": response_msg
                        }
                    except Exception as e:
                        logger.error(f"메모 업데이트 실패: {e}")
                        return {
                            "action": "memo_update",
                            "confidence": 0.95,
                            "response": f"메모 저장 중 오류가 발생했어요: {str(e)}"
                        }

        # ================================
        # 2. 브리핑 패턴 (API 호출 필요)
        # ================================
        briefing_patterns = [
            r'오늘.*?(예약|손님|고객|브리핑)',
            r'(예약|스케줄).*?알려',
            r'브리핑.*?해'
        ]

        for pattern in briefing_patterns:
            if re.search(pattern, question):
                if shop_id:
                    try:
                        response_msg = self._handle_customer_briefing_direct(shop_id)
                        return {
                            "action": "briefing",
                            "confidence": 0.9,
                            "response": response_msg
                        }
                    except Exception as e:
                        logger.error(f"브리핑 조회 실패: {e}")
                        return {
                            "action": "briefing",
                            "confidence": 0.9,
                            "response": f"브리핑 조회 중 오류가 발생했어요: {str(e)}"
                        }

        # ================================
        # 3. VIP 패턴 (API 호출 필요)
        # ================================
        vip_patterns = [
            r'(vip|브이아이피|단골|우수고객)',
            r'특별.*?고객'
        ]

        for pattern in vip_patterns:
            if re.search(pattern, question_lower):
                if shop_id:
                    try:
                        response_msg = self._handle_vip_customers_direct(shop_id)
                        return {
                            "action": "vip_list",
                            "confidence": 0.9,
                            "response": response_msg
                        }
                    except Exception as e:
                        logger.error(f"VIP 조회 실패: {e}")
                        return {
                            "action": "vip_list",
                            "confidence": 0.9,
                            "response": f"VIP 고객 조회 중 오류가 발생했어요: {str(e)}"
                        }

        # ================================
        # 4. 고객 정보 조회 패턴 (API 호출 필요)
        # ================================
        customer_info_patterns = [
            r'([가-힣]{2,4})\s*님.*?(정보|알려|조회)',
            r'([가-힣]{2,4})\s*고객.*?(정보|알려|조회)'
        ]

        for pattern in customer_info_patterns:
            match = re.search(pattern, question)
            if match:
                customer_name = match.group(1)
                if shop_id:
                    try:
                        response_msg = self._handle_customer_info_direct(customer_name, shop_id)
                        return {
                            "action": "customer_info",
                            "confidence": 0.85,
                            "response": response_msg
                        }
                    except Exception as e:
                        logger.error(f"고객 정보 조회 실패: {e}")
                        return {
                            "action": "customer_info",
                            "confidence": 0.85,
                            "response": f"고객 정보 조회 중 오류가 발생했어요: {str(e)}"
                        }

        # ================================
        # 5. 일반 대화 (API 호출 불필요)
        # ================================
        greetings = ["안녕", "hello", "hi", "반가워"]
        if any(greeting in question_lower for greeting in greetings):
            return {
                "action": "general",
                "confidence": 0.9,
                "response": "안녕하세요! 헤어샵 고객관리 AI입니다. 무엇을 도와드릴까요? 😊"
            }

        # 패턴 매칭 실패
        return {"action": "general", "confidence": 0.0, "response": ""}

    # ================================
    # 직접 API 호출 메서드들
    # ================================

    def _handle_memo_update_direct(self, customer_name: str, memo_content: str, shop_id: int) -> str:
        """메모 업데이트 직접 처리"""
        try:
            # 1. 고객 검색
            customer = self._find_customer_by_name(shop_id, customer_name)
            if not customer:
                return f"'{customer_name}' 고객을 찾을 수 없어요. 정확한 이름을 확인해주세요 😅"

            # 2. 메모 업데이트
            success = self._update_customer_memo(shop_id, customer['clientCode'], memo_content)
            if success:
                return f"{customer_name}님의 시술 내용이 메모에 잘 저장되었어요! 다음 방문때 참고하겠습니다 ✨"
            else:
                return f"{customer_name}님의 메모 저장에 실패했어요. 다시 시도해주세요. 😅"

        except Exception as e:
            logger.error(f"메모 업데이트 직접 처리 오류: {e}")
            raise

    def _handle_customer_briefing_direct(self, shop_id: int) -> str:
        """브리핑 직접 처리"""
        try:
            # 1. 오늘 예약 정보 조회
            reservations = self._get_today_reservations(shop_id)
            if not reservations:
                return "오늘 예약된 고객이 없어서 여유로운 하루가 될 것 같아요! 😴✨"

            # 2. 고객별 상세 정보 수집
            customer_details = []
            for reservation in reservations:
                try:
                    detail = self._get_customer_detail(shop_id, reservation.get('clientCode'))
                    customer_details.append({**reservation, **detail})
                except Exception as e:
                    logger.warning(f"고객 상세정보 조회 실패: {reservation.get('clientCode')} - {e}")
                    customer_details.append(reservation)

            # 3. 브리핑 메시지 생성
            return self._generate_briefing_message(customer_details)

        except Exception as e:
            logger.error(f"브리핑 직접 처리 오류: {e}")
            raise

    def _handle_vip_customers_direct(self, shop_id: int) -> str:
        """VIP 고객 직접 처리"""
        try:
            vip_customers = self._get_vip_customers(shop_id)
            if not vip_customers:
                return "현재 등록된 VIP 고객이 없어요. 👑"
            return self._generate_vip_list_message(vip_customers)

        except Exception as e:
            logger.error(f"VIP 고객 직접 처리 오류: {e}")
            raise

    def _handle_customer_info_direct(self, customer_name: str, shop_id: int) -> str:
        """고객 정보 직접 처리"""
        try:
            # 1. 고객 검색
            customer = self._find_customer_by_name(shop_id, customer_name)
            if not customer:
                return f"'{customer_name}' 고객을 찾을 수 없어요. 정확한 이름을 확인해주세요 😅"

            # 2. 고객 상세 정보 조회
            try:
                detail = self._get_customer_detail(shop_id, customer['clientCode'])
            except Exception as e:
                logger.warning(f"고객 상세정보 조회 실패, 기본 정보만 제공: {e}")
                detail = {}

            # 3. 고객 정보 메시지 생성
            return self._generate_customer_info_message(customer, detail)

        except Exception as e:
            logger.error(f"고객 정보 직접 처리 오류: {e}")
            raise

    def _analyze_user_intent(self, user_question: str) -> Dict[str, Any]:
        """AI로 사용자 의도 분석 (개선된 버전)"""
        try:
            logger.info(f"🤖 AI 분석 시작...")
            system_prompt = self.config.get("system_prompt")

            response = self.client.chat.completions.create(
                model="gpt-3.5-turbo",
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_question}
                ],
                temperature=0.1,
                max_tokens=300
            )

            content = response.choices[0].message.content.strip()
            logger.info(f"🤖 AI 원본 응답: {content}")

            # JSON 추출
            json_content = self._extract_json_robust(content)
            if json_content:
                try:
                    parsed_result = json.loads(json_content)
                    logger.info(f"✅ JSON 파싱 성공: {parsed_result}")

                    if self._validate_analysis_result(parsed_result):
                        return parsed_result

                except json.JSONDecodeError as e:
                    logger.error(f"❌ JSON 파싱 실패: {e}")

            # 실패 시 백업 분석
            return self._fallback_analysis(user_question, content)

        except Exception as e:
            logger.error(f"❌ AI 분석 오류: {e}")
            return self._rule_based_analysis(user_question)

    def _extract_json_robust(self, content: str) -> Optional[str]:
        """강화된 JSON 추출"""
        content = content.strip()

        # 전체가 JSON인지 체크
        if content.startswith("{") and content.endswith("}"):
            return content

        # ```json 블록 찾기
        if "```json" in content:
            start = content.find("```json") + 7
            end = content.find("```", start)
            if end > start:
                return content[start:end].strip()

        # ``` 블록 찾기
        if "```" in content:
            start = content.find("```") + 3
            end = content.find("```", start)
            if end > start:
                extracted = content[start:end].strip()
                if extracted.startswith("{"):
                    return extracted

        # 첫 { 부터 마지막 } 추출
        start_idx = content.find("{")
        end_idx = content.rfind("}") + 1
        if start_idx != -1 and end_idx > start_idx:
            return content[start_idx:end_idx]

        return None

    def _validate_analysis_result(self, result: Dict[str, Any]) -> bool:
        """분석 결과 검증"""
        if "action" not in result:
            return False

        valid_actions = ["briefing", "memo_update", "customer_info", "vip_list", "general"]
        if result["action"] not in valid_actions:
            return False

        if "parameters" in result and not isinstance(result.get("parameters"), dict):
            return False

        return True

    def _process_analysis_result(self, analysis: Dict[str, Any], shop_id: int) -> str:
        """분석 결과 처리"""
        action = analysis["action"]
        params = analysis.get("parameters", {})

        if action == "memo_update":
            customer_name = params.get("customer_name")
            memo_content = params.get("memo_content")

            if customer_name and memo_content:
                logger.info(f"💾 메모 저장: {customer_name} -> {memo_content}")
                return f"{customer_name}님의 '{memo_content}' 메모가 성공적으로 저장되었어요! 다음 방문때 참고하겠습니다 ✨"
            else:
                return "고객명과 메모 내용을 확인할 수 없어요. 다시 말씀해주세요."

        elif action == "briefing":
            return "오늘 예약된 고객들을 확인해드릴게요! 📋"

        elif action == "vip_list":
            return "VIP 고객 목록을 확인해드릴게요! 👑"

        else:
            return "무엇을 도와드릴까요? 😊"

    def _fallback_analysis(self, user_question: str, ai_response: str) -> Dict[str, Any]:
        """AI JSON 파싱 실패 시 백업"""
        action = self._classify_by_keywords(user_question)
        parameters = self._extract_parameters(user_question, action)

        return {
            "action": action,
            "parameters": parameters,
            "response": ai_response if ai_response else self._get_default_response(action)
        }

    def _rule_based_analysis(self, user_question: str) -> Dict[str, Any]:
        """완전 백업: 규칙 기반 분석"""
        action = self._classify_by_keywords(user_question)
        parameters = self._extract_parameters(user_question, action)

        return {
            "action": action,
            "parameters": parameters,
            "response": self._get_default_response(action)
        }

    def _classify_by_keywords(self, query: str) -> str:
        """키워드 기반 액션 분류 (강화된 버전)"""
        query_lower = query.lower()
        keywords = self.config.get("keywords", {})

        # 메모 업데이트 체크 (강화된 패턴)
        memo_keywords = keywords.get("memo_update", [])
        has_customer_name = any(keyword in query_lower for keyword in ["님", "씨"])
        has_memo_keyword = any(keyword in query_lower for keyword in memo_keywords)

        if has_customer_name and has_memo_keyword:
            return "memo_update"

        # 브리핑 체크
        if any(keyword in query_lower for keyword in keywords.get("briefing", [])):
            return "briefing"

        # VIP 체크
        if any(keyword in query_lower for keyword in keywords.get("vip", [])):
            return "vip_list"

        # 고객 정보 체크
        if any(keyword in query_lower for keyword in keywords.get("customer_info", [])) and \
                any(keyword in query_lower for keyword in ["님", "씨", "고객"]):
            return "customer_info"

        return "general"

    def _extract_parameters(self, query: str, action: str) -> Dict[str, Any]:
        """액션별 파라미터 추출"""
        parameters = {}

        if action in ["memo_update", "customer_info"]:
            customer_name = self._extract_customer_name(query)
            if customer_name:
                parameters["customer_name"] = customer_name

        if action == "memo_update":
            memo_content = self._extract_memo_content(query, parameters.get("customer_name"))
            if memo_content:
                parameters["memo_content"] = memo_content

        return parameters

    def _extract_customer_name(self, text: str) -> Optional[str]:
        """고객명 추출"""
        patterns = [r'([가-힣]{2,4})님', r'([가-힣]{2,4})\s*고객', r'([가-힣]{2,4})\s*씨']
        for pattern in patterns:
            match = re.search(pattern, text)
            if match:
                return match.group(1)
        return None

    def _extract_memo_content(self, text: str, customer_name: Optional[str]) -> Optional[str]:
        """메모 내용 추출"""
        if not customer_name:
            return text

        memo = re.sub(rf'{customer_name}[님씨고객]*\s*', '', text).strip()
        return memo if memo else text

    def _get_default_response(self, action: str) -> str:
        """액션별 기본 응답"""
        responses = {
            "briefing": "오늘 예약된 고객들을 확인해드릴게요! 📋",
            "memo_update": "고객 메모를 업데이트하겠습니다! 📝",
            "customer_info": "고객 정보를 조회해드릴게요! 🔍",
            "vip_list": "VIP 고객 목록을 확인해드릴게요! 👑",
            "general": self.config.get("response_templates", {}).get("welcome", "무엇을 도와드릴까요? 😊")
        }
        return responses.get(action, responses["general"])

    # === Spring Boot API 연동 메서드들 (예외처리 강화) ===

    def _handle_customer_briefing(self, shop_id: int) -> str:
        """오늘 고객 브리핑 처리"""
        try:
            # 오늘 예약 정보 조회
            reservations = self._get_today_reservations(shop_id)

            if not reservations:
                return self.config.get("response_templates", {}).get("briefing_empty",
                                                                     "오늘 예약된 고객이 없어서 여유로운 하루가 될 것 같아요! 😴✨")

            # 고객별 상세 정보 수집
            customer_details = []
            for reservation in reservations:
                try:
                    detail = self._get_customer_detail(shop_id, reservation.get('clientCode'))
                    customer_details.append({**reservation, **detail})
                except APIException as e:
                    logger.warning(f"고객 상세정보 조회 실패: {reservation.get('clientCode')} - {e}")
                    # 기본 예약 정보만으로도 브리핑 제공
                    customer_details.append(reservation)

            # 브리핑 메시지 생성
            return self._generate_briefing_message(customer_details)

        except APIException:
            raise  # API 예외는 상위로 전파
        except Exception as e:
            logger.error(f"브리핑 처리 중 예상치 못한 오류: {e}")
            return "브리핑 조회 중 예상치 못한 오류가 발생했어요. 다시 시도해주세요. 😅"

    def _handle_memo_update(self, parameters: Dict[str, Any], shop_id: int) -> str:
        """고객 메모 업데이트 처리"""
        try:
            customer_name = parameters.get('customer_name')
            memo_content = parameters.get('memo_content')

            if not customer_name:
                return "고객명을 명확히 말씀해주세요. 📝\n예: '김민수님 핑크색 염색하셨어요'"

            # 고객 검색
            customer = self._find_customer_by_name(shop_id, customer_name)

            if not customer:
                template = self.config.get("response_templates", {}).get("customer_not_found",
                                                                         "'{customer_name}' 고객을 찾을 수 없어요. 정확한 이름을 확인해주세요 😅")
                return template.format(customer_name=customer_name)

            # 메모 업데이트
            success = self._update_customer_memo(shop_id, customer['clientCode'], memo_content)

            if success:
                template = self.config.get("response_templates", {}).get("memo_success",
                                                                         "{customer_name}님의 시술 내용이 메모에 잘 저장되었어요! 다음 방문때 참고하겠습니다 ✨")
                return template.format(customer_name=customer_name)
            else:
                return f"{customer_name}님의 메모 저장에 실패했어요. 다시 시도해주세요. 😅"

        except APIException:
            raise  # API 예외는 상위로 전파
        except Exception as e:
            logger.error(f"메모 업데이트 중 예상치 못한 오류: {e}")
            return f"메모 업데이트 중 예상치 못한 오류가 발생했어요: {str(e)}"

    def _handle_customer_info(self, parameters: Dict[str, Any], shop_id: int) -> str:
        """고객 정보 조회 처리"""
        try:
            customer_name = parameters.get('customer_name')

            if not customer_name:
                return "조회할 고객명을 명확히 말씀해주세요. 🔍"

            # 고객 정보 조회
            customer = self._find_customer_by_name(shop_id, customer_name)

            if not customer:
                template = self.config.get("response_templates", {}).get("customer_not_found",
                                                                         "'{customer_name}' 고객을 찾을 수 없어요. 정확한 이름을 확인해주세요 😅")
                return template.format(customer_name=customer_name)

            # 고객 상세 정보 조회
            try:
                detail = self._get_customer_detail(shop_id, customer['clientCode'])
            except APIException as e:
                logger.warning(f"고객 상세정보 조회 실패, 기본 정보만 제공: {e}")
                detail = {}

            # 고객 정보 요약 생성
            return self._generate_customer_info_message(customer, detail)

        except APIException:
            raise  # API 예외는 상위로 전파
        except Exception as e:
            logger.error(f"고객 정보 조회 중 예상치 못한 오류: {e}")
            return f"고객 정보 조회 중 예상치 못한 오류가 발생했어요: {str(e)}"

    def _handle_vip_customers(self, shop_id: int) -> str:
        """VIP 고객 리스트 처리"""
        try:
            vip_customers = self._get_vip_customers(shop_id)

            if not vip_customers:
                return "현재 등록된 VIP 고객이 없어요. 👑"

            return self._generate_vip_list_message(vip_customers)

        except APIException:
            raise  # API 예외는 상위로 전파
        except Exception as e:
            logger.error(f"VIP 고객 조회 중 예상치 못한 오류: {e}")
            return f"VIP 고객 조회 중 예상치 못한 오류가 발생했어요: {str(e)}"

    def _handle_general_chat(self, user_question: str, ai_response: str) -> str:
        """일반 대화 처리"""
        if ai_response:
            return ai_response

        # 인사말 체크
        if any(keyword in user_question.lower() for keyword in ["안녕", "hello", "hi"]):
            return self.config.get("response_templates", {}).get("welcome", "안녕하세요! 😊")

        return "무엇을 도와드릴까요? 😊"

    # === Spring Boot API 호출 메서드들 (예외처리 강화) ===

    def _get_today_reservations(self, shop_id: int) -> List[Dict[str, Any]]:
        """오늘 예약 정보 조회"""
        try:
            base_url = self.config.get("spring_api", {}).get("base_url", "http://localhost:8080")
            endpoint = self.config.get("spring_api", {}).get("endpoints", {}).get("today_reservations", "")
            url = base_url + endpoint.format(shop_id=shop_id)

            logger.info(f"📡 예약 정보 조회 API 호출: {url}")

            response = requests.get(url, timeout=10)

            # HTTP 상태 코드 체크
            if response.status_code == 404:
                raise APIException("예약 정보를 찾을 수 없습니다", response.status_code, "today_reservations")
            elif response.status_code == 500:
                raise APIException("서버 내부 오류가 발생했습니다", response.status_code, "today_reservations")
            elif response.status_code != 200:
                raise APIException(f"API 호출 실패: {response.status_code}", response.status_code, "today_reservations")

            try:
                data = response.json()
            except json.JSONDecodeError:
                raise APIException("서버 응답을 해석할 수 없습니다", response.status_code, "today_reservations")

            # 응답 구조 검증
            if not isinstance(data, dict) or 'data' not in data:
                raise APIException("잘못된 응답 형식입니다", response.status_code, "today_reservations")

            result = data.get('data', [])
            logger.info(f"✅ 예약 정보 조회 성공: {len(result)}건")
            return result

        except requests.exceptions.Timeout:
            raise APIException("서버 응답 시간이 초과되었습니다", None, "today_reservations")
        except requests.exceptions.ConnectionError:
            raise APIException("서버에 연결할 수 없습니다", None, "today_reservations")
        except requests.exceptions.RequestException as e:
            raise APIException(f"네트워크 오류가 발생했습니다: {str(e)}", None, "today_reservations")

    def _get_customer_detail(self, shop_id: int, client_code: int) -> Dict[str, Any]:
        """고객 상세 정보 조회"""
        try:
            base_url = self.config.get("spring_api", {}).get("base_url", "http://localhost:8080")
            endpoint = self.config.get("spring_api", {}).get("endpoints", {}).get("customer_detail", "")
            url = base_url + endpoint.format(shop_id=shop_id, client_code=client_code)

            logger.info(f"📡 고객 상세정보 조회 API 호출: {url}")

            response = requests.get(url, timeout=10)

            if response.status_code == 404:
                raise APIException("고객 정보를 찾을 수 없습니다", response.status_code, "customer_detail")
            elif response.status_code == 500:
                raise APIException("서버 내부 오류가 발생했습니다", response.status_code, "customer_detail")
            elif response.status_code != 200:
                raise APIException(f"API 호출 실패: {response.status_code}", response.status_code, "customer_detail")

            try:
                data = response.json()
            except json.JSONDecodeError:
                raise APIException("서버 응답을 해석할 수 없습니다", response.status_code, "customer_detail")

            if not isinstance(data, dict) or 'data' not in data:
                raise APIException("잘못된 응답 형식입니다", response.status_code, "customer_detail")

            result = data.get('data', {})
            logger.info(f"✅ 고객 상세정보 조회 성공: {client_code}")
            return result

        except requests.exceptions.Timeout:
            raise APIException("서버 응답 시간이 초과되었습니다", None, "customer_detail")
        except requests.exceptions.ConnectionError:
            raise APIException("서버에 연결할 수 없습니다", None, "customer_detail")
        except requests.exceptions.RequestException as e:
            raise APIException(f"네트워크 오류가 발생했습니다: {str(e)}", None, "customer_detail")

    def _find_customer_by_name(self, shop_id: int, customer_name: str) -> Optional[Dict[str, Any]]:
        """고객명으로 고객 검색"""
        try:
            customers = self._get_all_customers(shop_id)

            for customer in customers:
                if customer.get('userName') == customer_name:
                    logger.info(f"✅ 고객 검색 성공: {customer_name}")
                    return customer

            logger.info(f"❌ 고객 검색 실패: {customer_name}")
            return None

        except APIException:
            raise  # API 예외는 상위로 전파

    def _get_all_customers(self, shop_id: int) -> List[Dict[str, Any]]:
        """전체 고객 목록 조회"""
        try:
            base_url = self.config.get("spring_api", {}).get("base_url", "http://localhost:8080")
            endpoint = self.config.get("spring_api", {}).get("endpoints", {}).get("customers", "")
            url = base_url + endpoint.format(shop_id=shop_id)

            logger.info(f"📡 고객 목록 조회 API 호출: {url}")

            response = requests.get(url, timeout=10)

            if response.status_code == 404:
                raise APIException("고객 목록을 찾을 수 없습니다", response.status_code, "customers")
            elif response.status_code == 500:
                raise APIException("서버 내부 오류가 발생했습니다", response.status_code, "customers")
            elif response.status_code != 200:
                raise APIException(f"API 호출 실패: {response.status_code}", response.status_code, "customers")

            try:
                data = response.json()
            except json.JSONDecodeError:
                raise APIException("서버 응답을 해석할 수 없습니다", response.status_code, "customers")

            if not isinstance(data, dict) or 'data' not in data:
                raise APIException("잘못된 응답 형식입니다", response.status_code, "customers")

            result = data.get('data', [])
            logger.info(f"✅ 고객 목록 조회 성공: {len(result)}명")
            return result

        except requests.exceptions.Timeout:
            raise APIException("서버 응답 시간이 초과되었습니다", None, "customers")
        except requests.exceptions.ConnectionError:
            raise APIException("서버에 연결할 수 없습니다", None, "customers")
        except requests.exceptions.RequestException as e:
            raise APIException(f"네트워크 오류가 발생했습니다: {str(e)}", None, "customers")

    def _update_customer_memo(self, shop_id: int, client_code: int, memo: str) -> bool:
        """고객 메모 업데이트"""
        try:
            base_url = self.config.get("spring_api", {}).get("base_url", "http://localhost:8080")
            endpoint = self.config.get("spring_api", {}).get("endpoints", {}).get("update_memo", "")
            url = base_url + endpoint.format(shop_id=shop_id, client_code=client_code)

            logger.info(f"📡 메모 업데이트 API 호출: {url}")

            response = requests.patch(url, params={"memo": memo}, timeout=10)

            if response.status_code == 404:
                raise APIException("고객을 찾을 수 없습니다", response.status_code, "update_memo")
            elif response.status_code == 500:
                raise APIException("서버 내부 오류가 발생했습니다", response.status_code, "update_memo")
            elif response.status_code != 200:
                raise APIException(f"API 호출 실패: {response.status_code}", response.status_code, "update_memo")

            # 성공 응답 검증
            try:
                data = response.json()
                if not data.get('success', False):
                    raise APIException("메모 업데이트에 실패했습니다", response.status_code, "update_memo")
            except json.JSONDecodeError:
                # JSON 응답이 없어도 200이면 성공으로 간주
                pass

            logger.info(f"✅ 메모 업데이트 성공: {client_code}")
            return True

        except requests.exceptions.Timeout:
            raise APIException("서버 응답 시간이 초과되었습니다", None, "update_memo")
        except requests.exceptions.ConnectionError:
            raise APIException("서버에 연결할 수 없습니다", None, "update_memo")
        except requests.exceptions.RequestException as e:
            raise APIException(f"네트워크 오류가 발생했습니다: {str(e)}", None, "update_memo")

    def _get_vip_customers(self, shop_id: int) -> List[Dict[str, Any]]:
        """VIP 고객 목록 조회"""
        try:
            customers = self._get_all_customers(shop_id)

            features = self.config.get("features", {})
            vip_config = features.get("vip", {})
            min_payment = vip_config.get("min_payment_amount", 500000)
            min_visits = vip_config.get("min_visit_count", 5)

            vip_customers = []
            for customer in customers:
                try:
                    is_vip = (
                            (customer.get('memo', '').upper().find('VIP') != -1) or
                            (customer.get('totalPaymentAmount', 0) >= min_payment) or
                            (customer.get('visitCount', 0) >= min_visits)
                    )

                    if is_vip:
                        vip_customers.append(customer)
                except (TypeError, ValueError) as e:
                    logger.warning(f"VIP 고객 판별 중 데이터 오류: {customer.get('userName', 'Unknown')} - {e}")
                    continue

            # 총 결제금액 기준 내림차순 정렬
            try:
                vip_customers.sort(key=lambda x: x.get('totalPaymentAmount', 0), reverse=True)
            except (TypeError, ValueError) as e:
                logger.warning(f"VIP 고객 정렬 중 오류: {e}")

            logger.info(f"✅ VIP 고객 목록 조회 성공: {len(vip_customers)}명")
            return vip_customers

        except APIException:
            raise  # API 예외는 상위로 전파
        except Exception as e:
            logger.error(f"VIP 고객 목록 처리 중 예상치 못한 오류: {e}")
            raise APIException(f"VIP 고객 목록 처리 중 오류가 발생했습니다: {str(e)}", None, "vip_customers")

    # === 메시지 생성 메서드들 (안전성 강화) ===

    def _generate_briefing_message(self, customer_data: List[Dict[str, Any]]) -> str:
        """브리핑 메시지 생성"""
        try:
            briefing = "📋 **오늘의 고객 브리핑**\n\n"

            for i, customer in enumerate(customer_data, 1):
                try:
                    briefing += f"**{i}. {customer.get('customerName', customer.get('userName', '이름없음'))}님** 👤\n"
                    briefing += f"⏰ **예약시간**: {customer.get('reservationTime', customer.get('resvTime', '시간미정'))}\n"
                    briefing += f"💇 **서비스**: {customer.get('serviceName', customer.get('menuName', '서비스미정'))}\n"
                    briefing += f"📊 **방문횟수**: {customer.get('visitCount', 0)}회\n"

                    if customer.get('favoriteMenuName'):
                        briefing += f"❤️ **선호서비스**: {customer['favoriteMenuName']}\n"

                    if customer.get('memo'):
                        briefing += f"📝 **메모**: {customer['memo']}\n"

                    if customer.get('lastVisited') and customer['lastVisited'] != '방문 기록 없음':
                        briefing += f"📅 **최근방문**: {customer['lastVisited']}\n"

                    briefing += "\n"
                except Exception as e:
                    logger.warning(f"고객 브리핑 항목 생성 중 오류: {customer} - {e}")
                    briefing += f"**{i}. 고객 정보 오류** ⚠️\n\n"

            briefing += f"총 **{len(customer_data)}명**의 고객이 예약되어 있습니다.\n"
            briefing += "오늘도 최고의 서비스로 고객님들을 맞이해주세요! 💪✨"

            return briefing

        except Exception as e:
            logger.error(f"브리핑 메시지 생성 중 오류: {e}")
            return "브리핑 메시지 생성 중 오류가 발생했어요. 관리자에게 문의해주세요. 🛠️"

    def _generate_customer_info_message(self, customer: Dict[str, Any], detail: Dict[str, Any]) -> str:
        """고객 정보 메시지 생성"""
        try:
            name = customer.get('userName', '이름없음')

            info = f"👤 **{name}님 고객 정보**\n\n"

            # 안전한 정보 추출
            phone = customer.get('phone', detail.get('phone', '정보없음'))
            birthday = customer.get('birthday', detail.get('birthday', '정보없음'))
            visit_count = detail.get('visitCount', customer.get('visitCount', 0))
            total_amount = detail.get('totalPaymentAmount', customer.get('totalPaymentAmount', 0))
            last_visited = detail.get('lastVisited', customer.get('lastVisited', '방문기록없음'))
            favorite_menu = detail.get('favoriteMenuName', customer.get('favoriteMenuName', ''))
            memo = customer.get('memo', detail.get('memo', ''))
            sendable = customer.get('sendable', detail.get('sendable', False))

            info += f"📞 **연락처**: {phone}\n"
            info += f"🎂 **생년월일**: {birthday}\n"
            info += f"📊 **방문횟수**: {visit_count}회\n"

            # 금액 포맷팅 (오류 방지)
            try:
                if isinstance(total_amount, (int, float)):
                    info += f"💰 **총 결제금액**: {int(total_amount):,}원\n"
                else:
                    info += f"💰 **총 결제금액**: 정보없음\n"
            except (ValueError, TypeError):
                info += f"💰 **총 결제금액**: 정보없음\n"

            info += f"📅 **최근방문**: {last_visited}\n"

            if favorite_menu:
                info += f"❤️ **선호서비스**: {favorite_menu}\n"

            if memo:
                info += f"📝 **메모**: {memo}\n"

            if sendable:
                info += f"📱 **수신동의**: ✅ 동의\n"
            else:
                info += f"📱 **수신동의**: ❌ 거부\n"

            return info

        except Exception as e:
            logger.error(f"고객 정보 메시지 생성 중 오류: {e}")
            return f"고객 정보 메시지 생성 중 오류가 발생했어요: {str(e)} 🛠️"

    def _generate_vip_list_message(self, vip_customers: List[Dict[str, Any]]) -> str:
        """VIP 고객 리스트 메시지 생성"""
        try:
            vip_list = "👑 **VIP 고객 리스트**\n\n"

            for i, customer in enumerate(vip_customers, 1):
                try:
                    name = customer.get('userName', '이름없음')
                    phone = customer.get('phone', '연락처없음')
                    total_amount = customer.get('totalPaymentAmount', 0)
                    visit_count = customer.get('visitCount', 0)
                    last_visited = customer.get('lastVisited', '방문기록없음')

                    vip_list += f"**{i}. {name}님**\n"
                    vip_list += f"📞 {phone}\n"

                    # 금액 포맷팅 (오류 방지)
                    try:
                        if isinstance(total_amount, (int, float)):
                            vip_list += f"💰 총 결제: {int(total_amount):,}원\n"
                        else:
                            vip_list += f"💰 총 결제: 정보없음\n"
                    except (ValueError, TypeError):
                        vip_list += f"💰 총 결제: 정보없음\n"

                    vip_list += f"📊 방문: {visit_count}회\n"
                    vip_list += f"📅 최근: {last_visited}\n\n"

                except Exception as e:
                    logger.warning(f"VIP 고객 항목 생성 중 오류: {customer} - {e}")
                    vip_list += f"**{i}. VIP 고객 정보 오류** ⚠️\n\n"

            vip_list += f"총 **{len(vip_customers)}명**의 VIP 고객이 등록되어 있습니다! 👑✨"

            return vip_list

        except Exception as e:
            logger.error(f"VIP 리스트 메시지 생성 중 오류: {e}")
            return f"VIP 리스트 메시지 생성 중 오류가 발생했어요: {str(e)} 🛠️"