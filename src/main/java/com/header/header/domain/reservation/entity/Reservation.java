package com.header.header.domain.reservation.entity;

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
public class Reservation {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private int resvCode;
    private Integer userCode;
    private Integer shopCode;
    private Integer menuCode;
    private Date resvDate;
    private Time resvTime;
    private String userComment;
    private String resvState;

}
