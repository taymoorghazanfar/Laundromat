package com.laundromat.customer.utils;

public class NumberUtils {

    public static double getDiscount(double discount, double actualAmount) {

        return actualAmount * (discount / 100.0f);
    }
}
