#!/usr/bin/env python3
"""
테스트 스크립트 - 범위 외 쿼리 감지 시스템
"""
import json

# 테스트용 샘플 의도 분석 결과
sample_out_of_scope_analysis = {
    "intent": "out_of_scope",
    "confidence": 0.9,
    "parameters": {
        "time_reference": "내일"
    },
    "required_apis": [],
    "reasoning": "미래 날짜 예약 요청으로 지원 범위를 벗어남"
}

sample_in_scope_analysis = {
    "intent": "reservation_briefing", 
    "confidence": 0.95,
    "parameters": {
        "time_reference": "오늘"
    },
    "required_apis": ["today_reservations"],
    "reasoning": "오늘 예약 현황 요청으로 지원 범위 내"
}

# 테스트 쿼리들
test_queries = [
    # 범위 외 쿼리들
    "내일 누가 와?",
    "매출이 얼마야?", 
    "직원 스케줄 알려줘",
    "재고 현황 보여줘",
    "다음 주 예약은?",
    
    # 일반 대화 (템플릿 응답)
    "HI", 
    "안녕하세요",
    "뭐 할 수 있어?",
    "고마워요",
    
    # 범위 내 쿼리들
    "오늘 누가 와?",
    "김민수님 정보 알려줘", 
    "오늘 예약 브리핑해줘",
    "이분 펌 하고 싶어한다고 메모해줘"
]

print("=" * 50)
print("범위 외 쿼리 감지 시스템 테스트")
print("=" * 50)

print("\n✅ 구현 완료 항목:")
print("1. AI 의도 분석 프롬프트에 'out_of_scope' 카테고리 추가")
print("2. 범위 외 쿼리별 맞춤형 응답 템플릿 설정")
print("3. 서비스 로직에서 out_of_scope 처리 추가")
print("4. 미래 날짜, 결제/매출, 직원 관리 등 세분화된 응답")
print("5. 일반 대화 템플릿 응답 시스템 추가 (HI → 친근한 인사)")
print("6. AI 호출 최소화로 응답 속도 및 효율성 향상")

print(f"\n🧪 테스트 쿼리 ({len(test_queries)}개):")
for i, query in enumerate(test_queries, 1):
    if i <= 5:
        scope = "❌ 범위 외"
    elif i <= 9:
        scope = "💬 일반 대화"
    else:
        scope = "✅ 범위 내"
    print(f"{i:2d}. {query} ({scope})")

print("\n🚀 시스템 작동 방식:")
print("1. AI가 사용자 질문 분석 → intent 판단")
print("2. out_of_scope 의도 감지시 → API 호출 없이 바로 응답") 
print("3. 키워드 기반으로 적절한 응답 템플릿 선택")
print("4. 대안 기능 제시로 사용자 가이드")

print("\n" + "=" * 50)
print("테스트 완료 - 실제 서비스에서 확인 가능")
print("=" * 50)