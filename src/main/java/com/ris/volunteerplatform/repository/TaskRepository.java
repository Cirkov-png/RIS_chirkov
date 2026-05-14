package com.ris.volunteerplatform.repository;

import com.ris.volunteerplatform.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOrganizerId(Long organizerId);

    Optional<Task> findByUuid(UUID uuid);
}
