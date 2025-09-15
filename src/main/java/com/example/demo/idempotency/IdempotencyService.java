package com.example.demo.idempotency;

import com.example.demo.common.exception.IdempotencyException;
import com.example.demo.user.UserEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Slf4j
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRepository idempotencyRepository, ObjectMapper objectMapper) {
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public <T> T executeIdempotent(String idempotencyKey, UserEntity user, Object request,
                                   Supplier<T> operation, Class<T> responseType) throws IdempotencyException {
        // Check if we already have a record for this idempotency key
        Optional<IdempotencyRecord> existingRecord = idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if (existingRecord.isPresent()) {
            IdempotencyRecord record = existingRecord.get();

            // Validate request hasn't changed
            String requestHash = generateHash(request);
            if (!record.getRequestHash().equals(requestHash)) {
                throw new IdempotencyException("Idempotency key reused with different request data");
            }

            // Check if expired
            if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new IdempotencyException("Idempotency key has expired");
            }

            // Return cached response if completed
            if (record.getStatus() == IdempotencyStatus.COMPLETED) {
                try {
                    return objectMapper.readValue(record.getResponseData(), responseType);
                } catch (JsonProcessingException e) {
                    log.error("Failed to deserialize cached response for key {}: {}", idempotencyKey, e.getMessage());
                    throw new IdempotencyException("Failed to process cached response", e);
                }
            }

            // If still processing, return error
            if (record.getStatus() == IdempotencyStatus.PROCESSING) {
                throw new IdempotencyException("Request is already being processed");
            }

            // If failed, allow retry by deleting the record
            if (record.getStatus() == IdempotencyStatus.FAILED) {
                idempotencyRepository.delete(record);
            }
        }

        // Create new idempotency record
        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setUser(user);
        record.setRequestHash(generateHash(request));
        record.setResponseData(""); // Will be updated after successful operation
        record.setStatus(IdempotencyStatus.PROCESSING);

        IdempotencyRecord savedRecord = idempotencyRepository.save(record);
        log.info("Created idempotency record for key: {}", idempotencyKey);

        try {
            // Execute the operation
            T result = operation.get();

            // Update record with success
            savedRecord.setResponseData(serializeResponse(result));
            savedRecord.setStatus(IdempotencyStatus.COMPLETED);
            idempotencyRepository.save(savedRecord);

            log.info("Completed idempotent operation for key: {}", idempotencyKey);
            return result;

        } catch (Exception e) {
            // Update record with failure
            savedRecord.setStatus(IdempotencyStatus.FAILED);
            savedRecord.setResponseData(e.getMessage());
            idempotencyRepository.save(savedRecord);

            log.error("Failed idempotent operation for key {}: {}", idempotencyKey, e.getMessage());
            throw e;
        }
    }

    private String generateHash(Object request) {
        try {
            String requestJson = objectMapper.writeValueAsString(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(requestJson.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate request hash", e);
        }
    }

    private String serializeResponse(Object response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize response", e);
        }
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredRecords() {
        int deletedCount = idempotencyRepository.deleteExpiredRecords(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired idempotency records", deletedCount);
        }
    }
}