package ru.mtuci.rbpo.dto;

/** Тело POST/PATCH для телескопа. aperture — в миллиметрах, available по умолчанию true. */
public record TelescopeRequest(String name, Double aperture, Boolean available) {
}
