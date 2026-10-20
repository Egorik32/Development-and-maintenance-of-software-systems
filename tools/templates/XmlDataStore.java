package com.example.cityparks.util;

import java.io.File;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import com.example.cityparks.model.City;
import com.example.cityparks.model.CityListWrapper;
import com.example.cityparks.model.Park;
import javafx.collections.ObservableList;

public final class XmlDataStore {
    private XmlDataStore() { }
    public static void save(File file, ObservableList<City> cities) throws Exception {
        JAXBContext context = JAXBContext.newInstance(CityListWrapper.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        CityListWrapper wrapper = new CityListWrapper();
        wrapper.setCities(new ArrayList<>(cities));
        marshaller.marshal(wrapper, file);
    }
    public static void load(File file, ObservableList<City> cities, ObservableList<Park> parks) throws Exception {
        JAXBContext context = JAXBContext.newInstance(CityListWrapper.class);
        CityListWrapper wrapper = (CityListWrapper) context.createUnmarshaller().unmarshal(file);
        for (City city : wrapper.getCities()) {
            Park park = city.getPark();
            if (city.getName() == null || city.getName().trim().isEmpty()
                    || city.getStatus() == null || city.getStatus().trim().isEmpty()
                    || city.getPopulation() <= 0 || park == null
                    || park.getName() == null || park.getName().trim().isEmpty()
                    || park.getType() == null || park.getType().trim().isEmpty()
                    || !Double.isFinite(park.getArea()) || park.getArea() <= 0
                    || park.getBuildDate() == null) {
                throw new IllegalArgumentException("XML содержит неполную или некорректную запись.");
            }
        }
        cities.setAll(wrapper.getCities());
        parks.clear();
        for (City city : cities) parks.add(city.getPark());
    }
}
