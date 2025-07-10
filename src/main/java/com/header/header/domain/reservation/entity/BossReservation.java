package com.header.header.domain.reservation.entity;

import com.header.header.domain.menu.entity.Menu;
import com.header.header.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name="tbl_reservation")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class BossReservation {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private int resvCode;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "userCode")
    private User userInfo;
    private Integer shopCode;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "menuCode")
    private Menu menuInfo;
    private Date resvDate;
    private Time resvTime;
    private String userComment;
    private String resvState;

    public BossReservation(int resvCode, User userInfo, Integer shopCode, Menu menuInfo, Date resvDate, Time resvTime, String userComment, String resvState) {
        this.resvCode = resvCode;
        this.userInfo = userInfo;
        this.shopCode = shopCode;
        this.menuInfo = menuInfo;
        this.resvDate = resvDate;
        this.resvTime = resvTime;
        this.userComment = userComment;
        this.resvState = resvState;
    }
}
