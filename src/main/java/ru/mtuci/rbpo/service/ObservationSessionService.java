package ru.mtuci.rbpo.service;

import static ru.mtuci.rbpo.service.Checks.required;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
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
        } else if (session.getStatus() != SessionStatus.PLANNED) {
            throw new BadRequestException("Новый сеанс может иметь только статус PLANNED");
        }
        validate(session, true);
        return sessionRepository.save(session);
    }

    public ObservationSession update(Long id, ObservationSession patch) {
        ObservationSession session = findById(id);
        if (patch.getStatus() != null && patch.getStatus() != session.getStatus()) {
            throw new BadRequestException("Статус сеанса изменяется бизнес-операциями start, complete и cancel");
        }
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
        validate(session, true);
        return sessionRepository.save(session);
    }

    /** Начинает запланированный сеанс и занимает телескоп. */
    public ObservationSession start(Long id) {
        ObservationSession session = findByIdForUpdate(id);
        requireStatus(session, SessionStatus.PLANNED, "начать");
        Telescope telescope = findTelescopeForUpdate(session.getTelescopeId());
        if (!telescope.getAvailable()) {
            throw new ConflictException("Телескоп '" + telescope.getName() + "' недоступен");
        }
        telescope.setAvailable(false);
        session.setStatus(SessionStatus.IN_PROGRESS);
        telescopeRepository.save(telescope);
        return sessionRepository.save(session);
    }

    /** Завершает текущий сеанс и освобождает телескоп. */
    public ObservationSession complete(Long id) {
        ObservationSession session = findByIdForUpdate(id);
        requireStatus(session, SessionStatus.IN_PROGRESS, "завершить");
        if (!recordRepository.existsBySessionId(id)) {
            throw new ConflictException("Нельзя завершить сеанс без наблюдательных записей");
        }
        Telescope telescope = findTelescopeForUpdate(session.getTelescopeId());
        telescope.setAvailable(true);
        session.setStatus(SessionStatus.COMPLETED);
        telescopeRepository.save(telescope);
        return sessionRepository.save(session);
    }

    /** Отменяет запланированный или текущий сеанс; занятый телескоп освобождается. */
    public ObservationSession cancel(Long id) {
        ObservationSession session = findByIdForUpdate(id);
        if (session.getStatus() == SessionStatus.COMPLETED || session.getStatus() == SessionStatus.CANCELLED) {
            throw new ConflictException("Нельзя отменить сеанс в статусе " + session.getStatus());
        }
        if (session.getStatus() == SessionStatus.IN_PROGRESS) {
            Telescope telescope = findTelescopeForUpdate(session.getTelescopeId());
            telescope.setAvailable(true);
            telescopeRepository.save(telescope);
        }
        session.setStatus(SessionStatus.CANCELLED);
        return sessionRepository.save(session);
    }

    /** Переносит запланированный сеанс на другой интервал с проверкой расписания. */
    public ObservationSession reschedule(Long id, Instant startTime, Instant endTime) {
        ObservationSession session = findByIdForUpdate(id);
        requireStatus(session, SessionStatus.PLANNED, "перенести");
        Instant newStart = required(startTime, "startTime");
        Instant newEnd = required(endTime, "endTime");
        if (newStart.equals(session.getStartTime()) && newEnd.equals(session.getEndTime())) {
            throw new BadRequestException("Новый интервал совпадает с текущим");
        }
        session.setStartTime(newStart);
        session.setEndTime(newEnd);
        validate(session, true);
        return sessionRepository.save(session);
    }

    /** Назначает запланированному сеансу другой подходящий свободный телескоп. */
    public ObservationSession changeTelescope(Long id, Long telescopeId) {
        ObservationSession session = findByIdForUpdate(id);
        requireStatus(session, SessionStatus.PLANNED, "сменить телескоп для");
        Long newTelescopeId = required(telescopeId, "telescopeId");
        if (newTelescopeId.equals(session.getTelescopeId())) {
            throw new BadRequestException("Этот телескоп уже назначен сеансу");
        }
        session.setTelescopeId(newTelescopeId);
        validate(session, true);
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
        ObservationSession session = findByIdForUpdate(sessionId);
        if (session.getStatus() == SessionStatus.IN_PROGRESS) {
            Telescope telescope = findTelescopeForUpdate(session.getTelescopeId());
            telescope.setAvailable(true);
            telescopeRepository.save(telescope);
        }
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
        Telescope telescope = telescopeRepository.findByIdForUpdate(telescopeId)
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
        Long excludedId = session.getId() == null ? -1L : session.getId();
        if (sessionRepository.hasOverlap(telescopeId, start, end, excludedId, SessionStatus.CANCELLED)) {
            throw new ConflictException("Телескоп '" + telescope.getName() + "' уже занят в этом интервале");
        }
    }

    private ObservationSession findByIdForUpdate(Long id) {
        return sessionRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Сеанс наблюдения", id));
    }

    private Telescope findTelescopeForUpdate(Long id) {
        return telescopeRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Телескоп", id));
    }

    private void requireStatus(ObservationSession session, SessionStatus expected, String operation) {
        if (session.getStatus() != expected) {
            throw new ConflictException("Нельзя " + operation + " сеанс в статусе " + session.getStatus());
        }
    }
}
