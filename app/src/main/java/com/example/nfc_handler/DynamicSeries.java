/*
 * Copyright 2022 muyeyifeng
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
