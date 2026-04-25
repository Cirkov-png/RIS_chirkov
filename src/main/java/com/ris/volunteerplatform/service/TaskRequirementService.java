package com.ris.volunteerplatform.service;

import com.ris.volunteerplatform.dto.TaskRequirementDto;
import com.ris.volunteerplatform.dto.TaskRequirementRequest;
import com.ris.volunteerplatform.entity.TaskRequirement;
import com.ris.volunteerplatform.exception.ResourceNotFoundException;
import com.ris.volunteerplatform.repository.SkillRepository;
import com.ris.volunteerplatform.repository.TaskRepository;
import com.ris.volunteerplatform.repository.TaskRequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskRequirementService {

    private final TaskRequirementRepository taskRequirementRepository;
    private final TaskRepository taskRepository;
    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    public List<TaskRequirementDto> findAll() {
        return taskRequirementRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TaskRequirementDto findById(Long id) {
        return taskRequirementRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Требование не найдено: " + id));
    }

    @Transactional(readOnly = true)
    public List<TaskRequirementDto> findByTaskId(Long taskId) {
        return taskRequirementRepository.findByTaskId(taskId).stream().map(this::toDto).toList();
    }

    @Transactional
    public TaskRequirementDto create(TaskRequirementRequest req) {
        var task = taskRepository.findById(req.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + req.taskId()));
        var skill = skillRepository.findById(req.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Навык не найден: " + req.skillId()));
        TaskRequirement tr = TaskRequirement.builder()
                .task(task)
                .skill(skill)
                .importanceWeight(req.importanceWeight())
                .build();
        return toDto(taskRequirementRepository.save(tr));
    }

    @Transactional
    public TaskRequirementDto update(Long id, TaskRequirementRequest req) {
        TaskRequirement tr = taskRequirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Требование не найдено: " + id));
        tr.setTask(taskRepository.findById(req.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена: " + req.taskId())));
        tr.setSkill(skillRepository.findById(req.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Навык не найден: " + req.skillId())));
        tr.setImportanceWeight(req.importanceWeight());
        return toDto(taskRequirementRepository.save(tr));
    }

    @Transactional
    public void delete(Long id) {
        if (!taskRequirementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Требование не найдено: " + id);
        }
        taskRequirementRepository.deleteById(id);
    }

    private TaskRequirementDto toDto(TaskRequirement tr) {
        return new TaskRequirementDto(
                tr.getId(),
                tr.getTask().getId(),
                tr.getSkill().getId(),
                tr.getImportanceWeight(),
                tr.getSkill().getName());
    }
}
