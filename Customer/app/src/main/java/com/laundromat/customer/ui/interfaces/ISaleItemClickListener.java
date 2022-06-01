package com.laundromat.customer.ui.interfaces;

import com.laundromat.customer.model.order.SaleItem;

public interface ISaleItemClickListener {

    void onSaleItemClick(String serviceTypeName, SaleItem saleItem);
}
