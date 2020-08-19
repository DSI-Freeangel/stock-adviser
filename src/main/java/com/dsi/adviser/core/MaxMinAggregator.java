package com.dsi.adviser.core;

import lombok.Getter;

@Getter
public class MaxMinAggregator {
    private Double max;
    private Double min;

    public void add(Double value) {
        if(max == null || max < value) {
            max = value;
        }
        if(min == null || min > value) {
            min = value;
        }
    }
}