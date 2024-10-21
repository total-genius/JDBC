package com.angubaidullin.jdbc.dao_starter;

import com.angubaidullin.jdbc.dao_starter.entity.Flight;
import com.angubaidullin.jdbc.dao_starter.service.FlightService;

import java.sql.SQLException;

public class DaoRunner {
    public static void main(String[] args) throws SQLException {
//        SimpleTicketDao simpleTicketDao = SimpleTicketDao.getInstance();
//        SimpleTicket petrPetrovSimpleTicket = new SimpleTicket("Petr Petrov", BigDecimal.TEN, 5L);
//
//        List<SimpleTicket> result = simpleTicketDao.findAll(new TicketFilter(null, BigDecimal.valueOf(400.00), 5L));
//        result.forEach(ticket -> System.out.println(ticket));
        Flight flight = FlightService.getInstance().getFlightById(1L);
        System.out.println(flight);



    }

}
