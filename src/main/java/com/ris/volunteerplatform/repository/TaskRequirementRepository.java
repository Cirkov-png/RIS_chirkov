package com.ris.volunteerplatform.repository;

import com.ris.volunteerplatform.entity.TaskRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRequirementRepository extends JpaRepository<TaskRequirement, Long> {
    List<TaskRequirement> findByTaskId(Long taskId);

    void deleteByTaskId(Long taskId);
}
