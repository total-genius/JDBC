package com.angubaidullin.jdbc.dao_starter.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Flight {
    private Long id;
    private Airport departureAirport;
    private Airport arrivalAirport;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private Airplane airplane;

    public Flight() {
    }

    public Flight(Airport departureAirport, Airport arrivalAirport, LocalDate departureDate, LocalDate arrivalDate, Airplane airplane) {
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.airplane = airplane;
    }

    public Flight(Long id, Airport departureAirport, Airport arrivalAirport, LocalDate departureDate, LocalDate arrivalDate, Airplane airplane) {
        this.id = id;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.airplane = airplane;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Airport getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(Airport departureAirport) {
        this.departureAirport = departureAirport;
    }

    public Airport getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(Airport arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public Airplane getAirplane() {
        return airplane;
    }

    public void setAirplane(Airplane airplane) {
        this.airplane = airplane;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(id, flight.id) && Objects.equals(departureAirport, flight.departureAirport) && Objects.equals(arrivalAirport, flight.arrivalAirport) && Objects.equals(departureDate, flight.departureDate) && Objects.equals(arrivalDate, flight.arrivalDate) && Objects.equals(airplane, flight.airplane);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, departureAirport, arrivalAirport, departureDate, arrivalDate, airplane);
    }

    @Override
    public String toString() {
        return "Flight{" +
                "id=" + id +
                ", departureAirport=" + departureAirport +
                ", arrivalAirport=" + arrivalAirport +
                ", departureDate=" + departureDate +
                ", arrivalDate=" + arrivalDate +
                ", airplane=" + airplane +
                '}';
    }
}
