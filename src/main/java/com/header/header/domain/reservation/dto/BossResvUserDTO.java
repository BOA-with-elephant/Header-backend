package com.header.header.domain.reservation.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;

@Getter
@Setter
@ToString
public class BossResvUserDTO {

    private Integer userCode;
    private String userName;
    private String userPhone;
    private Date birthday;

}
