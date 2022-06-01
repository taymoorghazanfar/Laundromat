package com.laundromat.customer.model;

import com.laundromat.customer.model.observers.IOrderObserver;
import com.laundromat.customer.model.observers.IOrderSubject;
import com.laundromat.customer.model.order.Order;
import com.laundromat.customer.model.order.OrderStatus;
import com.laundromat.customer.model.util.Cart;
import com.laundromat.customer.model.util.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

public class Customer extends User implements IOrderSubject {

    private final ArrayList<IOrderObserver> observers = new ArrayList<>();
    private List<Location> locations;
    private ArrayList<Order> orders;
    private Cart cart;

    public Customer() {

        super();
        this.locations = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.cart = new Cart();
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    @Override
    public void registerObserver(IOrderObserver observer) {

        this.observers.add(observer);
    }

    @Override
    public void notifyObservers(String task, String orderId, OrderStatus status) {

        for (IOrderObserver observer : observers) {

            observer.updateView(task, orderId, status);
        }
    }
}
