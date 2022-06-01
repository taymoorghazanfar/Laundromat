package com.laundromat.merchant.ui.interfaces;

import com.laundromat.merchant.model.Laundry;

public interface ILaundrySignupListener {

    void onLaundrySignup(Laundry laundry);

    void onButtonPreviousClick();
}
