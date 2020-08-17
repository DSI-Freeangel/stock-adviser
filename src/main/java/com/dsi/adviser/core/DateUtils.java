package com.dsi.adviser.core;

import java.time.LocalDate;

public class DateUtils {
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    public static LocalDate getFirstMonthOfQuarter(LocalDate date) {
        LocalDate firstDayOfMonth = getFirstDayOfMonth(date);
        int month = ((firstDayOfMonth.getMonthValue() - 1) / 3 + 1) * 3 - 2;
        return firstDayOfMonth.withMonth(month);
    }

    public static LocalDate getFirstMonthOfYear(LocalDate date) {
        return getFirstDayOfMonth(date).withMonth(1);
    }
}
