package ru.mtuci.rbpo.dto;

import java.time.Instant;

public record ObservationRecordResponse(Long id, Long sessionId, Instant recordedAt, String description, Integer quality) {
}
