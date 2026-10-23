package ru.mtuci.rbpo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import ru.mtuci.rbpo.model.Telescope;

public interface TelescopeRepository extends JpaRepository<Telescope, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Telescope t where t.id = :id")
    Optional<Telescope> findByIdForUpdate(@Param("id") Long id);
}
