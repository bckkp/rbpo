package ru.mtuci.rbpo.repository;

import org.springframework.stereotype.Repository;

import ru.mtuci.rbpo.model.CelestialObject;

@Repository
public class CelestialObjectRepository extends InMemoryRepository<CelestialObject> {
}
