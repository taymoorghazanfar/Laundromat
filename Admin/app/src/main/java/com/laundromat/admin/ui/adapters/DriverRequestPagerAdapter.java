package com.laundromat.admin.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.admin.fragments.DriverRequestProfileFragment;
import com.laundromat.admin.fragments.VehicleRequestProfileFragment;

public class DriverRequestPagerAdapter extends FragmentPagerAdapter {

    private String driverGson;
    private String vehicleGson;

    public DriverRequestPagerAdapter(@NonNull FragmentManager fm, int behavior,
                                     String driverGson, String vehicleGson) {
        super(fm, behavior);

        this.driverGson = driverGson;
        this.vehicleGson = vehicleGson;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return DriverRequestProfileFragment.newInstance(driverGson);

            case 1:
                return VehicleRequestProfileFragment.newInstance(vehicleGson);

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
                return "Vehicle";
        }

        return null;
    }
}

