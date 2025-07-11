package com.header.header.domain.reservation.mapper;

import com.header.header.domain.menu.dto.MenuCategoryDTO;
import com.header.header.domain.reservation.dto.BossReservationDTO;
import com.header.header.domain.reservation.dto.BossResvMenuCategoryDTO;
import com.header.header.domain.reservation.dto.BossResvMenuDTO;
import com.header.header.domain.reservation.dto.BossResvUserDTO;
import com.header.header.domain.reservation.projection.BossReservationProjection;

public class BossReservationMapper {

    public static BossReservationDTO toDTO(BossReservationProjection p) {
        BossReservationDTO dto = new BossReservationDTO();
        dto.setResvCode(p.getResvCode());

        BossResvUserDTO userDTO = new BossResvUserDTO();
        userDTO.setUserName(p.getUserName());
        userDTO.setUserPhone(p.getUserPhone());
        dto.setUserInfo(userDTO);


        BossResvMenuDTO menuDTO = new BossResvMenuDTO();
        menuDTO.setMenuName(p.getMenuName());
        menuDTO.setIsActive(p.getIsActive());
        dto.setMenuInfo(menuDTO);

        BossResvMenuCategoryDTO menuCategoryDTO = new BossResvMenuCategoryDTO();
        menuCategoryDTO.setMenuColor(p.getMenuColor());
        menuDTO.setMenuCategoryInfo(menuCategoryDTO);

        dto.setResvDate(p.getResvDate());
        dto.setResvTime(p.getResvTime());
        dto.setUserComment(p.getUserComment());
        dto.setResvState(p.getResvState());

        return dto;
    }
}
