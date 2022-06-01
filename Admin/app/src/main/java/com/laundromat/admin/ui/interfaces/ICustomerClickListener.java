package com.laundromat.admin.ui.interfaces;

import com.laundromat.admin.model.Customer;
import com.laundromat.admin.model.Merchant;

public interface ICustomerClickListener {

    void onCustomerClick(int index, Customer customer);
}
