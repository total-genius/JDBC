package com.angubaidullin.jdbc.dao_starter.dto;

import java.math.BigDecimal;

public record TicketFilter(
        String passengerName,
        BigDecimal price,
        Long flightId) {

}
