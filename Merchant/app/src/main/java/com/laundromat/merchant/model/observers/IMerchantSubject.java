package com.laundromat.merchant.model.observers;

import com.laundromat.merchant.model.order.OrderStatus;

public interface IMerchantSubject {

    void registerObserver(IMerchantObserver observer);

    void notifyObservers(String task, String orderId, OrderStatus status);
}
