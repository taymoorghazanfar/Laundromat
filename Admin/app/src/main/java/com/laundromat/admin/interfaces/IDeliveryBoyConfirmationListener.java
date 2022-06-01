package com.laundromat.admin.interfaces;

import com.laundromat.admin.model.DeliveryBoy;

public interface IDeliveryBoyConfirmationListener {

    void onDeliveryBoyConfirmationClick(DeliveryBoy deliveryBoy, int index);
}
