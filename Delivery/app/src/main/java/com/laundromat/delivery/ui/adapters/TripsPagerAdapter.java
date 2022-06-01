package com.laundromat.delivery.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.delivery.fragments.CurrentTripsFragment;
import com.laundromat.delivery.fragments.PastTripsFragment;
import com.laundromat.delivery.fragments.RequestedTripsFragment;

public class TripsPagerAdapter extends FragmentPagerAdapter {

    public TripsPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return new CurrentTripsFragment();

            case 1:
                return new RequestedTripsFragment();

            case 2:
                return new PastTripsFragment();

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
