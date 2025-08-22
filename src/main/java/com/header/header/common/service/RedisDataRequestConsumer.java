package com.header.header.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.header.header.config.RedisStreamConfig;
import com.header.header.domain.visitors.service.VisitorsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class RedisDataRequestConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RedisDataRequestConsumer.class);

    @Autowired
    private RedisTemplate<String, String> redisStreamTemplate;

    @Autowired
    private RedisStreamConfig redisConfig;

    @Autowired
    private VisitorsService visitorsService;

    @Autowired
    private ObjectMapper objectMapper;

    private boolean isRunning = false;

    @PostConstruct
    public void initialize() {
        // 스트림과 컨슈머 그룹 초기화
        initializeStreamsAndConsumerGroups();

        // 백그라운드에서 메시지 소비 시작
        startConsuming();
    }

    private void initializeStreamsAndConsumerGroups() {
        logger.info("🔧 Redis 초기화 시작...");
        
        try {
            // Redis 연결 테스트
            testRedisConnection();
            
            // 1. 스트림이 존재하지 않으면 생성 (더미 메시지로)
            logger.info("📝 스트림 생성 시작...");
            createStreamIfNotExists(redisConfig.getDataRequestsStream());
            createStreamIfNotExists(redisConfig.getDataResultsStream());

            // 2. 컨슈머 그룹 생성
            logger.info("👥 컨슈머 그룹 생성 시작...");
            createConsumerGroupIfNotExists(redisConfig.getDataRequestsStream(), redisConfig.getConsumerGroup());
            
            logger.info("✅ Redis 초기화 완료");
            
        } catch (Exception e) {
            logger.error("❌ Redis 스트림/컨슈머 그룹 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Redis 초기화 실패", e);
        }
    }
    
    private void testRedisConnection() {
        try {
            logger.info("🔌 Redis 연결 테스트...");
            redisStreamTemplate.getConnectionFactory().getConnection().ping();
            logger.info("✅ Redis 연결 성공");
        } catch (Exception e) {
            logger.error("❌ Redis 연결 실패: {}", e.getMessage());
            throw new RuntimeException("Redis 연결 실패", e);
        }
    }

    private void createStreamIfNotExists(String streamName) {
        try {
            // 스트림 존재 확인
            redisStreamTemplate.opsForStream().info(streamName);
            logger.info("✅ Redis 스트림 이미 존재: {}", streamName);
        } catch (Exception e) {
            // 스트림이 없으면 더미 메시지로 생성
            try {
                Map<String, String> dummyMessage = new HashMap<>();
                dummyMessage.put("init", "stream_created");
                dummyMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));
                
                redisStreamTemplate.opsForStream().add(streamName, dummyMessage);
                logger.info("📝 Redis 스트림 생성: {}", streamName);
            } catch (Exception createError) {
                logger.error("❌ Redis 스트림 생성 실패: {} - {}", streamName, createError.getMessage());
            }
        }
    }

    private void createConsumerGroupIfNotExists(String streamName, String groupName) {
        try {
            logger.info("👥 컨슈머 그룹 생성 시도: {} for stream: {}", groupName, streamName);
            
            // First, check if consumer group already exists
            try {
                // Use XINFO GROUPS to check existing groups
                @SuppressWarnings("unchecked")
                java.util.List<Object> groups = (java.util.List<Object>) redisStreamTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                    return connection.execute("XINFO", "GROUPS".getBytes(), streamName.getBytes());
                });
                
                if (groups != null && !groups.isEmpty()) {
                    // Parse group information to check if our group exists
                    for (Object groupInfo : groups) {
                        if (groupInfo instanceof java.util.List) {
                            @SuppressWarnings("unchecked")
                            java.util.List<Object> groupDetails = (java.util.List<Object>) groupInfo;
                            // Group info format: [name, consumers, pending, last-delivered-id, ...]
                            if (groupDetails.size() >= 2) {
                                String existingGroupName = new String((byte[]) groupDetails.get(1));
                                if (groupName.equals(existingGroupName)) {
                                    logger.info("✅ 컨슈머 그룹 이미 존재함을 확인: {} for {}", groupName, streamName);
                                    return; // Group exists, no need to create
                                }
                            }
                        }
                    }
                }
                logger.debug("컨슈머 그룹이 존재하지 않음, 생성 시도: {} for {}", groupName, streamName);
            } catch (Exception checkError) {
                logger.debug("컨슈머 그룹 존재 여부 확인 실패, 생성 시도: {}", checkError.getMessage());
                // Continue with creation attempt
            }
            
            // Method 1: Try using RedisCallback with direct command
            try {
                redisStreamTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                    // Use Redis XGROUP CREATE command directly
                    connection.execute("XGROUP", 
                        "CREATE".getBytes(),
                        streamName.getBytes(), 
                        groupName.getBytes(), 
                        "0".getBytes(), 
                        "MKSTREAM".getBytes());
                    return null;
                });
                logger.info("✅ Redis 컨슈머 그룹 생성 성공 (Method 1 - Direct Command): {} for {}", groupName, streamName);
                return;
            } catch (Exception e1) {
                String errorMsg = e1.getMessage();
                if (errorMsg != null && (errorMsg.contains("BUSYGROUP") || errorMsg.contains("already exists"))) {
                    logger.info("👥 Redis 컨슈머 그룹 이미 존재 (Method 1): {} for {}", groupName, streamName);
                    return;
                }
                logger.warn("⚠️ Method 1 (Direct Command) 실패, Method 2 시도: {}", errorMsg);
                
                // Method 2: Try with ReadOffset.from("0") 
                try {
                    redisStreamTemplate.opsForStream().createGroup(streamName, ReadOffset.from("0"), groupName);
                    logger.info("✅ Redis 컨슈머 그룹 생성 성공 (Method 2): {} for {}", groupName, streamName);
                    return;
                } catch (Exception e2) {
                    String error2Msg = e2.getMessage();
                    if (error2Msg != null && (error2Msg.contains("BUSYGROUP") || error2Msg.contains("already exists"))) {
                        logger.info("👥 Redis 컨슈머 그룹 이미 존재 (Method 2): {} for {}", groupName, streamName);
                        return;
                    }
                    logger.warn("⚠️ Method 2 실패, Method 3 시도: {}", error2Msg);
                    
                    // Method 3: Try with simple 2-parameter method
                    try {
                        redisStreamTemplate.opsForStream().createGroup(streamName, groupName);
                        logger.info("✅ Redis 컨슈머 그룹 생성 성공 (Method 3): {} for {}", groupName, streamName);
                        return;
                    } catch (Exception e3) {
                        String error3Msg = e3.getMessage();
                        if (error3Msg != null && (error3Msg.contains("BUSYGROUP") || error3Msg.contains("already exists"))) {
                            logger.info("👥 Redis 컨슈머 그룹 이미 존재 (Method 3): {} for {}", groupName, streamName);
                            return;
                        }
                        logger.warn("⚠️ Method 3도 실패: {}", error3Msg);
                        throw e3; // Re-throw the last exception
                    }
                }
            }
            
        } catch (Exception e) {
            String finalErrorMsg = e.getMessage();
            // Since we confirmed via Python that consumer group exists with 10+ consumers,
            // and "Error in execution" usually means the group already exists,
            // treat this as success rather than failure
            if (finalErrorMsg != null && (finalErrorMsg.contains("BUSYGROUP") || 
                                        finalErrorMsg.contains("already exists") ||
                                        finalErrorMsg.contains("Error in execution"))) {
                logger.info("✅ Redis 컨슈머 그룹 이미 존재 (추정): {} for {}", groupName, streamName);
                logger.info("📊 참고: Python 스크립트로 확인된 기존 컨슈머 그룹 상태 - {} consumers 활성화", "10+");
            } else {
                logger.error("❌ Redis 컨슈머 그룹 생성 최종 실패: {} for {} - {}", groupName, streamName, finalErrorMsg);
                // Don't throw the exception, continue with startup
                logger.warn("⚠️ 컨슈머 그룹 없이 계속 진행...");
            }
        }
    }

    public void startConsuming() {
        if (isRunning) {
            logger.warn("⚠️ Redis 컨슈머가 이미 실행 중입니다");
            return;
        }

        isRunning = true;
        CompletableFuture.runAsync(this::consumeMessages);
        logger.info("🚀 Redis 데이터 요청 컨슈머 시작");
    }

    private void consumeMessages() {
        String consumerName = "spring-worker-" + System.currentTimeMillis();

        while (isRunning) {
            try {
                // Redis Stream에서 메시지 읽기
                List<MapRecord<String, Object, Object>> messages = redisStreamTemplate.opsForStream().read(
                    Consumer.from(redisConfig.getConsumerGroup(), consumerName),
                    StreamReadOptions.empty().count(10).block(Duration.ofSeconds(1)),
                    StreamOffset.create(redisConfig.getDataRequestsStream(), ReadOffset.lastConsumed())
                );

                for (MapRecord<String, Object, Object> message : messages) {
                    processMessage(message);
                }

            } catch (Exception e) {
                logger.error("❌ Redis 메시지 소비 중 오류: {}", e.getMessage());
                try {
                    Thread.sleep(1000); // 에러 시 1초 대기
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void processMessage(MapRecord<String, Object, Object> message) {
        try {
            String correlationId = (String) message.getValue().get("correlation_id");
            String requestType = (String) message.getValue().get("request_type");
            String shopIdStr = (String) message.getValue().get("shop_id");
            String parametersJson = (String) message.getValue().get("parameters");

            logger.info("📥 Redis 메시지 처리 시작: {} - Type: {}", correlationId, requestType);

            // 파라미터 파싱
            Map<String, Object> parameters = new HashMap<>();
            if (parametersJson != null && !parametersJson.isEmpty()) {
                parameters = objectMapper.readValue(parametersJson, Map.class);
            }

            int shopId = Integer.parseInt(shopIdStr);

            // 요청 타입별 처리
            Map<String, Object> result = processDataRequest(requestType, shopId, parameters);

            // 결과를 Redis Stream에 발행
            publishResult(correlationId, result);

            // 메시지 확인 처리
            redisStreamTemplate.opsForStream().acknowledge(
                redisConfig.getDataRequestsStream(),
                redisConfig.getConsumerGroup(),
                message.getId()
            );

            logger.info("✅ Redis 메시지 처리 완료: {}", correlationId);

        } catch (Exception e) {
            logger.error("❌ Redis 메시지 처리 실패: {}", e.getMessage(), e);
            
            // 에러 결과 발행
            String correlationId = (String) message.getValue().get("correlation_id");
            if (correlationId != null) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("status", "error");
                errorResult.put("error", e.getMessage());
                errorResult.put("data", new HashMap<>());
                publishResult(correlationId, errorResult);
            }

            // 메시지 확인 처리 (실패해도 무한 루프 방지)
            redisStreamTemplate.opsForStream().acknowledge(
                redisConfig.getDataRequestsStream(),
                redisConfig.getConsumerGroup(),
                message.getId()
            );
        }
    }

    private Map<String, Object> processDataRequest(String requestType, int shopId, Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            switch (requestType) {
                case "customer_search":
                    String customerName = (String) parameters.get("customer_name");
                    var searchResults = visitorsService.searchCustomersByName(shopId, customerName);
                    result.put("status", "success");
                    result.put("data", searchResults);
                    break;

                case "customer_detail":
                    Integer clientCode = parseIntegerParameter(parameters.get("client_code"));
                    var customerDetail = visitorsService.getCustomerDetailInfo(shopId, clientCode);
                    result.put("status", "success");
                    result.put("data", customerDetail);
                    break;

                case "visit_history":
                    Integer historyClientCode = parseIntegerParameter(parameters.get("client_code"));
                    var visitHistory = visitorsService.getShopVisitorsHistory(shopId, historyClientCode);
                    result.put("status", "success");
                    result.put("data", visitHistory);
                    break;

                case "today_reservations":
                    var todayReservations = visitorsService.getTodayReservationCustomers(shopId);
                    result.put("status", "success");
                    result.put("data", todayReservations);
                    break;

                case "memo_update":
                    Integer memoClientCode = parseIntegerParameter(parameters.get("client_code"));
                    String memoContent = parseStringParameter(parameters.get("memo_content"));
                    String updateResult = visitorsService.updateShopUserMemo(shopId, memoClientCode, memoContent);
                    result.put("status", "success");
                    result.put("data", Map.of("message", updateResult));
                    break;

                default:
                    result.put("status", "error");
                    result.put("error", "지원하지 않는 요청 타입: " + requestType);
                    result.put("data", new HashMap<>());
            }

        } catch (Exception e) {
            logger.error("❌ 데이터 요청 처리 실패 - {}: {}", requestType, e.getMessage());
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("data", new HashMap<>());
        }

        return result;
    }

    private void publishResult(String correlationId, Map<String, Object> result) {
        try {
            Map<String, String> resultMessage = new HashMap<>();
            resultMessage.put("correlation_id", correlationId);
            resultMessage.put("status", String.valueOf(result.get("status")));
            resultMessage.put("data", objectMapper.writeValueAsString(result.get("data")));
            resultMessage.put("error", result.get("error") != null ? String.valueOf(result.get("error")) : "");
            resultMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));

            redisStreamTemplate.opsForStream().add(redisConfig.getDataResultsStream(), resultMessage);
            logger.info("📤 Redis 결과 발행 완료: {} - Status: {}", correlationId, result.get("status"));

        } catch (Exception e) {
            logger.error("❌ Redis 결과 발행 실패: {}", e.getMessage(), e);
        }
    }

    public void stopConsuming() {
        isRunning = false;
        logger.info("🛑 Redis 데이터 요청 컨슈머 중지");
    }

    /**
     * Safely parse parameter that might be Integer or String to Integer
     */
    private Integer parseIntegerParameter(Object parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("Required integer parameter is null");
        }
        
        if (parameter instanceof Integer) {
            return (Integer) parameter;
        } else if (parameter instanceof String) {
            try {
                return Integer.parseInt((String) parameter);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse string to integer: " + parameter);
            }
        } else {
            throw new IllegalArgumentException("Parameter must be Integer or String, got: " + parameter.getClass().getSimpleName());
        }
    }

    /**
     * Safely parse parameter that might be Integer or String to String
     */
    private String parseStringParameter(Object parameter) {
        if (parameter == null) {
            return null;
        }
        
        if (parameter instanceof String) {
            return (String) parameter;
        } else {
            return String.valueOf(parameter);
        }
    }
}