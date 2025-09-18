package com.example.demo.common.repository;

import com.example.demo.common.exception.RecordNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface with common utility methods to reduce code duplication.
 * All entity repositories should extend this instead of JpaRepository directly.
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * Find entity by ID or throw RecordNotFoundException.
     * This eliminates the repeated pattern of:
     * repository.findById(id).orElseThrow(() -> new RecordNotFoundException("Entity", id))
     *
     * @param id the entity ID
     * @param entityName the entity name for error messages
     * @return the found entity
     * @throws RecordNotFoundException if entity not found
     */
    default T findByIdOrThrow(ID id, String entityName) {
        return findById(id)
                .orElseThrow(() -> new RecordNotFoundException(entityName, id));
    }

    /**
     * Find entity by ID or throw RecordNotFoundException with generic message.
     * Uses the repository's domain type name for the error message.
     *
     * @param id the entity ID
     * @return the found entity
     * @throws RecordNotFoundException if entity not found
     */
    default T findByIdOrThrow(ID id) {
        return findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Entity", id));
    }
}