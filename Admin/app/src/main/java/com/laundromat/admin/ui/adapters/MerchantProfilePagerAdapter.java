package com.laundromat.admin.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.admin.fragments.LaundryProfileFragment;
import com.laundromat.admin.fragments.LaundryRequestProfileFragment;
import com.laundromat.admin.fragments.MerchantProfileFragment;
import com.laundromat.admin.fragments.MerchantRequestProfileFragment;
import com.laundromat.admin.fragments.TransactionsFragment;

public class MerchantProfilePagerAdapter extends FragmentPagerAdapter {

    private String merchantGson;
    private String laundryGson;
    private String transactionsGson;
    private int index;

    public MerchantProfilePagerAdapter(@NonNull FragmentManager fm, int behavior,
                                       String merchantGson, String laundryGson,
                                       String transactionsGson, int index) {
        super(fm, behavior);

        this.merchantGson = merchantGson;
        this.laundryGson = laundryGson;
        this.transactionsGson = transactionsGson;
        this.index = index;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return MerchantProfileFragment.newInstance(merchantGson, index);

            case 1:
                return LaundryProfileFragment.newInstance(laundryGson, merchantGson, index);

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
                return "Laundry";

            case 2:
                return "Transactions";
        }

        return null;
    }
}
