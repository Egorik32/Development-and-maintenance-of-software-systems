package com.example.cityparks.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "cities")
public class CityListWrapper {
    private List<City> cities = new ArrayList<>();
    @XmlElement(name = "city")
    public List<City> getCities() { return cities; }
    public void setCities(List<City> cities) { this.cities = cities; }
}
