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

/**
 * 数据处理部分
 * 输入数据字符串、点格式化
 * <p>
 * 预设参数：温度、实时数据×2
 * temperature, realTimeData1, realTimeData2
 * <p>
 * 要求：
 * 温度显示当前状态，断开时(读入为空时显示'--')
 * 实时数据显示time_interval = 20s范围的数据
 * 保存所有数据为txt/csv
 */
public class DataHandler {

    //待输入格式确定后重构

    /**
     * @param str
     * @return Comma：'Temperature,20,RTD1,0,RTD2,0'
     */
    private String StringToComma(String str) {
        return null;
    }
}
