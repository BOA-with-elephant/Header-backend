package com.header.header.domain.visitors.enitity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_visitors")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class Visitors {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Integer clientCode;
    private Integer userCode;
    private Integer shopCode;
    private String memo;
    private boolean sendable;
    private boolean isActive;


    public void modifyClientMemo(String memo){
        this.memo = memo;
    }

    public void deleteLogical(){
        this.isActive = false;
    }
}
