package ru.mtuci.rbpo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.mtuci.rbpo.dto.CelestialObjectRequest;
import ru.mtuci.rbpo.dto.CelestialObjectResponse;
import ru.mtuci.rbpo.model.CelestialObject;
import ru.mtuci.rbpo.service.CelestialObjectService;

@RestController
@RequestMapping("/api/celestial-objects")
public class CelestialObjectController {

    private final CelestialObjectService service;

    public CelestialObjectController(CelestialObjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<CelestialObjectResponse> getAll() {
        return service.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public CelestialObjectResponse getById(@PathVariable Long id) {
        return toResponse(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CelestialObjectResponse create(@RequestBody CelestialObjectRequest request) {
        return toResponse(service.create(toEntity(request)));
    }

    @PatchMapping("/{id}")
    public CelestialObjectResponse update(@PathVariable Long id, @RequestBody CelestialObjectRequest request) {
        return toResponse(service.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private CelestialObject toEntity(CelestialObjectRequest request) {
        CelestialObject entity = new CelestialObject();
        entity.setName(request.name());
        entity.setType(request.type());
        entity.setMinAperture(request.minAperture());
        return entity;
    }

    private CelestialObjectResponse toResponse(CelestialObject entity) {
        return new CelestialObjectResponse(entity.getId(), entity.getName(), entity.getType(), entity.getMinAperture());
    }
}
