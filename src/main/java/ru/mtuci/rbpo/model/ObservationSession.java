package ru.mtuci.rbpo.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Сеанс наблюдения: какой объект, на каком телескопе, кто наблюдает и когда. */
@Entity
@Table(name = "observation_sessions")
public class ObservationSession extends BaseEntity {

    @Column(name = "celestial_object_id", nullable = false)
    private Long celestialObjectId;

    @Column(name = "telescope_id", nullable = false)
    private Long telescopeId;

    @Column(name = "observer_id", nullable = false)
    private Long observerId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;
    /** Время создания сеанса, задаётся сервером. */
    @Column(name = "created_at", nullable = false, updatable = false)
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
