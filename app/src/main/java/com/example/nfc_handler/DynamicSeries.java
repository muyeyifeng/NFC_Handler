package com.example.nfc_handler;

import com.androidplot.xy.XYSeries;

import java.util.Arrays;

class DynamicSeries implements XYSeries {
    private final int size;
    private final Number[] data;
    private final String title;

    DynamicSeries(int size, String title) {
        this.size = size;
        data = new Number[size];
        Arrays.fill(data, 0);
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Number getX(int index) {
        if (index > size)
            throw new IllegalArgumentException();
        return index;
    }

    @Override
    public Number getY(int index) {
        if (index > size)
            throw new IllegalArgumentException();
        return data[index];
    }

    public void update(int index, double num) {
        if (index > size)
            throw new IllegalArgumentException();
        data[index] = num;
    }

    public void clear() {
        Arrays.fill(data, 0);
    }
}
