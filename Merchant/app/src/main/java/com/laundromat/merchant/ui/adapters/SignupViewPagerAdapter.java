package com.laundromat.merchant.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.laundromat.merchant.fragments.CompleteSignupFragment;
import com.laundromat.merchant.fragments.LaundrySignupFragment;
import com.laundromat.merchant.fragments.MerchantSignupFragment;

public class SignupViewPagerAdapter extends FragmentPagerAdapter {

    public SignupViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return new MerchantSignupFragment();

            case 1:
                return new LaundrySignupFragment();

            case 2:
                return new CompleteSignupFragment();

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
                return "Merchant Details";

            case 1:
                return "Laundry Details";

            case 2:
                return "Complete Signup";
        }
        return "";
    }
}
