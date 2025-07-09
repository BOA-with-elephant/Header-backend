package com.header.header.domain.message.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlaceholderDTO {
    private String placeholder;

    public PlaceholderDTO(String placeholder){
        this.placeholder = placeholder;
    }
}
