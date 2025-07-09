package com.header.header.domain.reservation.entity;

import com.header.header.domain.user.enitity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name="tbl_reservation")
@Immutable  // Hibernate에게 읽기 전용임을 명시
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
    private BossResvMenu menuInfo;
    private Date resvDate;
    private Time resvTime;
    private String userComment;
    private String resvState;

}
