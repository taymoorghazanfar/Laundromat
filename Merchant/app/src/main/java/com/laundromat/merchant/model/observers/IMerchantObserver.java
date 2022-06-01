package com.laundromat.merchant.model.observers;

import com.laundromat.merchant.model.order.OrderStatus;

public interface IMerchantObserver {

    void updateView(String task, String orderId, OrderStatus status);
}
