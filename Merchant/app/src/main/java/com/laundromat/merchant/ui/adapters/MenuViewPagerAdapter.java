package com.laundromat.merchant.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuViewPagerAdapter extends FragmentPagerAdapter {

    public Map<String, Fragment> fragments;

    public MenuViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Map<String, Fragment> fragments) {
        super(fm, behavior);
        this.fragments = fragments;
    }

    public Map<String, Fragment> getFragments() {
        return fragments;
    }

    public void setFragments(Map<String, Fragment> fragments) {
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        return (new ArrayList<>(fragments.keySet())).get(position);
    }
}