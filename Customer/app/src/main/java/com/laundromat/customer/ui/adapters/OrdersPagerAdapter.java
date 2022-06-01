package com.laundromat.customer.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.customer.fragments.CurrentOrdersFragment;
import com.laundromat.customer.fragments.PastOrdersFragment;

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
                return new PastOrdersFragment();

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
                return "Current";

            case 1:
                return "History";
        }

        return null;
    }
}
