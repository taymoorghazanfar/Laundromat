package com.laundromat.merchant.ui.interfaces;

import com.laundromat.merchant.model.washable.WashableItem;

public interface IMenuItemClickListener {

    void onMenuItemClick(WashableItem menuItem);

    void onMenuItemEditClick(WashableItem menuItem, int index);

    void onMenuItemDeleteClick(int position);
}
