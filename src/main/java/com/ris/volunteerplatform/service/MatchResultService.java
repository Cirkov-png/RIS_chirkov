package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.MatchResultCreateRequest;
import com.ris.volunteerplatform.dto.MatchResultDto;
import com.ris.volunteerplatform.dto.MatchResultUpdateRequest;
import com.ris.volunteerplatform.entity.MatchResult;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.MatchResultRepository;
import com.ris.volunteerplatform.repository.TaskRepository;
import com.ris.volunteerplatform.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD по сохранённым результатам матчинга (обычно заполняются {@link MatchingService}).
 */
@Service
@RequiredArgsConstructor
public class MatchResultService {

    private final MatchResultRepository matchResultRepository;
    private final TaskRepository taskRepository;
    private final VolunteerRepository volunteerRepository;

    @Transactional(readOnly = true)
    public List<MatchResultDto> findAll() {
        return matchResultRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public MatchResultDto findById(Long id) {
        return matchResultRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Результат матчинга не найден: " + id));
    }

    @Transactional(readOnly = true)
    public List<MatchResultDto> findByTaskId(Long taskId) {
        return matchResultRepository.findByTaskIdOrderByMatchScoreDesc(taskId).stream().map(this::toDto).toList();
    }

    @Transactional
    public MatchResultDto create(MatchResultCreateRequest req) {
        var task = taskRepository.findById(req.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + req.taskId()));
        var volunteer = volunteerRepository.findById(req.volunteerId())
                .orElseThrow(() -> new ResourceNotFoundException("Волонтёр не найден: " + req.volunteerId()));
        MatchResult mr = MatchResult.builder()
                .task(task)
                .volunteer(volunteer)
                .matchScore(req.matchScore())
                .build();
        return toDto(matchResultRepository.save(mr));
    }

    @Transactional
    public MatchResultDto update(Long id, MatchResultUpdateRequest req) {
        MatchResult mr = matchResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Результат матчинга не найден: " + id));
        mr.setMatchScore(req.matchScore());
        return toDto(matchResultRepository.save(mr));
    }

    @Transactional
    public void delete(Long id) {
        if (!matchResultRepository.existsById(id)) {
            throw new ResourceNotFoundException("Результат матчинга не найден: " + id);
        }
        matchResultRepository.deleteById(id);
    }

    private MatchResultDto toDto(MatchResult mr) {
        return new MatchResultDto(
                mr.getId(),
                mr.getTask().getId(),
                mr.getVolunteer().getId(),
                mr.getMatchScore(),
                mr.getComputedAt());
    }
}
