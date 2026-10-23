package ru.mtuci.rbpo.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Наблюдательная запись в рамках сеанса. */
@Entity
@Table(name = "observation_records")
public class ObservationRecord extends BaseEntity {

    @Column(name = "session_id", nullable = false)
    private Long sessionId;
    /** Время записи, задаётся сервером. */
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @Column(nullable = false, length = 1000)
    private String description;
    /** Оценка качества наблюдения, 1..5. */
    @Column(nullable = false)
    private Integer quality;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }
}
