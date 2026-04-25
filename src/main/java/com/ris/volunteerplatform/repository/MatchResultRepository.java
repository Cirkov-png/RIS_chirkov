package com.ris.volunteerplatform.repository;

import com.ris.volunteerplatform.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    List<MatchResult> findByTaskIdOrderByMatchScoreDesc(Long taskId);

    void deleteByTaskId(Long taskId);
}
