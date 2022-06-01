package com.laundromat.customer.model.observers;

import com.laundromat.customer.model.order.OrderStatus;

public interface IOrderObserver {

    void updateView(String task, String orderId, OrderStatus status);
}
