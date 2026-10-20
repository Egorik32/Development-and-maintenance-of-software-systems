package com.example.cityparks.util;

import java.time.LocalDate;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    @Override public LocalDate unmarshal(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
    @Override public String marshal(LocalDate value) {
        return value == null ? null : value.toString();
    }
}
