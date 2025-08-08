package com.dsi.adviser.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateUtilsTest {
    private static final Map<Integer, Integer> quarters = new HashMap<>() {{
        put(1, 1);
        put(2, 1);
        put(3, 1);
        put(4, 4);
        put(5, 4);
        put(6, 4);
        put(7, 7);
        put(8, 7);
        put(9, 7);
        put(10, 10);
        put(11, 10);
        put(12, 10);
    }};

    @Test
    public void testQuarters() {
        for(Map.Entry<Integer, Integer> entry : quarters.entrySet()) {
            assertEquals(entry.getValue().intValue(), DateUtils.getFirstMonthOfQuarter(LocalDate.of(2020, entry.getKey(), 1)).getMonthValue());
        }
    }
}
