package ru.mtuci.rbpo.dto;

import java.time.Instant;

import ru.mtuci.rbpo.model.SessionStatus;

public record ObservationSessionResponse(Long id,
                                         Long celestialObjectId,
                                         Long telescopeId,
                                         Long observerId,
                                         Instant startTime,
                                         Instant endTime,
                                         SessionStatus status,
                                         Instant createdAt) {
}
