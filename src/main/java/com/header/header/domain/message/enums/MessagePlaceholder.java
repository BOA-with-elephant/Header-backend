package com.header.header.domain.message.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum MessagePlaceholder {
    CUSTOMER_NAME("{고객명}"),
    DATE("{날짜}"),
    TIME("{시간}"),
    SERVICE_NAME("{서비스명}"),
    AMOUNT("{금액}");

    private final String placeholder;

    // comment. Enum은 싱글톤이기 때문에 Spring이 관리하지 않아도 JVM이 관리한다.
    MessagePlaceholder(String placeholder){
        this.placeholder = placeholder;
    }

    public String getPlaceholder() { return placeholder; }

    // ✅ 모든 플레이스홀더 목록 반환
    public static Set<String> getAllPlaceholders(){
        return Arrays.stream(values())
                .map(MessagePlaceholder::getPlaceholder) // :: 메서드 참조
                .collect(Collectors.toSet());
    }

    // ✅ 유효한 플레이스 홀더인지 체크
    public static boolean isValidPlaceholder(String placeholder){
        return getAllPlaceholders().contains(placeholder);
    }

    // ✅ 템플릿에서 플레이스홀더 추출
    // Set 사용 : 중복 제거를 위해서. set의 contains 메서드가 O(1)이므로.
    public static Set<String> extractPlaceholders(String template){
        /* todo. 올바르지 않은 플레이스 홀더일 경우? 에 대한 로직은 없음 추후 개선 예정*/
        Set<String> found = new HashSet<>();
        Pattern pattern = Pattern.compile("\\{[^}]+\\}");// 정규표현식을 컴파일해서 Pattern 객체 생성
        Matcher matcher = pattern.matcher(template);

        while (matcher.find()){
            found.add(matcher.group());
        }

        return found;
    }
}
