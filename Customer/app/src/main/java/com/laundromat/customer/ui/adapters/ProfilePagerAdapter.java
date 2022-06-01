package com.laundromat.customer.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.customer.fragments.AddressesFragment;
import com.laundromat.customer.fragments.UserProfileFragment;

public class ProfilePagerAdapter extends FragmentPagerAdapter {

    public ProfilePagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return new UserProfileFragment();

            case 1:
                return new AddressesFragment();

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
                return "Addresses";
        }

        return null;
    }
}
