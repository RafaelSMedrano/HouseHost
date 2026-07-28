package com.househost.publicapi.application.dto;

import java.time.LocalDate;

public class PublicQuoteRequestDTO {
    public Long roomId;
    public LocalDate checkIn;
    public LocalDate checkOut;
    public Integer adults;
    public Integer children;
    public Integer pets;
}
