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

import ru.mtuci.rbpo.dto.ObservationRecordRequest;
import ru.mtuci.rbpo.dto.ObservationRecordResponse;
import ru.mtuci.rbpo.model.ObservationRecord;
import ru.mtuci.rbpo.service.ObservationRecordService;

@RestController
@RequestMapping("/api/observation-records")
public class ObservationRecordController {

    private final ObservationRecordService service;

    public ObservationRecordController(ObservationRecordService service) {
        this.service = service;
    }

    @GetMapping
    public List<ObservationRecordResponse> getAll() {
        return service.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ObservationRecordResponse getById(@PathVariable Long id) {
        return toResponse(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ObservationRecordResponse create(@RequestBody ObservationRecordRequest request) {
        return toResponse(service.create(toEntity(request)));
    }

    @PatchMapping("/{id}")
    public ObservationRecordResponse update(@PathVariable Long id, @RequestBody ObservationRecordRequest request) {
        return toResponse(service.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private ObservationRecord toEntity(ObservationRecordRequest request) {
        ObservationRecord entity = new ObservationRecord();
        entity.setSessionId(request.sessionId());
        entity.setDescription(request.description());
        entity.setQuality(request.quality());
        return entity;
    }

    private ObservationRecordResponse toResponse(ObservationRecord entity) {
        return new ObservationRecordResponse(entity.getId(), entity.getSessionId(), entity.getRecordedAt(),
                entity.getDescription(), entity.getQuality());
    }
}
