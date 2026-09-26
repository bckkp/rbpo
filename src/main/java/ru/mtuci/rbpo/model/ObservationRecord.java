package ru.mtuci.rbpo.model;

import java.time.Instant;

/** Наблюдательная запись в рамках сеанса. */
public class ObservationRecord extends BaseEntity {

    private Long sessionId;
    /** Время записи, задаётся сервером. */
    private Instant recordedAt;
    private String description;
    /** Оценка качества наблюдения, 1..5. */
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
