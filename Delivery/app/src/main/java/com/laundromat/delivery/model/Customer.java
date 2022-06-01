package com.laundromat.delivery.model;

import com.laundromat.delivery.model.order.Order;
import com.laundromat.delivery.model.util.Cart;
import com.laundromat.delivery.model.util.Location;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User{

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
}
