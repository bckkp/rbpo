package ru.mtuci.rbpo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.mtuci.rbpo.model.CelestialObject;

public interface CelestialObjectRepository extends JpaRepository<CelestialObject, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
