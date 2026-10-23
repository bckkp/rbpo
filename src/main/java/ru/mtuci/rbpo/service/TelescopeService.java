package ru.mtuci.rbpo.service;

import static ru.mtuci.rbpo.service.Checks.notBlank;
import static ru.mtuci.rbpo.service.Checks.positive;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mtuci.rbpo.exception.ConflictException;
import ru.mtuci.rbpo.exception.NotFoundException;
import ru.mtuci.rbpo.model.Telescope;
import ru.mtuci.rbpo.model.SessionStatus;
import ru.mtuci.rbpo.repository.CelestialObjectRepository;
import ru.mtuci.rbpo.repository.ObservationSessionRepository;
import ru.mtuci.rbpo.repository.TelescopeRepository;

@Service
@Transactional
public class TelescopeService {

    private final TelescopeRepository telescopeRepository;
    private final ObservationSessionRepository sessionRepository;
    private final CelestialObjectRepository celestialObjectRepository;
    private final ObservationSessionService sessionService;

    public TelescopeService(TelescopeRepository telescopeRepository,
                            ObservationSessionRepository sessionRepository,
                            CelestialObjectRepository celestialObjectRepository,
                            ObservationSessionService sessionService) {
        this.telescopeRepository = telescopeRepository;
        this.sessionRepository = sessionRepository;
        this.celestialObjectRepository = celestialObjectRepository;
        this.sessionService = sessionService;
    }

    public List<Telescope> findAll() {
        return telescopeRepository.findAll();
    }

    public Telescope findById(Long id) {
        return telescopeRepository.findById(id).orElseThrow(() -> new NotFoundException("Телескоп", id));
    }

    public Telescope create(Telescope telescope) {
        telescope.setId(null);
        if (telescope.getAvailable() == null) {
            telescope.setAvailable(true);
        }
        validate(telescope);
        return telescopeRepository.save(telescope);
    }

    public Telescope update(Long id, Telescope patch) {
        Telescope telescope = findById(id);
        if (patch.getName() != null) {
            telescope.setName(patch.getName());
        }
        if (patch.getAperture() != null) {
            telescope.setAperture(patch.getAperture());
        }
        if (patch.getAvailable() != null) {
            if (patch.getAvailable()
                    && sessionRepository.existsByTelescopeIdAndStatus(id, SessionStatus.IN_PROGRESS)) {
                throw new ConflictException("Нельзя освободить телескоп: на нём идёт сеанс наблюдения");
            }
            telescope.setAvailable(patch.getAvailable());
        }
        validate(telescope);
        return telescopeRepository.save(telescope);
    }

    /** Удаляет телескоп вместе с сеансами на нём и их записями. */
    public void delete(Long id) {
        findById(id);
        sessionService.deleteAllByTelescopeId(id);
        telescopeRepository.deleteById(id);
    }

    private void validate(Telescope telescope) {
        telescope.setName(notBlank(telescope.getName(), "name"));
        positive(telescope.getAperture(), "aperture");
        boolean nameTaken = telescope.getId() == null
                ? telescopeRepository.existsByNameIgnoreCase(telescope.getName())
                : telescopeRepository.existsByNameIgnoreCaseAndIdNot(telescope.getName(), telescope.getId());
        if (nameTaken) {
            throw new ConflictException("Телескоп '" + telescope.getName() + "' уже существует");
        }
        if (telescope.getId() == null) {
            return;
        }
        sessionRepository.findByTelescopeId(telescope.getId()).stream()
                .filter(session -> session.getStatus() != SessionStatus.CANCELLED)
                .map(session -> celestialObjectRepository.findById(session.getCelestialObjectId()).orElseThrow())
                .filter(object -> object.getMinAperture() > telescope.getAperture())
                .findFirst()
                .ifPresent(object -> {
                    throw new ConflictException("Апертуры недостаточно для связанного объекта '"
                            + object.getName() + "'");
                });
    }
}
