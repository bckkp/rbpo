package ru.mtuci.rbpo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Телескоп. */
@Entity
@Table(name = "telescopes")
public class Telescope extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String name;
    /** Апертура, мм. */
    @Column(nullable = false)
    private Double aperture;

    @Column(nullable = false)
    private Boolean available;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAperture() {
        return aperture;
    }

    public void setAperture(Double aperture) {
        this.aperture = aperture;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
