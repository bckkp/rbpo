package ru.mtuci.rbpo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Небесный объект. */
@Entity
@Table(name = "celestial_objects")
public class CelestialObject extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CelestialObjectType type;
    /** Минимальная требуемая апертура телескопа, мм. */
    @Column(name = "min_aperture", nullable = false)
    private Double minAperture;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CelestialObjectType getType() {
        return type;
    }

    public void setType(CelestialObjectType type) {
        this.type = type;
    }

    public Double getMinAperture() {
        return minAperture;
    }

    public void setMinAperture(Double minAperture) {
        this.minAperture = minAperture;
    }
}
