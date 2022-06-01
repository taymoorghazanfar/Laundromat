package com.laundromat.admin.interfaces;

import com.laundromat.admin.model.Merchant;

public interface IMerchantConfirmationListener {

    void onMerchantConfirmationClick(Merchant merchant, int index);
}