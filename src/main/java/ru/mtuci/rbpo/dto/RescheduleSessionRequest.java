package ru.mtuci.rbpo.dto;

import java.time.Instant;

public record RescheduleSessionRequest(Instant startTime, Instant endTime) {
}
