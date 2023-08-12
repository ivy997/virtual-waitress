package com.example.virtualwaitress.util;

import android.app.Application;

public class RestaurantUser extends Application {
    private String email;
    private String userId;

    private static RestaurantUser instance;

    public static RestaurantUser getInstance() {
        if (instance == null) {
            instance = new RestaurantUser();
        }

        return instance;
    }

    public RestaurantUser() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
