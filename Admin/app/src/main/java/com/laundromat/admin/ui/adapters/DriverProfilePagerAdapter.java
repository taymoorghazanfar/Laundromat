package com.laundromat.admin.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.admin.fragments.DriverProfileFragment;
import com.laundromat.admin.fragments.TransactionsFragment;
import com.laundromat.admin.fragments.VehicleProfileFragment;

public class DriverProfilePagerAdapter extends FragmentPagerAdapter {

    private String driverGson;
    private String vehicleGson;
    private String transactionsGson;
    private int index;

    public DriverProfilePagerAdapter(@NonNull FragmentManager fm, int behavior,
                                     String driverGson, String vehicleGson,
                                     String transactionsGson, int index) {
        super(fm, behavior);

        this.driverGson = driverGson;
        this.vehicleGson = vehicleGson;
        this.transactionsGson = transactionsGson;
        this.index = index;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return DriverProfileFragment.newInstance(driverGson, index);

            case 1:
                return VehicleProfileFragment.newInstance(vehicleGson, index);

            case 2:
                return TransactionsFragment.newInstance(transactionsGson);

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
                return "Profile";

            case 1:
                return "Vehicle";

            case 2:
                return "Transactions";
        }

        return null;
    }
}
