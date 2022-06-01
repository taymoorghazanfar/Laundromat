package com.laundromat.delivery.ui.interfaces;

public interface IDriverEditedListener {

    void onDriverEdited(String avatarUrl, String fullName,
                          String phoneNumber, String email,
                          String jazzCashNumber, String licenseNumber);
}
