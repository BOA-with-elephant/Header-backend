from fastapi import APIRouter, Depends
from app.core.security import admin_required

router = APIRouter(prefix="/admin", tags=["admin"])

@router.get("/dashboard")
def fetch_dashboard(user: dict = Depends(admin_required)):
    return {"message": f"Welcome admin {user['sub']}"}
