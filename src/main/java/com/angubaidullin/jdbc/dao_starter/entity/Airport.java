package com.angubaidullin.jdbc.dao_starter.entity;

import java.util.Objects;

public class Airport {
    private Long id;
    private String city;
    private String airportName;

    public Airport() {
    }

    public Airport(String city, String airportName) {
        this.city = city;
        this.airportName = airportName;
    }

    public Airport(Long id, String city, String airportName) {
        this.id = id;
        this.city = city;
        this.airportName = airportName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAirportName() {
        return airportName;
    }

    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airport airport = (Airport) o;
        return Objects.equals(id, airport.id) && Objects.equals(city, airport.city) && Objects.equals(airportName, airport.airportName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, city, airportName);
    }

    @Override
    public String toString() {
        return "Airport{" +
                "id=" + id +
                ", city='" + city + '\'' +
                ", airportName='" + airportName + '\'' +
                '}';
    }
}
