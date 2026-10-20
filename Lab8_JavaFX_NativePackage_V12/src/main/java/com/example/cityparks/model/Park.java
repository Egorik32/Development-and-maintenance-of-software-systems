package com.example.cityparks.model;

import java.time.LocalDate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Park {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final DoubleProperty area = new SimpleDoubleProperty();
    private final BooleanProperty hasWater = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDate> buildDate = new SimpleObjectProperty<>();

    public Park() {
        this("", "", 0.0, false, LocalDate.now());
    }

    public Park(String name, String type, double area, boolean hasWater, LocalDate buildDate) {
        setName(name);
        setType(type);
        setArea(area);
        setHasWater(hasWater);
        setBuildDate(buildDate);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public double getArea() {
        return area.get();
    }

    public void setArea(double area) {
        this.area.set(area);
    }

    public DoubleProperty areaProperty() {
        return area;
    }

    public boolean isHasWater() {
        return hasWater.get();
    }

    public void setHasWater(boolean hasWater) {
        this.hasWater.set(hasWater);
    }

    public BooleanProperty hasWaterProperty() {
        return hasWater;
    }

    @javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(com.example.cityparks.util.LocalDateAdapter.class)
    public LocalDate getBuildDate() {
        return buildDate.get();
    }

    public void setBuildDate(LocalDate buildDate) {
        this.buildDate.set(buildDate);
    }

    public ObjectProperty<LocalDate> buildDateProperty() {
        return buildDate;
    }

    @Override
    public String toString() {
        return getName();
    }
}
