package com.laundromat.merchant.model.order;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.laundromat.merchant.model.util.PaymentMethod;
import com.laundromat.merchant.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Order {

    private String id;
    private String pickupCode;
    private String deliveryCode;
    private String laundryId;
    private String customerId;
    private String laundryName;
    private String dateCreated;
    private String dateCompleted;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private LatLng deliveryLocation;
    private boolean isPayed;
    private int itemsQuantity;
    private double price;
    private double discount;
    private Map<String, OrderItem> items;

    public Order() {

        this.dateCreated = StringUtils.getCurrentDateTime();
        this.dateCompleted = null;
        this.isPayed = false;
        this.items = new HashMap<>();
    }

    // copy constructor
    public Order(Order order) {

        this.id = order.id;
        this.laundryId = order.laundryId;
        this.customerId = order.customerId;
        this.laundryName = order.laundryName;
        this.dateCreated = order.dateCreated;
        this.dateCompleted = order.dateCompleted;
        this.status = order.status;
        this.paymentMethod = order.paymentMethod;
        this.deliveryLocation = order.deliveryLocation;
        this.itemsQuantity = order.itemsQuantity;
        this.price = order.price;
        this.discount = order.discount;
        this.items = order.items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public String getDeliveryCode() {
        return deliveryCode;
    }

    public void setDeliveryCode(String deliveryCode) {
        this.deliveryCode = deliveryCode;
    }

    public String getLaundryId() {
        return laundryId;
    }

    public void setLaundryId(String laundryId) {
        this.laundryId = laundryId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getLaundryName() {
        return laundryName;
    }

    public void setLaundryName(String laundryName) {
        this.laundryName = laundryName;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(String dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LatLng getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(LatLng deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public boolean isPayed() {
        return isPayed;
    }

    public void setPayed(boolean payed) {
        isPayed = payed;
    }

    public int getItemsQuantity() {
        return itemsQuantity;
    }

    public void setItemsQuantity(int itemsQuantity) {
        this.itemsQuantity = itemsQuantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public Map<String, OrderItem> getItems() {
        return items;
    }

    public void setItems(Map<String, OrderItem> items) {
        this.items = items;
    }

    public JSONObject toJson() {

        Gson gson = new Gson();
        String jsonString = gson.toJson(this);

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            return null;
        }
    }
}
