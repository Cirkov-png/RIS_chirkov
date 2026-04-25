package com.ris.volunteerplatform.entity;

/** Статус заявки волонтёра на участие в задаче. */
public enum ApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN,
    /** Организатор закрыл заявку: задача выполнена успешно */
    COMPLETED_SUCCESS,
    /** Организатор закрыл заявку: задача не была выполнена */
    COMPLETED_FAILURE
}
