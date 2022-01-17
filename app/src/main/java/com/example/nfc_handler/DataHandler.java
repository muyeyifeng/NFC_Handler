package com.example.nfc_handler;

import android.util.Xml;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据处理部分
 * 输入数据字符串json（首选格式）化/XML化/逗点格式化
 *
 * 预设参数：温度、实时数据×2
 * temperature, realTimeData1, realTimeData2
 *
 * 要求：
 * 温度显示当前状态，断开时(读入为空时显示'--')
 * 实时数据显示time_interval = 20s范围的数据
 * 保存所有数据为txt/csv
 *
 */
public class DataHandler {
    public final int JSON = 1;
    public final int XML = 2;
    public final int Comma = 2;
    private final Map<String, String> Temperature = new HashMap<>();
    private final Map<String, String> realTimeData1 = new HashMap<>();
    private final Map<String, String> realTimeData2 = new HashMap<>();

    public Object stringTranslate(String str, int Type) {
        switch (Type) {
            case 1:
                return StringToJSON(str);
            case 2:
                return StringToXML(str);
            case 3:
                return StringToComma(str);
        }
        return null;
    }

    //待输入格式确定后重构
    /**
     * @param str
     * @return
     */
    private JSONObject StringToJSON(String str) {
        return null;
    }

    /**
     * @param str
     * @return
     */
    private Xml StringToXML(String str) {
        return null;
    }

    /**
     * @param str
     * @return Comma：'Temperature,20,RTD1,0,RTD2,0'
     */
    private String StringToComma(String str) {
        return null;
    }
}
