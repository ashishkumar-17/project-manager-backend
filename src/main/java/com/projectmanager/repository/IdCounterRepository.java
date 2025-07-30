package com.projectmanager.repository;

import com.projectmanager.entity.IdCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IdCounterRepository extends JpaRepository<IdCounter, String> {

    /**
     * Atomically increment counter and return new value
     * This uses native SQL for better performance and thread safety
     */
    @Modifying
    @Query(value = "UPDATE id_counters SET counter_value = counter_value + 1 WHERE entity_name = :entityName",
            nativeQuery = true)
    void incrementCounter(@Param("entityName") String entityName);

    /**
     * Get current counter value
     */
    @Query("SELECT c.counterValue FROM IdCounter c WHERE c.entityName = :entityName")
    Long getCurrentCounter(@Param("entityName") String entityName);

    /**
     * Increment counter by a specific amount (for batch operations)
     */
    @Modifying
    @Query(value = "UPDATE id_counters SET counter_value = counter_value + :increment WHERE entity_name = :entityName",
            nativeQuery = true)
    void incrementCounterBy(@Param("entityName") String entityName, @Param("increment") Long increment);

    /**
     * Reset counter to a specific value (useful for testing or data migration)
     */
    @Modifying
    @Query(value = "UPDATE id_counters SET counter_value = :value WHERE entity_name = :entityName",
            nativeQuery = true)
    void resetCounter(@Param("entityName") String entityName, @Param("value") Long value);

    /**
     * Check if counter exists for entity
     */
    @Query("SELECT COUNT(c) > 0 FROM IdCounter c WHERE c.entityName = :entityName")
    boolean existsByEntityName(@Param("entityName") String entityName);
}