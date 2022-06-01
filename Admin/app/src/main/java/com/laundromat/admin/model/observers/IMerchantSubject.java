package com.laundromat.admin.model.observers;

import com.laundromat.admin.model.order.OrderStatus;

public interface IMerchantSubject {

    void registerObserver(IMerchantObserver observer);

    void notifyObservers(String task, String orderId, OrderStatus status);
}
