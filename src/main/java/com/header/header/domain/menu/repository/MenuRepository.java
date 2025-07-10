package com.header.header.domain.menu.repository;

import com.header.header.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MenuRepository extends JpaRepository<Menu, Integer> {

    /* 메뉴명으로 메뉴 조회 */
    Menu findByMenuName(String menuName);
}
