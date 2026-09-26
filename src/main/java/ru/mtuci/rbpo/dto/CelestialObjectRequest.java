package ru.mtuci.rbpo.dto;

import ru.mtuci.rbpo.model.CelestialObjectType;

/** Тело POST/PATCH для небесного объекта. minAperture — в миллиметрах. */
public record CelestialObjectRequest(String name, CelestialObjectType type, Double minAperture) {
}
