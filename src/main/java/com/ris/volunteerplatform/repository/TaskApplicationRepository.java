package com.ris.volunteerplatform.repository;

import com.ris.volunteerplatform.entity.ApplicationStatus;
import com.ris.volunteerplatform.entity.TaskApplication;
import com.ris.volunteerplatform.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface TaskApplicationRepository extends JpaRepository<TaskApplication, Long> {
    List<TaskApplication> findByTaskId(Long taskId);

    List<TaskApplication> findByVolunteerId(Long volunteerId);

    List<TaskApplication> findByVolunteer(Volunteer volunteer);

    List<TaskApplication> findByVolunteerAndStatus(Volunteer volunteer, ApplicationStatus status);

    List<TaskApplication> findByVolunteerAndTaskId(Volunteer volunteer, Long taskId);

    List<TaskApplication> findByVolunteerAndStatusIn(Volunteer volunteer, Collection<ApplicationStatus> statuses);

    int countByVolunteerAndTaskId(Volunteer volunteer, Long taskId);

    long countByVolunteerAndStatus(Volunteer volunteer, ApplicationStatus status);

    boolean existsByTask_IdAndStatusIn(Long taskId, Collection<ApplicationStatus> statuses);

    void deleteByTask_Id(Long taskId);

    @Query("SELECT COALESCE(SUM(a.organizerRating), 0) FROM TaskApplication a WHERE a.volunteer.id = :vid AND a.organizerReviewedAt IS NOT NULL")
    BigDecimal sumOrganizerRatings(@Param("vid") Long volunteerId);

    @Query("SELECT COUNT(a) FROM TaskApplication a WHERE a.volunteer.id = :vid AND a.organizerReviewedAt IS NOT NULL")
    long countOrganizerRatings(@Param("vid") Long volunteerId);
}
