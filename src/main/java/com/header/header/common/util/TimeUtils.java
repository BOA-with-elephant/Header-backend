package com.header.header.common.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
    public static String toRelativeTime(LocalDateTime time) {
        long days = ChronoUnit.DAYS.between(time, LocalDateTime.now());
        return days == 0 ? "오늘" : days + "일 전";
    }
}

