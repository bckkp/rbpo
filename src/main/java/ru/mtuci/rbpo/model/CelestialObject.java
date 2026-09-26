package ru.mtuci.rbpo.model;

/** Небесный объект. */
public class CelestialObject extends BaseEntity {

    private String name;
    private CelestialObjectType type;
    /** Минимальная требуемая апертура телескопа, мм. */
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
