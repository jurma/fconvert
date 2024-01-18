package com.jurma.mintos.fconvert.repository;

import com.jurma.mintos.fconvert.entity.History;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends CrudRepository<History, Long> {
    @Query(value = "SELECT * FROM history h WHERE account_id = ?1 ORDER BY h.id LIMIT ?3 OFFSET ?2 ", nativeQuery = true)
    List<History> findByAccountIdWithOffsetLimitQuery(Long id, Long offset, Long limit);

    @Query(value = "SELECT * FROM history h WHERE account_id = ?1 ORDER BY h.id OFFSET ?2 ", nativeQuery = true)
    List<History> findByAccountIdWithOffsetQuery(Long id, Long offset);
}
