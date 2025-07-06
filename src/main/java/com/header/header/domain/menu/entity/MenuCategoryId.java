package com.header.header.domain.menu.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 메뉴 카테고리의 복합 기본키를 나타내는 클래스
 * categoryCode와 shopCode를 조합하여 고유한 식별자 생성
 * 한 샵 내에서는 카테고리 코드가 중복될 수 없지만, 다른 샵에서는 같은 카테고리 코드 사용 가능
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode // equals()와 hashCode() 메서드를 자동으로 생성 (복합키에서 매우 중요!)
@ToString
public class MenuCategoryId implements Serializable {

    private int categoryCode;
    private int shopCode;

}