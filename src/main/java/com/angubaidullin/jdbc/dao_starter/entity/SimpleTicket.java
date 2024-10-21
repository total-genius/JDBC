package com.angubaidullin.jdbc.dao_starter.entity;

import java.math.BigDecimal;
import java.util.Objects;

public class SimpleTicket {
    private Long id;
    private String passengerName;
    private BigDecimal price;
    private Long flightId;

    public SimpleTicket() {
    }

    public SimpleTicket(String passengerName, BigDecimal price, Long flightId) {
        this.passengerName = passengerName;
        this.price = price;
        this.flightId = flightId;
    }

    public SimpleTicket(Long id, String passengerName, BigDecimal price, Long flightId) {
        this.id = id;
        this.passengerName = passengerName;
        this.price = price;
        this.flightId = flightId;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleTicket simpleTicket = (SimpleTicket) o;
        return Objects.equals(id, simpleTicket.id) && Objects.equals(passengerName, simpleTicket.passengerName) && Objects.equals(price, simpleTicket.price) && Objects.equals(flightId, simpleTicket.flightId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, passengerName, price, flightId);
    }

    @Override
    public String toString() {
        return "SimpleTicket{" +
                "id=" + id +
                ", passengerName='" + passengerName + '\'' +
                ", price=" + price +
                ", flightId=" + flightId +
                '}';
    }
}
