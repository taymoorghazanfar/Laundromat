package com.laundromat.delivery.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.delivery.fragments.DriverProfileFragment;
import com.laundromat.delivery.fragments.VehicleProfileFragment;

public class ProfilePagerAdapter extends FragmentPagerAdapter {

    public ProfilePagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return new DriverProfileFragment();

            case 1:
                return new VehicleProfileFragment();

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
                return "Rider";

            case 1:
                return "Vehicle";
        }

        return null;
    }
}
