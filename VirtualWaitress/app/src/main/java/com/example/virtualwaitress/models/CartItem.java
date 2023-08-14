package com.example.virtualwaitress.models;

public class CartItem {
    private String cartItemId;
    private Dish dish;
    private int quantity;
    private int tableNumber;
    private String userId;

    public CartItem() {
    }

    public CartItem(Dish dish, int quantity, int tableNumber, String userId) {
        this.dish = dish;
        this.quantity = quantity;
        this.tableNumber = tableNumber;
        this.userId = userId;
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
