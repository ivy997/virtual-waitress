package com.example.virtualwaitress.models;

import com.example.virtualwaitress.enums.OrderStatus;
import com.google.type.DateTime;

import java.util.Date;
import java.util.List;

public class Order {
    private String orderId;
    private List<CartItem> items;
    private Date orderDateAndTime;
    private OrderStatus orderStatus;
    private float billPrice;
    private int tableNumber;

    public Order() {
    }

    public Order(List<CartItem> items, Date orderDateAndTime, OrderStatus orderStatus, float billPrice, int tableNumber) {
        this.items = items;
        this.orderDateAndTime = orderDateAndTime;
        this.orderStatus = orderStatus;
        this.billPrice = billPrice;
        this.tableNumber = tableNumber;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Date getOrderDateAndTime() {
        return orderDateAndTime;
    }

    public void setOrderDateAndTime(Date orderDateAndTime) {
        this.orderDateAndTime = orderDateAndTime;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public float getBillPrice() {
        return billPrice;
    }

    public void setBillPrice(float billPrice) {
        this.billPrice = billPrice;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }
}
