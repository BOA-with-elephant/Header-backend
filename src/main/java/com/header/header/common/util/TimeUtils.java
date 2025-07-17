package com.header.header.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    private static final DateTimeFormatter BIRTHDAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private TimeUtils() {}

    public static String toRelativeTime(LocalDate time) {
        if (time == null) return "방문 기록 없음";
        long days = ChronoUnit.DAYS.between(time, LocalDate.now());
        return days == 0 ? "오늘" : days + "일 전";
    }

    public static String formatBirthday(LocalDate birthday) {
        if (birthday == null) return "";
        return birthday.format(BIRTHDAY_FORMATTER);
    }
}