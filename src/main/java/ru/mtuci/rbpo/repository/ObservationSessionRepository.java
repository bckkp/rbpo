package ru.mtuci.rbpo.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import ru.mtuci.rbpo.model.ObservationSession;

@Repository
public class ObservationSessionRepository extends InMemoryRepository<ObservationSession> {

    public List<ObservationSession> findByCelestialObjectId(Long celestialObjectId) {
        return findAllMatching(s -> celestialObjectId.equals(s.getCelestialObjectId()));
    }

    public List<ObservationSession> findByTelescopeId(Long telescopeId) {
        return findAllMatching(s -> telescopeId.equals(s.getTelescopeId()));
    }

    public List<ObservationSession> findByObserverId(Long observerId) {
        return findAllMatching(s -> observerId.equals(s.getObserverId()));
    }
}
