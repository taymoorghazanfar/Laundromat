package com.laundromat.admin.ui.interfaces;

import com.laundromat.admin.model.order.OrderStatus;

public interface IOrderFilterListener {

    void onOrderFiltered(OrderStatus status);
}
