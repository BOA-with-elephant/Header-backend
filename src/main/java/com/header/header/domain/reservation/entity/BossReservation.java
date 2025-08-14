package com.header.header.domain.reservation.entity;

import com.header.header.domain.menu.entity.Menu;
import com.header.header.domain.reservation.converter.ReservationStateConverter;
import com.header.header.domain.reservation.dto.BasicReservationDTO;
import com.header.header.domain.reservation.dto.BossReservationDTO;
import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.Date;
import java.sql.Time;

@Slf4j
@Entity
@Table(name="tbl_reservation")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
@Builder
public class BossReservation {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private int resvCode;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "userCode")
    private User userInfo;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "shopCode")
    private Shop shopInfo;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "menuCode")
    private Menu menuInfo;
    private Date resvDate;
    private Time resvTime;
    private String userComment;

    @Convert(converter = ReservationStateConverter.class)
    private ReservationState resvState;

    public BossReservation(int resvCode, User userInfo, Shop shopInfo, Menu menuInfo, Date resvDate, Time resvTime, String userComment, ReservationState resvState) {
        this.resvCode = resvCode;
        this.userInfo = userInfo;
        this.shopInfo = shopInfo;
        this.menuInfo = menuInfo;
        this.resvDate = resvDate;
        this.resvTime = resvTime;
        this.userComment = userComment;
        this.resvState = resvState;
    }

    public void cancelReservation() {
        this.resvState = ReservationState.CANCEL;
    }

    public void modifyReservation(BossReservationDTO reservationDTO, Menu menu){
        this.menuInfo = menu;
        this.resvDate = reservationDTO.getResvDate();
        this.resvTime = reservationDTO.getResvTime();
        this.userComment = reservationDTO.getUserComment();
    }

    public void setMenu(Menu menu) {
        this.menuInfo = menu;
    }

    public void setUser(User user){
        this.userInfo = user;
    }

    public void setShop(Shop shop){
        this.shopInfo = shop;
    }

    public void completeProcedure() {
        this.resvState = ReservationState.FINISH;
    }

    public void noShowHandling() {
        this.resvState = ReservationState.CANCEL;
        this.userComment = "노쇼";
    }
}
