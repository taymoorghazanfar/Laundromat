package com.laundromat.delivery.model.observers;

import com.laundromat.delivery.model.util.TripStatus;

public interface ITripSubject {

    void registerObserver(ITripObserver observer);

    void notifyObservers(String task, String tripId, TripStatus status);
}
