package ru.mtuci.rbpo.service;

import static ru.mtuci.rbpo.service.Checks.required;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import ru.mtuci.rbpo.exception.BadRequestException;
import ru.mtuci.rbpo.exception.ConflictException;
import ru.mtuci.rbpo.exception.NotFoundException;
import ru.mtuci.rbpo.model.CelestialObject;
import ru.mtuci.rbpo.model.ObservationSession;
import ru.mtuci.rbpo.model.SessionStatus;
import ru.mtuci.rbpo.model.Telescope;
import ru.mtuci.rbpo.repository.CelestialObjectRepository;
import ru.mtuci.rbpo.repository.ObservationRecordRepository;
import ru.mtuci.rbpo.repository.ObservationSessionRepository;
import ru.mtuci.rbpo.repository.TelescopeRepository;
import ru.mtuci.rbpo.repository.UserRepository;

@Service
public class ObservationSessionService {

    private final ObservationSessionRepository sessionRepository;
    private final ObservationRecordRepository recordRepository;
    private final CelestialObjectRepository celestialObjectRepository;
    private final TelescopeRepository telescopeRepository;
    private final UserRepository userRepository;

    public ObservationSessionService(ObservationSessionRepository sessionRepository,
                                     ObservationRecordRepository recordRepository,
                                     CelestialObjectRepository celestialObjectRepository,
                                     TelescopeRepository telescopeRepository,
                                     UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.celestialObjectRepository = celestialObjectRepository;
        this.telescopeRepository = telescopeRepository;
        this.userRepository = userRepository;
    }

    public List<ObservationSession> findAll() {
        return sessionRepository.findAll();
    }

    public ObservationSession findById(Long id) {
        return sessionRepository.findById(id).orElseThrow(() -> new NotFoundException("Сеанс наблюдения", id));
    }

    public ObservationSession create(ObservationSession session) {
        session.setId(null);
        session.setCreatedAt(Instant.now());
        if (session.getStatus() == null) {
            session.setStatus(SessionStatus.PLANNED);
        }
        validate(session, true);
        return sessionRepository.save(session);
    }

    public ObservationSession update(Long id, ObservationSession patch) {
        ObservationSession session = findById(id);
        boolean telescopeChanged = patch.getTelescopeId() != null
                && !patch.getTelescopeId().equals(session.getTelescopeId());
        if (patch.getCelestialObjectId() != null) {
            session.setCelestialObjectId(patch.getCelestialObjectId());
        }
        if (patch.getTelescopeId() != null) {
            session.setTelescopeId(patch.getTelescopeId());
        }
        if (patch.getObserverId() != null) {
            session.setObserverId(patch.getObserverId());
        }
        if (patch.getStartTime() != null) {
            session.setStartTime(patch.getStartTime());
        }
        if (patch.getEndTime() != null) {
            session.setEndTime(patch.getEndTime());
        }
        if (patch.getStatus() != null) {
            session.setStatus(patch.getStatus());
        }
        validate(session, telescopeChanged);
        return sessionRepository.save(session);
    }

    /** Удаляет сеанс вместе со всеми его наблюдательными записями. */
    public void delete(Long id) {
        findById(id);
        deleteWithRecords(id);
    }

    public void deleteAllByCelestialObjectId(Long celestialObjectId) {
        sessionRepository.findByCelestialObjectId(celestialObjectId).forEach(s -> deleteWithRecords(s.getId()));
    }

    public void deleteAllByTelescopeId(Long telescopeId) {
        sessionRepository.findByTelescopeId(telescopeId).forEach(s -> deleteWithRecords(s.getId()));
    }

    public void deleteAllByObserverId(Long observerId) {
        sessionRepository.findByObserverId(observerId).forEach(s -> deleteWithRecords(s.getId()));
    }

    private void deleteWithRecords(Long sessionId) {
        recordRepository.findBySessionId(sessionId).forEach(r -> recordRepository.deleteById(r.getId()));
        sessionRepository.deleteById(sessionId);
    }

    private void validate(ObservationSession session, boolean checkTelescopeAvailability) {
        Long objectId = required(session.getCelestialObjectId(), "celestialObjectId");
        Long telescopeId = required(session.getTelescopeId(), "telescopeId");
        Long observerId = required(session.getObserverId(), "observerId");
        Instant start = required(session.getStartTime(), "startTime");
        Instant end = required(session.getEndTime(), "endTime");

        CelestialObject object = celestialObjectRepository.findById(objectId)
                .orElseThrow(() -> new BadRequestException("Небесный объект с id=" + objectId + " не существует"));
        Telescope telescope = telescopeRepository.findById(telescopeId)
                .orElseThrow(() -> new BadRequestException("Телескоп с id=" + telescopeId + " не существует"));
        if (!userRepository.existsById(observerId)) {
            throw new BadRequestException("Пользователь с id=" + observerId + " не существует");
        }

        if (!end.isAfter(start)) {
            throw new BadRequestException("endTime должно быть позже startTime");
        }
        if (telescope.getAperture() < object.getMinAperture()) {
            throw new BadRequestException("Апертуры телескопа '%s' (%s мм) недостаточно для объекта '%s' (нужно от %s мм)"
                    .formatted(telescope.getName(), telescope.getAperture(), object.getName(), object.getMinAperture()));
        }
        if (session.getStatus() == SessionStatus.CANCELLED) {
            return;
        }
        if (checkTelescopeAvailability && !telescope.getAvailable()) {
            throw new ConflictException("Телескоп '" + telescope.getName() + "' сейчас недоступен");
        }
        boolean overlaps = sessionRepository.findByTelescopeId(telescopeId).stream()
                .filter(other -> !Objects.equals(other.getId(), session.getId()))
                .filter(other -> other.getStatus() != SessionStatus.CANCELLED)
                .anyMatch(other -> other.getStartTime().isBefore(end) && start.isBefore(other.getEndTime()));
        if (overlaps) {
            throw new ConflictException("Телескоп '" + telescope.getName() + "' уже занят в этом интервале");
        }
    }
}
