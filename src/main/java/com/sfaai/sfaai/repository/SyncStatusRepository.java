package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing synchronization status records
 */
@Repository
public interface SyncStatusRepository extends JpaRepository<SyncStatus, Long> {

    /**
     * Find the most recent sync status entry by sync type
     * @param syncType The type of synchronization
     * @return The most recent sync status entry
     */
    SyncStatus findTopBySyncTypeOrderByStartTimeDesc(String syncType);

    /**
     * Count the number of successful syncs of a particular type
     * @param syncType The type of synchronization
     * @param success Whether the sync was successful
     * @return Count of successful syncs
     */
    long countBySyncTypeAndSuccess(String syncType, boolean success);
}
