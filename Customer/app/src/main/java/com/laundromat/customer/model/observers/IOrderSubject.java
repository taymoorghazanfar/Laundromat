package com.laundromat.customer.model.observers;

import com.laundromat.customer.model.order.OrderStatus;

public interface IOrderSubject {

    void registerObserver(IOrderObserver observer);

    void notifyObservers(String task, String orderId, OrderStatus status);
}
