package com.laundromat.merchant.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

    public static boolean isSameDay(String date) {

        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String todayDate = df.format(c.getTime());

        String compareDate = date.substring(0, Math.min(date.length(), 10));

        return todayDate.equals(compareDate);
    }
}

