package ru.mtuci.rbpo.service;

import static ru.mtuci.rbpo.service.Checks.notBlank;
import static ru.mtuci.rbpo.service.Checks.positive;
import static ru.mtuci.rbpo.service.Checks.required;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mtuci.rbpo.exception.ConflictException;
import ru.mtuci.rbpo.exception.NotFoundException;
import ru.mtuci.rbpo.model.CelestialObject;
import ru.mtuci.rbpo.model.SessionStatus;
import ru.mtuci.rbpo.repository.CelestialObjectRepository;
import ru.mtuci.rbpo.repository.ObservationSessionRepository;
import ru.mtuci.rbpo.repository.TelescopeRepository;

@Service
@Transactional
public class CelestialObjectService {

    private final CelestialObjectRepository celestialObjectRepository;
    private final ObservationSessionRepository sessionRepository;
    private final TelescopeRepository telescopeRepository;
    private final ObservationSessionService sessionService;

    public CelestialObjectService(CelestialObjectRepository celestialObjectRepository,
                                  ObservationSessionRepository sessionRepository,
                                  TelescopeRepository telescopeRepository,
                                  ObservationSessionService sessionService) {
        this.celestialObjectRepository = celestialObjectRepository;
        this.sessionRepository = sessionRepository;
        this.telescopeRepository = telescopeRepository;
        this.sessionService = sessionService;
    }

    public List<CelestialObject> findAll() {
        return celestialObjectRepository.findAll();
    }

    public CelestialObject findById(Long id) {
        return celestialObjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Небесный объект", id));
    }

    public CelestialObject create(CelestialObject object) {
        object.setId(null);
        validate(object);
        return celestialObjectRepository.save(object);
    }

    public CelestialObject update(Long id, CelestialObject patch) {
        CelestialObject object = findById(id);
        if (patch.getName() != null) {
            object.setName(patch.getName());
        }
        if (patch.getType() != null) {
            object.setType(patch.getType());
        }
        if (patch.getMinAperture() != null) {
            object.setMinAperture(patch.getMinAperture());
        }
        validate(object);
        return celestialObjectRepository.save(object);
    }

    /** Удаляет объект вместе с запланированными на него сеансами и их записями. */
    public void delete(Long id) {
        findById(id);
        sessionService.deleteAllByCelestialObjectId(id);
        celestialObjectRepository.deleteById(id);
    }

    private void validate(CelestialObject object) {
        object.setName(notBlank(object.getName(), "name"));
        required(object.getType(), "type");
        positive(object.getMinAperture(), "minAperture");
        boolean nameTaken = object.getId() == null
                ? celestialObjectRepository.existsByNameIgnoreCase(object.getName())
                : celestialObjectRepository.existsByNameIgnoreCaseAndIdNot(object.getName(), object.getId());
        if (nameTaken) {
            throw new ConflictException("Небесный объект '" + object.getName() + "' уже существует");
        }
        if (object.getId() == null) {
            return;
        }
        sessionRepository.findByCelestialObjectId(object.getId()).stream()
                .filter(session -> session.getStatus() != SessionStatus.CANCELLED)
                .map(session -> telescopeRepository.findById(session.getTelescopeId()).orElseThrow())
                .filter(telescope -> telescope.getAperture() < object.getMinAperture())
                .findFirst()
                .ifPresent(telescope -> {
                    throw new ConflictException("Телескопа '" + telescope.getName()
                            + "' недостаточно для новой минимальной апертуры");
                });
    }
}
