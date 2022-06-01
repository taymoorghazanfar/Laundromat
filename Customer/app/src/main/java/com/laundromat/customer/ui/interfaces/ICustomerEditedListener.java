package com.laundromat.customer.ui.interfaces;

public interface ICustomerEditedListener {

    void onCustomerEdited(String avatarUrl, String fullName,
                          String phoneNumber, String email);
}
