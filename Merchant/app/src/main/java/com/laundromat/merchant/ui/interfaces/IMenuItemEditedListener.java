package com.laundromat.merchant.ui.interfaces;

import com.laundromat.merchant.model.washable.WashableItem;

public interface IMenuItemEditedListener {

    void onMenuItemEdited(WashableItem menuItem, int index);
}
