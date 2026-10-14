package domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Park {
    private Long id;
    private String name;
    private String type;
    private BigDecimal area;
    private boolean hasWater;
    private LocalDate buildDate;

    public Park() {
    }

    public Park(String name, String type, BigDecimal area, boolean hasWater, LocalDate buildDate) {
        this.name = name;
        this.type = type;
        this.area = area;
        this.hasWater = hasWater;
        this.buildDate = buildDate;
    }

    public Park(Long id, String name, String type, BigDecimal area, boolean hasWater, LocalDate buildDate) {
        this(name, type, area, hasWater, buildDate);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public boolean isHasWater() {
        return hasWater;
    }

    public void setHasWater(boolean hasWater) {
        this.hasWater = hasWater;
    }

    public LocalDate getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(LocalDate buildDate) {
        this.buildDate = buildDate;
    }
    @Override public String toString() {
        return "Park {id=" + id + ", name=" + name + ", type=" + type + ", area=" + area + ", hasWater=" + hasWater + ", buildDate=" + buildDate + "}";
    }
}
