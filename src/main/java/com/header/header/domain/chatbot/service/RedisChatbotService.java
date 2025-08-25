package com.header.header.domain.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.header.header.config.RedisStreamConfig;
import com.header.header.domain.reservation.dto.ChatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class RedisChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(RedisChatbotService.class);
    private static final String CHATBOT_REQUEST_STREAM = "data-requests";
    private static final String CHATBOT_RESPONSE_STREAM = "data-results";
    private static final int RESPONSE_TIMEOUT_SECONDS = 30;

    @Qualifier("redisStreamTemplate")
    private final RedisTemplate<String, String> redisStreamTemplate;

    private final RedisStreamConfig redisConfig;

    private final ObjectMapper objectMapper;


    public ChatResponseDTO sendCustomerMessage(Integer shopCode, String message, String messageType) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        
        // Send message to FastAPI via Redis
        Map<String, String> redisMessage = new HashMap<>();
        redisMessage.put("correlation_id", correlationId);
        redisMessage.put("request_type", "customer_message");
        redisMessage.put("shop_code", shopCode.toString());
        redisMessage.put("message", message);
        redisMessage.put("message_type", messageType != null ? messageType : "general");
        redisMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));

        logger.info("📤 Sending customer message to FastAPI: {} - Shop: {}, Type: {}, Message: {}", 
                   correlationId, shopCode, messageType, message);
        
        redisStreamTemplate.opsForStream().add(CHATBOT_REQUEST_STREAM, redisMessage);

        // Wait for response
        return waitForResponse(correlationId);
    }

    private ChatResponseDTO waitForResponse(String correlationId) throws Exception {
        String consumerName = "spring-chatbot-" + System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        long timeoutMs = RESPONSE_TIMEOUT_SECONDS * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                List<MapRecord<String, Object, Object>> messages = redisStreamTemplate.opsForStream().read(
                    Consumer.from("chatbot-response-group", consumerName),
                    StreamReadOptions.empty().count(10).block(Duration.ofSeconds(1)),
                    StreamOffset.create(CHATBOT_RESPONSE_STREAM, ReadOffset.latest())
                );

                for (MapRecord<String, Object, Object> message : messages) {
                    String responseCorrelationId = (String) message.getValue().get("correlation_id");
                    
                    if (correlationId.equals(responseCorrelationId)) {
                        logger.info("📥 Received response from FastAPI: {}", correlationId);
                        
                        // Acknowledge the message
                        try {
                            redisStreamTemplate.opsForStream().acknowledge(
                                CHATBOT_RESPONSE_STREAM, 
                                "chatbot-response-group", 
                                message.getId()
                            );
                        } catch (Exception ackError) {
                            logger.warn("Failed to acknowledge message: {}", ackError.getMessage());
                        }
                        
                        return parseResponse(message);
                    }
                }
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("NOGROUP")) {
                    // Create consumer group if it doesn't exist
                    createConsumerGroupIfNotExists(CHATBOT_RESPONSE_STREAM, "chatbot-response-group");
                } else {
                    logger.warn("Error while waiting for response: {}", e.getMessage());
                }
            }
            
            // Brief pause to avoid overwhelming Redis
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new Exception("Request interrupted");
            }
        }

        throw new TimeoutException("No response received within " + RESPONSE_TIMEOUT_SECONDS + " seconds for correlation ID: " + correlationId);
    }

    private ChatResponseDTO parseResponse(MapRecord<String, Object, Object> message) throws Exception {
        String status = (String) message.getValue().get("status");
        String dataJson = (String) message.getValue().get("data");
        String error = (String) message.getValue().get("error");

        if ("error".equals(status)) {
            throw new Exception("FastAPI error: " + error);
        }

        if (dataJson != null && !dataJson.isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = objectMapper.readValue(dataJson, Map.class);
                
                ChatResponseDTO response = new ChatResponseDTO();
                response.setAnswer((String) data.get("answer"));
                response.setSessionId((String) data.get("session_id"));
                
                return response;
            } catch (Exception parseError) {
                logger.error("Failed to parse response data: {}", parseError.getMessage());
                throw new Exception("Failed to parse FastAPI response");
            }
        }

        throw new Exception("Empty or invalid response from FastAPI");
    }

    private void createConsumerGroupIfNotExists(String streamName, String groupName) {
        try {
            // Ensure stream exists first
            ensureStreamExists(streamName);
            
            // Try to create consumer group
            redisStreamTemplate.opsForStream().createGroup(streamName, ReadOffset.from("0"), groupName);
            logger.info("✅ Created consumer group: {} for stream: {}", groupName, streamName);
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("BUSYGROUP") || e.getMessage().contains("already exists"))) {
                logger.info("👥 Consumer group already exists: {} for stream: {}", groupName, streamName);
            } else {
                logger.warn("Failed to create consumer group {}: {}", groupName, e.getMessage());
            }
        }
    }

    private void ensureStreamExists(String streamName) {
        try {
            redisStreamTemplate.opsForStream().info(streamName);
        } catch (Exception e) {
            // Stream doesn't exist, create it with a dummy message
            Map<String, String> dummyMessage = new HashMap<>();
            dummyMessage.put("init", "stream_created");
            dummyMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            try {
                redisStreamTemplate.opsForStream().add(streamName, dummyMessage);
                logger.info("📝 Created Redis stream: {}", streamName);
            } catch (Exception createError) {
                logger.error("Failed to create stream {}: {}", streamName, createError.getMessage());
            }
        }
    }
}