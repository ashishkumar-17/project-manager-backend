package com.projectmanager.service.Impl;

import com.projectmanager.entity.IdCounter;
import com.projectmanager.repository.IdCounterRepository;
import com.projectmanager.service.IdGenerationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class IdGenerationServiceImpl implements IdGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(IdGenerationServiceImpl.class);

    @Autowired
    private IdCounterRepository counterRepository;

    // Entity name constants for consistency
    private static final String USER_ENTITY = "User";
    private static final String PROJECT_ENTITY = "Project";
    private static final String TASK_ENTITY = "Task";
    private static final String FILE_ENTITY = "FileItem";
    private static final String TIME_ENTRY_ENTITY = "TimeEntry";

    /**
     * Generate next sequential ID for any entity
     * This method is synchronized to handle concurrent requests safely
     */
    public synchronized String generateId(String entityName, String prefix) {
        try {
            // Check if counter exists, create if not
            if (!counterRepository.existsByEntityName(entityName)) {
                createCounterForEntity(entityName);
            }

            // Increment counter atomically
            counterRepository.incrementCounter(entityName);

            // Get the new counter value
            Long counter = counterRepository.getCurrentCounter(entityName);

            if (counter == null) {
                throw new RuntimeException("Failed to get counter value for entity: " + entityName);
            }

            String generatedId = prefix + "-" + (counter + 1);
            logger.debug("Generated ID: {} for entity: {}", generatedId, entityName);

            return generatedId;

        } catch (Exception e) {
            logger.error("Error generating ID for entity: {} with prefix: {}", entityName, prefix, e);
            throw new RuntimeException("Failed to generate ID for " + entityName, e);
        }
    }

    /**
     * Generate multiple IDs at once for batch operations
     */
    public synchronized List<String> generateBatchIds(String entityName, String prefix, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }

        try {
            // Check if counter exists, create if not
            if (!counterRepository.existsByEntityName(entityName)) {
                createCounterForEntity(entityName);
            }

            // Get current counter value
            Long currentValue = counterRepository.getCurrentCounter(entityName);
            if (currentValue == null) {
                currentValue = 0L;
            }

            // Increment counter by the count
            counterRepository.incrementCounterBy(entityName, (long) count);

            // Generate IDs
            List<String> ids = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                ids.add(prefix + "-" + (currentValue + i));
            }

            logger.debug("Generated {} IDs for entity: {} starting from: {}",
                    count, entityName, prefix + "-" + (currentValue + 1));

            return ids;

        } catch (Exception e) {
            logger.error("Error generating batch IDs for entity: {} with prefix: {}, count: {}",
                    entityName, prefix, count, e);
            throw new RuntimeException("Failed to generate batch IDs for " + entityName, e);
        }
    }

    /**
     * Create counter entry for new entity type
     */
    private void createCounterForEntity(String entityName) {
        try {
            IdCounter counter = new IdCounter(entityName, 0L);
            counterRepository.save(counter);
            logger.info("Created counter for new entity: {}", entityName);
        } catch (Exception e) {
            logger.error("Error creating counter for entity: {}", entityName, e);
            throw new RuntimeException("Failed to create counter for " + entityName, e);
        }
    }

    // Specific methods for each entity type
    public String generateUserId() {
        return generateId(USER_ENTITY, "user");
    }

    public String generateProjectId() {
        return generateId(PROJECT_ENTITY, "proj");
    }

    public String generateTaskId() {
        return generateId(TASK_ENTITY, "task");
    }

    public String generateFileId() {
        return generateId(FILE_ENTITY, "file");
    }

    public String generateTimeEntryId() {
        return generateId(TIME_ENTRY_ENTITY, "time");
    }

    // Batch generation methods
    public List<String> generateUserIds(int count) {
        return generateBatchIds(USER_ENTITY, "user", count);
    }

    public List<String> generateProjectIds(int count) {
        return generateBatchIds(PROJECT_ENTITY, "proj", count);
    }

    public List<String> generateTaskIds(int count) {
        return generateBatchIds(TASK_ENTITY, "task", count);
    }

    public List<String> generateFileIds(int count) {
        return generateBatchIds(FILE_ENTITY, "file", count);
    }

    public List<String> generateTimeEntryIds(int count) {
        return generateBatchIds(TIME_ENTRY_ENTITY, "time", count);
    }

    // Utility methods
    public Long getCurrentCounterValue(String entityName) {
        return counterRepository.getCurrentCounter(entityName);
    }

    public void resetCounter(String entityName, Long value) {
        counterRepository.resetCounter(entityName, value);
        logger.info("Reset counter for entity: {} to value: {}", entityName, value);
    }

    // Reset methods for each entity (useful for testing)
    public void resetUserCounter(Long value) {
        resetCounter(USER_ENTITY, value);
    }

    public void resetProjectCounter(Long value) {
        resetCounter(PROJECT_ENTITY, value);
    }

    public void resetTaskCounter(Long value) {
        resetCounter(TASK_ENTITY, value);
    }

    public void resetFileCounter(Long value) {
        resetCounter(FILE_ENTITY, value);
    }

    public void resetTimeEntryCounter(Long value) {
        resetCounter(TIME_ENTRY_ENTITY, value);
    }
}
