package ru.mtuci.rbpo.service;

import static ru.mtuci.rbpo.service.Checks.notBlank;
import static ru.mtuci.rbpo.service.Checks.positive;

import java.util.List;

import org.springframework.stereotype.Service;

import ru.mtuci.rbpo.exception.NotFoundException;
import ru.mtuci.rbpo.model.Telescope;
import ru.mtuci.rbpo.repository.TelescopeRepository;

@Service
public class TelescopeService {

    private final TelescopeRepository telescopeRepository;
    private final ObservationSessionService sessionService;

    public TelescopeService(TelescopeRepository telescopeRepository, ObservationSessionService sessionService) {
        this.telescopeRepository = telescopeRepository;
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
    }
}
