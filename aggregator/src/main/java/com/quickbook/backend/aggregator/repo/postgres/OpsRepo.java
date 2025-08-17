package com.quickbook.backend.aggregator.repo.postgres;

import com.quickbook.backend.aggregator.entity.postgres.Ops;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OpsRepo extends JpaRepository<Ops, String> {

    Page<Ops> findByDocId(@Param("docId") String docId, Pageable pageable);
    Page<Ops> findByDocIdAndUserId(@Param("docId") String docId, @Param("userId") String userId, Pageable pageable);

    @Query("SELECT DISTINCT o.userId FROM Ops o WHERE o.docId = :docId")
    List<String> findDistinctUserIdsByDocId(@Param("docId") String docId);

    @Query("SELECT DISTINCT o.docId FROM Ops o")
    List<String> findDistinctDoc();

    Page<Ops> findByDocIdAndTsLessThanEqual(@Param("docId") String docId, @Param("ts") Instant ts, Pageable pageable);
    Page<Ops> findByDocIdAndTsGreaterThan(@Param("docId") String docId, @Param("ts") Instant ts, Pageable pageable);


}
