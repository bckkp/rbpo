package ru.mtuci.rbpo.dto;

import java.time.Instant;

import ru.mtuci.rbpo.model.SessionStatus;

/** Тело POST/PATCH для сеанса. Время создания сеанса задаёт сервер. */
public record ObservationSessionRequest(Long celestialObjectId,
                                        Long telescopeId,
                                        Long observerId,
                                        Instant startTime,
                                        Instant endTime,
                                        SessionStatus status) {
}
