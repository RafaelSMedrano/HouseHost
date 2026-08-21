package com.househost.publicapi.application.dto;

import java.time.LocalDate;

public class PublicBookingRequestDTO {
    public Long roomId;
    public LocalDate checkIn;
    public LocalDate checkOut;
    public Integer adults;
    public Integer children;
    public Integer pets;
    public Long privacyPolicyId;
    public String termsVersion;
    public Boolean privacyAccepted;
    public GuestData guest;
    public String notes;

    public static class GuestData {
        public String firstName;
        public String lastName;
        public String email;
        public String phone;
        public String city;
    }
}
