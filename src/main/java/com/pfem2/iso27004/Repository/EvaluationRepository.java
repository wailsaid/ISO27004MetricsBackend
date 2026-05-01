package com.pfem2.iso27004.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pfem2.iso27004.Entity.EvalStatus;
import com.pfem2.iso27004.Entity.Evaluation;
import com.pfem2.iso27004.Entity.RagStatus;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    @Query("SELECT e FROM Evaluation e WHERE e.evaluationDate = (SELECT MAX(e2.evaluationDate) FROM Evaluation e2 WHERE e2.indicator.id = e.indicator.id)")
    List<Evaluation> findLatestEvaluations();

    @Query("SELECT e FROM Evaluation e WHERE e.indicator.id = :indicatorId ORDER BY e.evaluationDate DESC")
    List<Evaluation> findAllEvaluationsByIndicatorId(@Param("indicatorId") Long indicatorId);

    @Query("SELECT e FROM Evaluation e WHERE e.indicator.id = :indicatorId AND e.evaluationDate = (SELECT MAX(e2.evaluationDate) FROM Evaluation e2 WHERE e2.indicator.id = :indicatorId)")
    Evaluation findLatestEvaluationByIndicatorId(@Param("indicatorId") Long indicatorId);

    @Modifying
    @Query("DELETE FROM Evaluation e WHERE e.indicator.id = :indicatorId")
    void deleteAllEvaluationsByIndicatorId(@Param("indicatorId") Long indicatorId);

    @Query("SELECT e FROM Evaluation e WHERE e.indicator.checked=true AND e.evaluationDate = (SELECT MAX(e2.evaluationDate) FROM Evaluation e2 WHERE e2.indicator.id = e.indicator.id)")
    List<Evaluation> findDashboardIndicator();

    @Modifying
    @Query("DELETE FROM Evaluation e WHERE e.resp.id = :uid")
    void deletebyResp(@Param("uid") Long id);

    // Phase 2: verification workflow queries
    @Query("SELECT e FROM Evaluation e WHERE e.evalStatus = :status ORDER BY e.evaluationDate DESC")
    List<Evaluation> findByEvalStatus(@Param("status") EvalStatus status);

    // Phase 3: RAG queries on latest evaluations per indicator
    @Query("SELECT e FROM Evaluation e WHERE e.ragStatus = :ragStatus AND e.evaluationDate = (SELECT MAX(e2.evaluationDate) FROM Evaluation e2 WHERE e2.indicator.id = e.indicator.id)")
    List<Evaluation> findLatestEvaluationsByRagStatus(@Param("ragStatus") RagStatus ragStatus);

    // Phase 5: overdue — latest evaluation per indicator where nextEvaluationDate is in the past
    @Query("SELECT e FROM Evaluation e WHERE e.nextEvaluationDate < CURRENT_TIMESTAMP AND e.evaluationDate = (SELECT MAX(e2.evaluationDate) FROM Evaluation e2 WHERE e2.indicator.id = e.indicator.id)")
    List<Evaluation> findOverdueLatestEvaluations();
}
