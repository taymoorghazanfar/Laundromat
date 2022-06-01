package com.laundromat.delivery.model.observers;

import com.laundromat.delivery.model.util.TripStatus;

public interface ITripObserver {

    void updateView(String task, String tripId, TripStatus status);
}
