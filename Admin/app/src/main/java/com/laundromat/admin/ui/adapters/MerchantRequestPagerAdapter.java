package com.laundromat.admin.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.admin.fragments.LaundryRequestProfileFragment;
import com.laundromat.admin.fragments.MerchantRequestProfileFragment;

public class MerchantRequestPagerAdapter extends FragmentPagerAdapter {

    private String merchantGson;
    private String laundryGson;

    public MerchantRequestPagerAdapter(@NonNull FragmentManager fm, int behavior,
                                       String merchantGson, String laundryGson) {
        super(fm, behavior);

        this.merchantGson = merchantGson;
        this.laundryGson = laundryGson;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return MerchantRequestProfileFragment.newInstance(merchantGson);

            case 1:
                return LaundryRequestProfileFragment.newInstance(laundryGson);

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
                return "Laundry";
        }

        return null;
    }
}
