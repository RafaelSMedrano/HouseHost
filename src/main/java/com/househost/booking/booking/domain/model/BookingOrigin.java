package com.househost.booking.booking.domain.model;

public enum BookingOrigin {
    DIRETO_TELEFONE("Direto / Telefone"),
    WHATSAPP("WhatsApp"),
    INSTAGRAM("Instagram"),
    BOOKING("Booking"),
    AIRBNB("Airbnb"),
    INDICACAO("Indicacao");

    private final String label;

    BookingOrigin(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
