package com.header.header.common.util;

public class MaskingUtils {
    public static String maskPhone(String phone) {
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1-****-$2");
    }
}
