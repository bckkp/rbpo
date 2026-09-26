package ru.mtuci.rbpo.dto;

import ru.mtuci.rbpo.model.CelestialObjectType;

public record CelestialObjectResponse(Long id, String name, CelestialObjectType type, Double minAperture) {
}
