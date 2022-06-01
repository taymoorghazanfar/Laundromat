package com.laundromat.admin.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.admin.fragments.CustomerProfileFragment;
import com.laundromat.admin.fragments.LaundryProfileFragment;
import com.laundromat.admin.fragments.MerchantProfileFragment;
import com.laundromat.admin.fragments.TransactionsFragment;

public class CustomerProfilePagerAdapter extends FragmentPagerAdapter {

    private String customerGson;
    private String transactionsGson;
    private int index;

    public CustomerProfilePagerAdapter(@NonNull FragmentManager fm, int behavior,
                                       String customerGson, String transactionsGson, int index) {
        super(fm, behavior);

        this.customerGson = customerGson;
        this.transactionsGson = transactionsGson;
        this.index = index;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return CustomerProfileFragment.newInstance(customerGson, index);

            case 1:
                return TransactionsFragment.newInstance(transactionsGson);

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {

            case 0:
                return "Profile";

            case 1:
                return "Transactions";
        }

        return null;
    }
}
