package com.laundromat.merchant.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.merchant.fragments.LaundryProfileFragment;
import com.laundromat.merchant.fragments.MerchantProfileFragment;

public class ProfilePagerAdapter extends FragmentPagerAdapter {

    public ProfilePagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return new MerchantProfileFragment();

            case 1:
                return new LaundryProfileFragment();

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
                return "Merchant";

            case 1:
                return "Laundry";
        }

        return null;
    }
}
