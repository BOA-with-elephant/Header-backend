from typing import Annotated
from fastapi import APIRouter, Form, HTTPException
import urllib.parse
import os
from app.models.chat_request import ChatRequest
from app.models.chat_response import ChatResponse
from app.services.visitors_service import VisitorsChatBotService

router = APIRouter(prefix="/visitors", tags=["visitors"])

# 서비스 인스턴스 생성
try:
    service = VisitorsChatBotService()
    print("✅ VisitorsChatBotService 인스턴스 생성 성공")
except Exception as e:
    print(f"❌ VisitorsChatBotService 인스턴스 생성 실패: {e}")
    import sys
    import logging
    logging.error(f"Critical: VisitorsChatBotService 초기화 실패 - {e}")
    # 개발 환경에서는 계속 실행하되, 프로덕션에서는 종료
    if os.getenv("ENV", "development") == "production":
            sys.exit(1)
    service = None

def fix_korean_encoding(text: str) -> str:
    """한글 인코딩 문제 해결"""
    try:
        # 1. 이미 올바른 한글이면 그대로 반환
        if all(ord(char) < 128 or '\uac00' <= char <= '\ud7af' for char in text if char.isalpha()):
            return text

        # 2. Latin-1로 잘못 인코딩된 UTF-8을 수정
        try:
            fixed = text.encode('latin-1').decode('utf-8')
            return fixed
        except (UnicodeDecodeError, UnicodeEncodeError):
            pass
        except (UnicodeDecodeError, ValueError):
            pass

        # 3. URL 디코딩 시도
        try:
            fixed = urllib.parse.unquote(text, encoding='utf-8')
            if fixed != text:
                print(f"🔧 URL 디코딩: '{text}' -> '{fixed}'")
                return fixed
        except:
            pass

        # 4. 수정 실패시 원본 반환
        print(f"⚠️ 인코딩 수정 실패, 원본 사용: '{text}'")
        return text

    except Exception as e:
        print(f"❌ 인코딩 처리 오류: {e}")
        return text

@router.post("/ask", response_model=ChatResponse)
async def ask_chatbot(request: ChatRequest):
    """고객관리 챗봇에게 질문하기"""
    if service is None:
        raise HTTPException(status_code=500, detail="챗봇 서비스를 사용할 수 없습니다")

    try:
        shop_id = getattr(request, 'shop_id', None)
        answer = await service.generate_response(request.question, shop_id)
        return ChatResponse(session_id="visitors-session", answer=answer)
    except Exception as e:
        error_message = "죄송합니다. 일시적인 오류가 발생했어요. 다시 시도해주세요 🙏"
        return ChatResponse(session_id="visitors-session", answer=error_message)

@router.post("/ask_with_shop", response_model=ChatResponse)
async def ask_chatbot_with_shop(
        question: Annotated[str, Form(...)],
        shop_id: Annotated[int, Form(...)]
):
    """샵 ID를 포함한 고객관리 챗봇 질문 (인코딩 수정)"""
    if service is None:
        raise HTTPException(status_code=500, detail="챗봇 서비스를 사용할 수 없습니다")

    try:
        # 한글 인코딩 수정
        fixed_question = fix_korean_encoding(question)
        answer = await service.generate_response(fixed_question, shop_id)

        return ChatResponse(session_id="visitors-session", answer=answer)

    except Exception as e:
        print(f"❌ 챗봇 처리 오류: {e}")
        error_message = "죄송합니다. 일시적인 오류가 발생했어요. 다시 시도해주세요 🙏"
        return ChatResponse(session_id="visitors-session", answer=error_message)

@router.get("/help")
async def get_help():
    """고객관리 챗봇 도움말"""
    help_message = """
🤖 **고객관리 도우미 사용법**

**1. 고객 브리핑** 📋
- "오늘 손님들 브리핑해줘"
- "오늘 예약 알려줘"

**2. 고객 메모 관리** 📝
- "김민수님 핑크색 염색하셨어요"
- "이지영님 펌 시술 받으셨습니다"
- "박철수님 다음에 커트 원하신다고 하셨어요"

**3. 고객 정보 조회** 🔍
- "김민수님 정보 알려줘"
- "이지영님 정보 보여줘"

**4. VIP 고객 관리** 👑
- "VIP 고객들 알려줘"
- "브이아이피 리스트"

궁금한 게 있으면 언제든 말씀해주세요! 😊
"""
    return {"help": help_message}

@router.get("/status")
async def get_bot_status():
    """챗봇 상태 확인"""
    return {
        "status": "healthy",
        "bot_name": "고객 관리 도우미",
        "version": "1.0",
        "description": "헤어샵 고객 정보 관리 및 브리핑을 도와주는 AI 어시스턴트"
    }

# 인코딩 테스트용 엔드포인트 (필요시)
@router.post("/test_encoding")
async def test_encoding(
        test_text: Annotated[str, Form(...)]
):
    """인코딩 테스트용"""
    try:
        fixed_text = fix_korean_encoding(test_text)

        return {
            "original": test_text,
            "fixed": fixed_text,
            "success": fixed_text != test_text and any('\uac00' <= char <= '\ud7af' for char in fixed_text)
        }
    except Exception as e:
        return {"error": str(e)}