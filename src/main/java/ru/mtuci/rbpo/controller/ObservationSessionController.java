package ru.mtuci.rbpo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.mtuci.rbpo.dto.ObservationSessionRequest;
import ru.mtuci.rbpo.dto.ObservationSessionResponse;
import ru.mtuci.rbpo.dto.ChangeTelescopeRequest;
import ru.mtuci.rbpo.dto.RescheduleSessionRequest;
import ru.mtuci.rbpo.model.ObservationSession;
import ru.mtuci.rbpo.service.ObservationSessionService;

@RestController
@RequestMapping("/api/observation-sessions")
public class ObservationSessionController {

    private final ObservationSessionService service;

    public ObservationSessionController(ObservationSessionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OBSERVER')")
    public List<ObservationSessionResponse> getAll() {
        return service.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OBSERVER')")
    public ObservationSessionResponse getById(@PathVariable Long id) {
        return toResponse(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authorizationService.canCreateSession(#request, authentication)")
    public ObservationSessionResponse create(@RequestBody ObservationSessionRequest request) {
        return toResponse(service.create(toEntity(request)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@authorizationService.canUpdateSession(#id, #request, authentication)")
    public ObservationSessionResponse update(@PathVariable Long id, @RequestBody ObservationSessionRequest request) {
        return toResponse(service.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authorizationService.canModifySession(#id, authentication)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("@authorizationService.canModifySession(#id, authentication)")
    public ObservationSessionResponse start(@PathVariable Long id) {
        return toResponse(service.start(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("@authorizationService.canModifySession(#id, authentication)")
    public ObservationSessionResponse complete(@PathVariable Long id) {
        return toResponse(service.complete(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("@authorizationService.canModifySession(#id, authentication)")
    public ObservationSessionResponse cancel(@PathVariable Long id) {
        return toResponse(service.cancel(id));
    }

    @PostMapping("/{id}/reschedule")
    @PreAuthorize("@authorizationService.canModifySession(#id, authentication)")
    public ObservationSessionResponse reschedule(@PathVariable Long id,
                                                 @RequestBody RescheduleSessionRequest request) {
        return toResponse(service.reschedule(id, request.startTime(), request.endTime()));
    }

    @PostMapping("/{id}/change-telescope")
    @PreAuthorize("@authorizationService.canModifySession(#id, authentication)")
    public ObservationSessionResponse changeTelescope(@PathVariable Long id,
                                                      @RequestBody ChangeTelescopeRequest request) {
        return toResponse(service.changeTelescope(id, request.telescopeId()));
    }

    private ObservationSession toEntity(ObservationSessionRequest request) {
        ObservationSession entity = new ObservationSession();
        entity.setCelestialObjectId(request.celestialObjectId());
        entity.setTelescopeId(request.telescopeId());
        entity.setObserverId(request.observerId());
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setStatus(request.status());
        return entity;
    }

    private ObservationSessionResponse toResponse(ObservationSession entity) {
        return new ObservationSessionResponse(entity.getId(), entity.getCelestialObjectId(), entity.getTelescopeId(),
                entity.getObserverId(), entity.getStartTime(), entity.getEndTime(), entity.getStatus(),
                entity.getCreatedAt());
    }
}
