package ru.mtuci.rbpo.service;

import ru.mtuci.rbpo.exception.BadRequestException;

/** Общие проверки входных данных для сервисов. */
final class Checks {

    private Checks() {
    }

    static <T> T required(T value, String field) {
        if (value == null) {
            throw new BadRequestException("Поле '" + field + "' обязательно");
        }
        return value;
    }

    static String notBlank(String value, String field) {
        if (required(value, field).isBlank()) {
            throw new BadRequestException("Поле '" + field + "' не может быть пустым");
        }
        return value.strip();
    }

    static double positive(Double value, String field) {
        if (required(value, field) <= 0) {
            throw new BadRequestException("Поле '" + field + "' должно быть больше 0");
        }
        return value;
    }
}
