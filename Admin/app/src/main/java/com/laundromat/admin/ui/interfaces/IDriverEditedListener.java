package com.laundromat.admin.ui.interfaces;

public interface IDriverEditedListener {

    void onDriverEdited(String avatarUrl, String fullName, String email,
                        String jazzCashNumber, String licenseNumber);
}
