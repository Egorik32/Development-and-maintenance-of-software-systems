package com.example.cityparks.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class City {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final IntegerProperty population = new SimpleIntegerProperty();
    private final ObjectProperty<Park> park = new SimpleObjectProperty<>();

    public City() {
        this("", "", 0, null);
    }

    public City(String name, String status, int population, Park park) {
        setName(name);
        setStatus(status);
        setPopulation(population);
        setPark(park);
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

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public int getPopulation() {
        return population.get();
    }

    public void setPopulation(int population) {
        this.population.set(population);
    }

    public IntegerProperty populationProperty() {
        return population;
    }

    public Park getPark() {
        return park.get();
    }

    public void setPark(Park park) {
        this.park.set(park);
    }

    public ObjectProperty<Park> parkProperty() {
        return park;
    }
}
