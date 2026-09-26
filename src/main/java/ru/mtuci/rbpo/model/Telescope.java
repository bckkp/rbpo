package ru.mtuci.rbpo.model;

/** Телескоп. */
public class Telescope extends BaseEntity {

    private String name;
    /** Апертура, мм. */
    private Double aperture;
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
