package com.laundromat.merchant.ui.interfaces;

import com.laundromat.merchant.model.Laundry;
import com.laundromat.merchant.model.Merchant;

public interface ISignupListener {

    void onSignupComplete(Merchant merchant, Laundry laundry);
}
