package com.laundromat.admin.ui.interfaces;

import com.google.android.gms.maps.model.LatLng;

public interface IMerchantEditedListener {

    void onMerchantEdited(String avatarUrl, String fullName,
                          String email,
                          String jazzCashNumber, LatLng location);
}
