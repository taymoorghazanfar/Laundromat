package com.laundromat.delivery.ui.interfaces;

import com.laundromat.delivery.model.DeliveryBoy;
import com.laundromat.delivery.model.Laundry;
import com.laundromat.delivery.model.Merchant;
import com.laundromat.delivery.model.Vehicle;

public interface ISignupListener {

    void onSignupComplete(DeliveryBoy deliveryBoy, Vehicle vehicle);
}
