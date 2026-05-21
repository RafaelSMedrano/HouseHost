package com.househost.guest.model;

public enum GuestType {
    NOVO("new"),
    REGULAR("regular"),
    VIP("vip");

    private final String apiValue;

    GuestType(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }
}
