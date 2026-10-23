package ru.mtuci.rbpo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.mtuci.rbpo.model.ObservationRecord;

public interface ObservationRecordRepository extends JpaRepository<ObservationRecord, Long> {

    List<ObservationRecord> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    long countBySessionId(Long sessionId);
}
