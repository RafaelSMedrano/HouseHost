package com.househost.shared.dto;

public class ResponseDTO {

    private String status;
    private String message;
    private Object data;

    public ResponseDTO() {
    }

    public ResponseDTO(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
