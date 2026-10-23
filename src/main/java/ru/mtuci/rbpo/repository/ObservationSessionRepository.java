package ru.mtuci.rbpo.repository;

import java.util.List;
import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import ru.mtuci.rbpo.model.ObservationSession;
import ru.mtuci.rbpo.model.SessionStatus;

public interface ObservationSessionRepository extends JpaRepository<ObservationSession, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ObservationSession s where s.id = :id")
    java.util.Optional<ObservationSession> findByIdForUpdate(@Param("id") Long id);

    List<ObservationSession> findByCelestialObjectId(Long celestialObjectId);

    List<ObservationSession> findByTelescopeId(Long telescopeId);

    List<ObservationSession> findByObserverId(Long observerId);

    boolean existsByTelescopeIdAndStatus(Long telescopeId, SessionStatus status);

    @Query("""
            select (count(s) > 0) from ObservationSession s
            where s.telescopeId = :telescopeId
              and s.status <> :cancelledStatus
              and s.id <> :excludedId
              and s.startTime < :endTime
              and :startTime < s.endTime
            """)
    boolean hasOverlap(@Param("telescopeId") Long telescopeId,
                       @Param("startTime") Instant startTime,
                       @Param("endTime") Instant endTime,
                       @Param("excludedId") Long excludedId,
                       @Param("cancelledStatus") SessionStatus cancelledStatus);
}
