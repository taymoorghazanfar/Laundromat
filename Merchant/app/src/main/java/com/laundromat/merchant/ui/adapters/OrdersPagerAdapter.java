package com.laundromat.merchant.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.merchant.fragments.CurrentOrdersFragment;
import com.laundromat.merchant.fragments.PastOrdersFragment;
import com.laundromat.merchant.fragments.RequestedOrdersFragment;

public class OrdersPagerAdapter extends FragmentPagerAdapter {

    public OrdersPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return new CurrentOrdersFragment();

            case 1:
                return new RequestedOrdersFragment();

            case 2:
                return new PastOrdersFragment();

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {

            case 0:
                return "Current";

            case 1:
                return "Requests";

            case 2:
                return "History";
        }

        return null;
    }
}
