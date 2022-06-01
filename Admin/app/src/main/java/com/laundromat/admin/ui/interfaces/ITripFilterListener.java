package com.laundromat.admin.ui.interfaces;

import com.laundromat.admin.model.util.TripStatus;

public interface ITripFilterListener {

    void onTripFiltered(TripStatus status);
}
