package com.laundromat.customer.utils;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.laundromat.customer.model.order.Order;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DateUtils {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long getDurationBetweenDates(String date1, String date2) {

        if (date1 == null || TextUtils.isEmpty(date1)
                || date2 == null || TextUtils.isEmpty(date2)) {

            return 0;
        }

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        final LocalDate firstDate = LocalDate.parse(date1, formatter);
        final LocalDate secondDate = LocalDate.parse(date2, formatter);
        return ChronoUnit.DAYS.between(firstDate, secondDate);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long getLaundryDeliveryDuration(List<Order> orders) {

        if (orders == null || orders.size() == 0) {

            return 0;
        }
        ArrayList<Long> durations = new ArrayList<>();

        for (Order order : orders) {

            long duration = getDurationBetweenDates
                    (order.getDateCreated(), order.getDateCompleted());

            durations.add(duration);
        }

        long duration = 0;

        for (Long time : durations) {

            duration += time;
        }

        return duration / durations.size();
    }

    public static boolean isSameDay(String date) {

        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String todayDate = df.format(c.getTime());

        String compareDate = date.substring(0, Math.min(date.length(), 10));

        Log.d("compare_dates", "isSameDay: today: " + todayDate + ", compare: " + compareDate);
        return todayDate.equals(compareDate);
    }
}
