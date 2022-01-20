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

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

import java.text.DecimalFormat;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    private static boolean prepared = false;
    private static PendingIntent pendingIntent;
    private static NfcAdapter nfcAdapter;
    private static Tag tag;
    private static String tech;
    private static DynamicSeries rtd1;
    private static DynamicSeries rtd2;
    //nfc读写协议，按列表排序有限使用
    private final String[] nfcTechList= {
            "android.nfc.tech.NfcV",
            "android.nfc.tech.Ndef",
            "android.nfc.tech.MifareClassic",
            "android.nfc.tech.MifareUltralight",
            "android.nfc.tech.NdefFormatable",
            "android.nfc.tech.NfcA",
            "android.nfc.tech.NfcB",
            "android.nfc.tech.NfcF",
    };
    private final Handler handler = new Handler();
    private TextView temperature;
    private XYPlot xyPlot1;
    private XYPlot xyPlot2;
    private Button start;
    private Button stop;
    private int count;
    private final Runnable runnable = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            int refreshTime = 100;       //ms
            if (!prepared) {
                handler.postDelayed(this, refreshTime);
                return;
            }
            //要做的事情，这里再次调用此Runnable对象，以实现定时器操作
            System.out.println(tech);
            String readData = readNfc(tech, tag);
            if (readData == null || readData.equals("null")) {
                System.out.println("null");
                pause();
                handler.removeCallbacks(runnable);
                return;
            }
            String[] stringData = StringHandler.hexToUtf8(readData).split(",");
            temperature.setText(stringData[0].substring(2) + "°C");
            rtd1.update(count, Double.parseDouble(stringData[1]));
            rtd2.update(count, Double.parseDouble(stringData[2]));
            plotDynamic(rtd1, rtd2);
            count++;
            count = count % rtd1.size();
            handler.postDelayed(this, refreshTime);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVar();
        initWegit();
        initEvent();
        initPlot();
        initDebug();
    }

    //NFC探测事件
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("On New Intent.");
        System.out.println(intent.getAction());
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

            //是否为同一张NFC卡片
            if (tag != null) {
                Tag newtag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                byte[] dataId = tag.getId();
                byte[] newDataId = newtag.getId();
                String oldId = NfcReader.readId(dataId);
                String newId = NfcReader.readId(newDataId);

                if (!oldId.equals(newId)) {
                    System.out.println("Different Card.");
                    reset();
                    tag = newtag;
                } else {
                    System.out.println(oldId + '\t' + newId);
                    System.out.println("Same Card.");
                }
            } else {
                tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            }
            processIntent(tag);
            handler.postDelayed(runnable, 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("On Resume.");
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("On Pause.");
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    //初始化全局变量
    public void initVar(){
        count = 0;
        int size = 11;
        rtd1 = new DynamicSeries(size, "RTD 1");
        rtd2 = new DynamicSeries(size, "RTD 2");
    }

    //初始化控件
    public void initWegit(){
        temperature = findViewById(R.id.temperature);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        stop.setClickable(false);
        stop.setBackgroundColor(Color.GRAY);

        start.setOnClickListener(view -> {
            start.setClickable(false);
            start.setBackgroundColor(Color.GRAY);

            stop.setClickable(true);
            stop.setBackgroundColor(Color.rgb(255, 0, 0));
            prepared = true;
        });

        stop.setOnClickListener(view -> {
            stop.setClickable(false);
            stop.setBackgroundColor(Color.GRAY);

            start.setClickable(true);
            start.setBackgroundColor(Color.rgb(37, 86, 234));
            prepared = false;
            reset();
        });
    }

    //定义NFC事件
    public void initEvent(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
    }

    //初始化绘图参数
    public void initPlot(){
        //初始化绘图控件
        xyPlot1 = findViewById(R.id.plot1);
        xyPlot2 = findViewById(R.id.plot2);

        //调整绘图背景
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.FILL);

        xyPlot1.getGraph().setBackgroundPaint(backgroundPaint);
        xyPlot1.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new DecimalFormat("0"));
        xyPlot1.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("0"));
        xyPlot1.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        xyPlot1.setDomainStepValue(1);
        xyPlot1.setRangeBoundaries(0, 100, BoundaryMode.FIXED);
        xyPlot1.setDomainBoundaries(0, 10, BoundaryMode.FIXED);

        xyPlot2.getGraph().setBackgroundPaint(backgroundPaint);
        xyPlot2.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new DecimalFormat("0"));
        xyPlot2.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("0"));
        xyPlot2.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        xyPlot2.setDomainStepValue(1);
        xyPlot2.setRangeBoundaries(0, 100, BoundaryMode.FIXED);
        xyPlot2.setDomainBoundaries(0, 10, BoundaryMode.FIXED);
    }

    //debug界面跳转
    public void initDebug(){
        //测试阶段保留
        Button changePage = findViewById(R.id.changePage);
        changePage.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            // start the activity connect to the specified class
            startActivity(intent);
        });
    }

    //首次读取
    private void processIntent(Tag tag) {
        String[] techlist = tag.getTechList();
        boolean decodeable = false;
        //检测支持协议
        for (String tech : nfcTechList) {
            if (Arrays.asList(techlist).contains(tech)) {
                MainActivity.tech = tech;
                decodeable = true;
                userToast(tech);
                break;
            }
        }

        //NFC是否可读
        if (decodeable) {
            userToast("success.");
        } else {
            userToast("fail.");
        }
    }

    //选择对应协议读取，按前设列表优选
    public String readNfc(String tech, Tag tag) {
        switch (tech) {
            case "android.nfc.tech.IsoDep":
                return NfcReader.readIsoDep(tag);
            case "android.nfc.tech.NfcA":
                return NfcReader.readNfcA(tag);
            case "android.nfc.tech.NfcB":
                return NfcReader.readNfcB(tag);
            case "android.nfc.tech.NfcF":
                return NfcReader.readNfcF(tag);
            case "android.nfc.tech.NfcV":
                return NfcReader.readNfcV(tag);
            case "android.nfc.tech.Ndef":
                return NfcReader.readNdef(tag);
            case "android.nfc.tech.NdefFormatable":
                return NfcReader.readNdefFormatable(tag);
            case "android.nfc.tech.MifareUltralight":
                return NfcReader.readMifareUltralight(tag);
            case "android.nfc.tech.MifareClassic":
                return NfcReader.readMifareClassic(tag);
        }
        return null;
    }

    //暂停读取
    public void pause() {
        prepared=false;
    }

    //重置图表
    public void reset() {
        rtd1.clear();
        rtd2.clear();
        plotDynamic(rtd1, rtd2);
        temperature.setText("--");
        count = 0;
    }

    //绘制图表
    public void plotDynamic(DynamicSeries dynamicSeries1, DynamicSeries dynamicSeries2) {
        LineAndPointFormatter formatter1 = new LineAndPointFormatter(Color.GREEN, null, null, null);
        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter1.getLinePaint().setStrokeWidth(10);

        xyPlot1.addSeries(dynamicSeries1, formatter1);
        xyPlot1.redraw();

        LineAndPointFormatter formatter2 = new LineAndPointFormatter(Color.RED, null, null, null);
        formatter2.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter2.getLinePaint().setStrokeWidth(10);

        xyPlot2.addSeries(dynamicSeries2, formatter2);
        xyPlot2.redraw();
    }

    //显示消息
    private void userToast(String src) {
        Toast.makeText(this, src, Toast.LENGTH_SHORT).show();
    }
}