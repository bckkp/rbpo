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

import ru.mtuci.rbpo.dto.TelescopeRequest;
import ru.mtuci.rbpo.dto.TelescopeResponse;
import ru.mtuci.rbpo.model.Telescope;
import ru.mtuci.rbpo.service.TelescopeService;

@RestController
@RequestMapping("/api/telescopes")
public class TelescopeController {

    private final TelescopeService service;

    public TelescopeController(TelescopeService service) {
        this.service = service;
    }

    @GetMapping
    public List<TelescopeResponse> getAll() {
        return service.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public TelescopeResponse getById(@PathVariable Long id) {
        return toResponse(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TelescopeResponse create(@RequestBody TelescopeRequest request) {
        return toResponse(service.create(toEntity(request)));
    }

    @PatchMapping("/{id}")
    public TelescopeResponse update(@PathVariable Long id, @RequestBody TelescopeRequest request) {
        return toResponse(service.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private Telescope toEntity(TelescopeRequest request) {
        Telescope entity = new Telescope();
        entity.setName(request.name());
        entity.setAperture(request.aperture());
        entity.setAvailable(request.available());
        return entity;
    }

    private TelescopeResponse toResponse(Telescope entity) {
        return new TelescopeResponse(entity.getId(), entity.getName(), entity.getAperture(), entity.getAvailable());
    }
}
