package com.header.header.domain.sales.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Time;

@Entity
@Table(name="tbl_sales")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class Sales {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private int salesCode;
    private Integer resvCode;
    private int payAmount;
    private String payMethod;
    private Time payDatetime;
    private String payStatus;

}