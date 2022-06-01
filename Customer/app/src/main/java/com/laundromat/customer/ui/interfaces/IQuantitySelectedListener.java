package com.laundromat.customer.ui.interfaces;

import com.laundromat.customer.model.washable.ServiceType;

public interface IQuantitySelectedListener {

    void onQuantitySelected(ServiceType serviceType, int quantity);
}
