package ru.mtuci.rbpo.model;

import java.time.Instant;

/** Сеанс наблюдения: какой объект, на каком телескопе, кто наблюдает и когда. */
public class ObservationSession extends BaseEntity {

    private Long celestialObjectId;
    private Long telescopeId;
    private Long observerId;
    private Instant startTime;
    private Instant endTime;
    private SessionStatus status;
    /** Время создания сеанса, задаётся сервером. */
    private Instant createdAt;

    public Long getCelestialObjectId() {
        return celestialObjectId;
    }

    public void setCelestialObjectId(Long celestialObjectId) {
        this.celestialObjectId = celestialObjectId;
    }

    public Long getTelescopeId() {
        return telescopeId;
    }

    public void setTelescopeId(Long telescopeId) {
        this.telescopeId = telescopeId;
    }

    public Long getObserverId() {
        return observerId;
    }

    public void setObserverId(Long observerId) {
        this.observerId = observerId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
