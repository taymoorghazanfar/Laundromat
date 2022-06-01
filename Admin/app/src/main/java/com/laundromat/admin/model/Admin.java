package com.laundromat.admin.model;

import com.laundromat.admin.model.order.Order;
import com.laundromat.admin.model.washable.ServiceType;

import java.util.List;

public class Admin {

    private String username;
    private String password;
    private String email;
    private double baseFare;
    private double deliveryRadius;
    private double perKm;

    private List<ServiceType> serviceTypes;
    private List<Merchant> newMerchants;
    private List<DeliveryBoy> newDeliveryBoys;
    private List<Merchant> merchants;
    private List<DeliveryBoy> deliveryBoys;
    private List<Customer> customers;
    private List<Order> orders;

    public Admin() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }

    public double getDeliveryRadius() {
        return deliveryRadius;
    }

    public void setDeliveryRadius(double deliveryRadius) {
        this.deliveryRadius = deliveryRadius;
    }

    public double getPerKmCharges() {
        return perKm;
    }

    public void setPerKmCharges(double perKmCharges) {
        this.perKm = perKmCharges;
    }

    public List<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public List<Merchant> getNewMerchants() {
        return newMerchants;
    }

    public void setNewMerchants(List<Merchant> newMerchants) {
        this.newMerchants = newMerchants;
    }

    public List<DeliveryBoy> getNewDeliveryBoys() {
        return newDeliveryBoys;
    }

    public void setNewDeliveryBoys(List<DeliveryBoy> newDeliveryBoys) {
        this.newDeliveryBoys = newDeliveryBoys;
    }

    public List<Merchant> getMerchants() {
        return merchants;
    }

    public void setMerchants(List<Merchant> merchants) {
        this.merchants = merchants;
    }

    public List<DeliveryBoy> getDeliveryBoys() {
        return deliveryBoys;
    }

    public void setDeliveryBoys(List<DeliveryBoy> deliveryBoys) {
        this.deliveryBoys = deliveryBoys;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
