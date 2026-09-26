package ru.mtuci.rbpo.dto;

/** Тело POST/PATCH для записи. Время записи задаёт сервер. */
public record ObservationRecordRequest(Long sessionId, String description, Integer quality) {
}
