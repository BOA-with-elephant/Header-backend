package com.header.header.common.controller;

import com.header.header.common.dto.response.ApiResponse;
import com.header.header.config.RedisStreamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/redis")
public class RedisTestController {
    // Note: Add /api/test/redis/** to Spring Security permitAll() if needed

    private static final Logger logger = LoggerFactory.getLogger(RedisTestController.class);

    @Autowired
    private RedisTemplate<String, String> redisStreamTemplate;

    @Autowired
    private RedisStreamConfig redisConfig;

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> testRedisConnection() {
        try {
            redisStreamTemplate.getConnectionFactory().getConnection().ping();
            return ResponseEntity.ok(ApiResponse.success("PONG"));
        } catch (Exception e) {
            logger.error("Redis 연결 테스트 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.fail("Redis 연결 실패: " + e.getMessage(), null));
        }
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();
        try {
            info.put("dataRequestsStream", redisConfig.getDataRequestsStream());
            info.put("dataResultsStream", redisConfig.getDataResultsStream());
            info.put("consumerGroup", redisConfig.getConsumerGroup());
            
            return ResponseEntity.ok(ApiResponse.success(info));
        } catch (Exception e) {
            info.put("error", e.getMessage());
            return ResponseEntity.ok(ApiResponse.fail("Redis 정보 조회 실패", info));
        }
    }

    @PostMapping("/create-stream/{streamName}")
    public ResponseEntity<ApiResponse<String>> createStream(@PathVariable String streamName) {
        try {
            Map<String, String> testMessage = new HashMap<>();
            testMessage.put("test", "stream_creation");
            testMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            RecordId messageId = redisStreamTemplate.opsForStream().add(streamName, testMessage);
            return ResponseEntity.ok(ApiResponse.success("Message ID: " + messageId));
        } catch (Exception e) {
            logger.error("스트림 생성 실패: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.fail("스트림 생성 실패: " + e.getMessage(), null));
        }
    }

    @PostMapping("/create-consumer-group")
    public ResponseEntity<ApiResponse<String>> createConsumerGroup(
            @RequestParam String streamName,
            @RequestParam String groupName) {
        try {
            // Try Method 1
            try {
                redisStreamTemplate.opsForStream().createGroup(streamName, ReadOffset.from("0"), groupName);
                return ResponseEntity.ok(ApiResponse.success(groupName));
            } catch (Exception e1) {
                logger.warn("Method 1 실패, Method 2 시도: {}", e1.getMessage());
                
                // Try Method 2
                redisStreamTemplate.opsForStream().createGroup(streamName, groupName);
                return ResponseEntity.ok(ApiResponse.success(groupName));
            }
        } catch (Exception e) {
            logger.error("컨슈머 그룹 생성 실패: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                return ResponseEntity.ok(ApiResponse.success(groupName + " (이미 존재)"));
            }
            return ResponseEntity.ok(ApiResponse.fail("컨슈머 그룹 생성 실패: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/check-streams")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStreams() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String[] streams = {redisConfig.getDataRequestsStream(), redisConfig.getDataResultsStream()};
            
            for (String streamName : streams) {
                try {
                    Long length = redisStreamTemplate.opsForStream().size(streamName);
                    result.put(streamName + "_exists", true);
                    result.put(streamName + "_length", length);
                } catch (Exception e) {
                    result.put(streamName + "_exists", false);
                    result.put(streamName + "_error", e.getMessage());
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.ok(ApiResponse.fail("스트림 상태 확인 실패", result));
        }
    }
}