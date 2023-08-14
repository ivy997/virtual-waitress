package com.example.virtualwaitress.enums;

import java.io.Serializable;

public enum OrderStatus implements Serializable {
    PLACED("Placed"),
    PREPARING("Preparing"),
    READY("Ready"),
    SERVED("Served"),
    COMPLETED("Completed"),
    PAID("Paid"),
    CANCELED("Canceled");

    private final String status;

    OrderStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
