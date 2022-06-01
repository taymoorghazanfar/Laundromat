package com.laundromat.delivery.model.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.laundromat.delivery.R;
import com.laundromat.delivery.model.Laundry;
import com.laundromat.delivery.model.order.OrderItem;
import com.laundromat.delivery.model.order.SaleItem;
import java.util.HashMap;
import java.util.Map;

public class Cart {

    private Laundry laundry;
    //<ServiceTypeName, List<SaleItems>> ie. <Dry Clean, List<Shirt, Pant....>>
    private Map<String, OrderItem> orderItems;
    private double price;

    public Cart() {

        this.orderItems = new HashMap<>();
    }

    public Cart(Laundry laundry, Map<String, OrderItem> orderItems, double price) {
        this.laundry = laundry;
        this.orderItems = orderItems;
        this.price = price;
    }

    public Laundry getLaundry() {
        return laundry;
    }

    public void setLaundry(Laundry laundry) {
        this.laundry = laundry;
    }

    public Map<String, OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Map<String, OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Utility methods
    public boolean isEmpty() {

        if (laundry == null) {
            return true;
        }

        if (orderItems.size() > 0) {

            for (String serviceTypeName : orderItems.keySet()) {

                if (orderItems.get(serviceTypeName).getSaleItems().size() > 0) {

                    return false;
                }
            }
        }

        return true;
    }

    public void clearCart(Context context) {

        laundry = null;
        orderItems.clear();
        price = 0;
//        CartPrefs.delete(context);
    }

    public void addItemsToCart(Context context, Laundry laundry, Map<String, OrderItem> orderItems) {

        // if there are already some items in the cart
        if (!isEmpty()) {

            // if the items already in cart are from another laundry
            if (!this.laundry.getName().equals(laundry.getName())) {

                AlertDialog.Builder alert;
                alert = new AlertDialog.Builder(context);
                alert.setTitle("Remove previous items ?");
                alert.setCancelable(false);
//                alert.setMessage(R.string.cart_not_empty);

                // remove previous items and add new one
                alert.setPositiveButton("YES", (dialog, whichButton) -> {

                    clearCart(context);
                    setLaundry(laundry);
                    addOrderItems(context, orderItems);
                });

                // cancel the add item process
                alert.setNegativeButton("NO", (dialogInterface, i)
                        -> dialogInterface.dismiss());

                alert.show();
            }
            // items in cart are from the same laundry
            else {

                addOrderItems(context, orderItems);
            }

            // if cart is empty
        } else {

            this.setLaundry(laundry);
            addOrderItems(context, orderItems);
        }
    }

    private void addOrderItems(Context context, Map<String, OrderItem> newOrderItems) {

        if (orderItems.size() > 0) {

            for (String serviceType : newOrderItems.keySet()) {

                // if order item already exist (ie. dry clean)
                if (orderItems.containsKey(serviceType)) {

                    // if there are any sale items with the order item
                    if (orderItems.get(serviceType).getSaleItems().size() > 0) {

                        // loop through new order item's sale items
                        for (String saleItem : newOrderItems.get(serviceType).getSaleItems().keySet()) {

                            // if there is a sale item (ie shirt) that is also in new order items
                            if (orderItems.get(serviceType).getSaleItems().containsKey(saleItem)) {

                                // update quantity
                                orderItems.get(serviceType).getSaleItems().get(saleItem)
                                        .setQuantity(newOrderItems.get(serviceType)
                                                .getSaleItems().get(saleItem).getQuantity());

                                // update price
                                orderItems.get(serviceType).getSaleItems().get(saleItem)
                                        .setPrice(newOrderItems.get(serviceType)
                                                .getSaleItems().get(saleItem).getPrice());

                            } else {

                                // add the sale item (ie) to order item (ie dry clean)
                                orderItems.get(serviceType).getSaleItems()
                                        .put(saleItem, newOrderItems
                                                .get(serviceType).getSaleItems().get(saleItem));
                            }
                        }

                        // add new sale items to the order item
                    } else {

                        orderItems.get(serviceType)
                                .setSaleItems(newOrderItems.get(serviceType).getSaleItems());
                    }

                } else {

                    orderItems.put(serviceType, newOrderItems.get(serviceType));
                }
            }
        } else {

            this.orderItems = newOrderItems;
        }

        //set cart price
        this.price = getTotalPrice();

//        CartPrefs.set(context, this);
    }

    public void showCart() {

        Log.d("cart", "showCart: " + laundry.getName());
        for (String serviceType : orderItems.keySet()) {

            Log.d("cart", "order item: " + serviceType);
            for (String saleItem : orderItems.get(serviceType).getSaleItems().keySet()) {

                Log.d("cart", "sale item name: " + saleItem);
                Log.d("cart", "sale item quantity: " + orderItems.get(serviceType).getSaleItems().get(saleItem).getQuantity());
                Log.d("cart", "sale item price " + orderItems.get(serviceType).getSaleItems().get(saleItem).getPrice());
            }

            Log.d("cart", "\n\n");
        }
    }

    public int getServiceQuantity(String serviceTypeName, String washableItemName) {

        // if there is a already a service type with provided name (ie. dry clean)
        if (orderItems.containsKey(serviceTypeName)) {

            // if there is already a sale item with provided sale item name (ie. shirt)
            if (orderItems.get(serviceTypeName).getSaleItems().containsKey(washableItemName)) {

                // return its quantity (ie. washing x3)
                return orderItems.get(serviceTypeName)
                        .getSaleItems().get(washableItemName).getQuantity();
            }
            // if there is no sale item with provided sale item name (ie. shirt)
            else {

                return -1;
            }
        }
        // if there is no service type with provided name (ie. dry clean)
        else {

            return -1;
        }
    }

    public int getTotalSaleItems() {

        int total = 0;

        for (String serviceTypeName : orderItems.keySet()) {

            for (SaleItem saleItem : orderItems.get(serviceTypeName).getSaleItems().values()) {

                total = total + saleItem.getQuantity();
            }
        }

        return total;
    }

    private double getTotalPrice() {

        double total = 0;

        for (String serviceTypeName : orderItems.keySet()) {

            double salePrice = 0;

            for (SaleItem saleItem : orderItems.get(serviceTypeName).getSaleItems().values()) {

                salePrice = salePrice + saleItem.getPrice();
            }

            total = total + salePrice;
        }

        return total;
    }
}
