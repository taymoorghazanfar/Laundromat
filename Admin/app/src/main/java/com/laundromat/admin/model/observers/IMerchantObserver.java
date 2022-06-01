package com.laundromat.admin.model.observers;

import com.laundromat.admin.model.order.OrderStatus;

public interface IMerchantObserver {

    void updateView(String task, String orderId, OrderStatus status);
}
