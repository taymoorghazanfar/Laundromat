package com.laundromat.merchant.ui.interfaces;

import com.laundromat.merchant.model.washable.WashableItemCategory;

public interface IMenuCategoryClickListener {

    void onMenuCategoryClick(WashableItemCategory menuCategory, int index);

    void onMenuCategoryEditClick(WashableItemCategory menuCategory, int index);

    void onMenuCategoryDeleteClick(int position);
}
