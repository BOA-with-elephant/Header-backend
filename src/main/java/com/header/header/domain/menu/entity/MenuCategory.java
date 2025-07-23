package com.header.header.domain.menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tbl_menu_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)   // 모든 필드를 받는 생성자를 private로 생성 (빌더 패턴용)
@Builder    // 빌더 패턴 지원 (객체 생성을 더 안전하고 가독성 있게)
@ToString
public class MenuCategory {

    /**
     * 복합 기본키
     * @EmbeddedId: 별도의 클래스(MenuCategoryId)로 정의된 복합키를 사용하기 위함
     * */
    @EmbeddedId
    private MenuCategoryId id; // categoryCode + shopCode

    private String categoryName;
    private String menuColor;
    private Boolean isActive;

    // 테스트 확인용 메소드 - null 안전성 추가
    public int getCategoryCode() {
        return id != null ? id.getCategoryCode() : 0;
    }

    public int getShopCode() {
        return id != null ? id.getShopCode() : 0;
    }
}
