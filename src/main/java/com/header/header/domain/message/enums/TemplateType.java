package com.header.header.domain.message.enums;

public enum TemplateType {
    INFORMATIONAL, // 정보성
    PROMOTIONAL ;   // 광고성

    // 사용자 관리 가능 여부
    public boolean isUserManageable() {
        return this == PROMOTIONAL;
    }

    // 시스템 제공 여부
    public boolean isSystemProvided(){
        return this == INFORMATIONAL;
    }

    // CRUD 권한 체크
    public boolean canCreate(){
        return this == PROMOTIONAL;
    }

    public boolean canUpdate(){
        return this == PROMOTIONAL;
    }

    public boolean canDelete(){
        return this == PROMOTIONAL;
    }
    
    // 표시명 반환
    public String getKoreanName(){
        return switch (this){
            case INFORMATIONAL -> "정보성";
            case PROMOTIONAL -> "광고성";
        };
    }
}
