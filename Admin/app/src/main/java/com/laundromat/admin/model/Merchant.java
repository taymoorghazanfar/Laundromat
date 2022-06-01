package com.laundromat.admin.model;

import android.util.Log;

import com.laundromat.admin.model.observers.IMerchantObserver;
import com.laundromat.admin.model.observers.IMerchantSubject;
import com.laundromat.admin.model.order.OrderStatus;

import java.util.ArrayList;

public class Merchant extends User implements IMerchantSubject {

    private final ArrayList<IMerchantObserver> observers = new ArrayList<>();
    private String nicNumber;
    private String nicImageUrl;
    private Laundry laundry;

    public Merchant() {

        super();
        // Empty constructor
    }

    public String getNicNumber() {
        return nicNumber;
    }

    public void setNicNumber(String nicNumber) {
        this.nicNumber = nicNumber;
    }

    public String getNicImageUrl() {
        return nicImageUrl;
    }

    public void setNicImageUrl(String nicImageUrl) {
        this.nicImageUrl = nicImageUrl;
    }

    public Laundry getLaundry() {
        return laundry;
    }

    public void setLaundry(Laundry laundry) {
        this.laundry = laundry;
    }

    @Override
    public void registerObserver(IMerchantObserver observer) {

        Log.d("observer", "registerObserver: observer added");
        observers.add(observer);
    }

    @Override
    public void notifyObservers(String task, String orderId, OrderStatus status) {

        for (IMerchantObserver observer : observers) {

            observer.updateView(task, orderId, status);
        }
    }
}
