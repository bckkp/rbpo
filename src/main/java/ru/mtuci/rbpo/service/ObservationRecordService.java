package ru.mtuci.rbpo.service;

import static ru.mtuci.rbpo.service.Checks.notBlank;
import static ru.mtuci.rbpo.service.Checks.required;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mtuci.rbpo.exception.BadRequestException;
import ru.mtuci.rbpo.exception.ConflictException;
import ru.mtuci.rbpo.exception.NotFoundException;
import ru.mtuci.rbpo.model.ObservationRecord;
import ru.mtuci.rbpo.model.ObservationSession;
import ru.mtuci.rbpo.model.SessionStatus;
import ru.mtuci.rbpo.repository.ObservationRecordRepository;
import ru.mtuci.rbpo.repository.ObservationSessionRepository;

@Service
@Transactional
public class ObservationRecordService {

    private static final int MIN_QUALITY = 1;
    private static final int MAX_QUALITY = 5;

    private final ObservationRecordRepository recordRepository;
    private final ObservationSessionRepository sessionRepository;

    public ObservationRecordService(ObservationRecordRepository recordRepository,
                                    ObservationSessionRepository sessionRepository) {
        this.recordRepository = recordRepository;
        this.sessionRepository = sessionRepository;
    }

    public List<ObservationRecord> findAll() {
        return recordRepository.findAll();
    }

    public ObservationRecord findById(Long id) {
        return recordRepository.findById(id).orElseThrow(() -> new NotFoundException("Наблюдательная запись", id));
    }

    public ObservationRecord create(ObservationRecord record) {
        record.setId(null);
        record.setRecordedAt(Instant.now());
        validate(record, true);
        return recordRepository.save(record);
    }

    public ObservationRecord update(Long id, ObservationRecord patch) {
        ObservationRecord record = findById(id);
        Long previousSessionId = record.getSessionId();
        boolean sessionChanged = patch.getSessionId() != null
                && !patch.getSessionId().equals(record.getSessionId());
        if (sessionChanged) {
            ensureCompletedSessionKeepsARecord(previousSessionId);
        }
        if (patch.getSessionId() != null) {
            record.setSessionId(patch.getSessionId());
        }
        if (patch.getDescription() != null) {
            record.setDescription(patch.getDescription());
        }
        if (patch.getQuality() != null) {
            record.setQuality(patch.getQuality());
        }
        validate(record, sessionChanged);
        return recordRepository.save(record);
    }

    public void delete(Long id) {
        ObservationRecord record = findById(id);
        ensureCompletedSessionKeepsARecord(record.getSessionId());
        recordRepository.deleteById(id);
    }

    /** checkSessionStatus: запрещать ли привязку к отменённому сеансу (при создании или смене сеанса). */
    private void validate(ObservationRecord record, boolean checkSessionStatus) {
        Long sessionId = required(record.getSessionId(), "sessionId");
        ObservationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BadRequestException("Сеанс наблюдения с id=" + sessionId + " не существует"));
        if (checkSessionStatus && session.getStatus() == SessionStatus.CANCELLED) {
            throw new BadRequestException("Нельзя добавлять записи к отменённому сеансу");
        }
        record.setDescription(notBlank(record.getDescription(), "description"));
        int quality = required(record.getQuality(), "quality");
        if (quality < MIN_QUALITY || quality > MAX_QUALITY) {
            throw new BadRequestException("Оценка качества должна быть от " + MIN_QUALITY + " до " + MAX_QUALITY);
        }
    }

    private void ensureCompletedSessionKeepsARecord(Long sessionId) {
        ObservationSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null
                && session.getStatus() == SessionStatus.COMPLETED
                && recordRepository.countBySessionId(sessionId) <= 1) {
            throw new ConflictException("Завершённый сеанс должен содержать хотя бы одну запись");
        }
    }
}
