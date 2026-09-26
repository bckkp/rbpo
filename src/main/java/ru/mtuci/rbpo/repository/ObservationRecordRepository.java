package ru.mtuci.rbpo.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import ru.mtuci.rbpo.model.ObservationRecord;

@Repository
public class ObservationRecordRepository extends InMemoryRepository<ObservationRecord> {

    public List<ObservationRecord> findBySessionId(Long sessionId) {
        return findAllMatching(r -> sessionId.equals(r.getSessionId()));
    }
}
