package com.example.nfc_handler;

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

import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.lang.ref.WeakReference;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private static boolean prepared = false;
    private static PendingIntent pendingIntent;
    private static NfcAdapter nfcAdapter;
    private static Tag tag;
    private static String tech;
    private static Button start;
    private static Button stop;
    private static Button changePage;
    private static ECGModel ecgSeries1;
    private static ECGModel ecgSeries2;
    private static XYPlot xyPlot1;
    private static XYPlot xyPlot2;
    private static TextView temperature;
    private static Redrawer redrawer1;
    private static Redrawer redrawer2;
    //nfc读写协议，按列表排序有限使用
    private final String[] nfcTechList = {
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
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int refreshTime = 100;       //ms
            if (!prepared) {
                return;
            }
            //要做的事情，这里再次调用此Runnable对象，以实现定时器操作
            System.out.println(tech);
            String readData = readNfc(tech, tag);
            if (readData == null || readData.equals("null")) {
                System.out.println("null");
                handler.removeCallbacks(runnable);
                return;
            }
            //textView.setText(textView.getText() + "\n" + readData);
            handler.postDelayed(this, refreshTime);
        }
    };

    //选择对应协议读取，按前设列表优选
    private static String readNfc(String tech, Tag tag) {
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化基本控件
        temperature = findViewById(R.id.temperature);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        stop.setClickable(false);

        start.setOnClickListener(view -> {
            start.setClickable(false);
            stop.setClickable(true);
            prepared = true;
        });

        stop.setOnClickListener(view -> {
            stop.setClickable(false);
            stop.setClickable(true);
            prepared = false;
        });

        //初始化NFC检测事件
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }

        //调试界面跳转按钮
        //测试阶段保留
        changePage = findViewById(R.id.changePage);
        changePage.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            // start the activity connect to the specified class
            startActivity(intent);
        });

        xyPlot1 = findViewById(R.id.plot1);
        xyPlot2 = findViewById(R.id.plot2);

        //调整绘图背景
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.FILL);
        xyPlot1.getGraph().setBackgroundPaint(backgroundPaint);
        xyPlot2.getGraph().setBackgroundPaint(backgroundPaint);

        //ECG随机数发生器，用于测试实时数据的绘图
        ecgSeries1 = new ECGModel(1000, 200);
        ecgSeries2 = new ECGModel(1000, 200);

        MyFadeFormatter formatter = new MyFadeFormatter(1000);
        formatter.setLegendIconEnabled(false);

        xyPlot1.addSeries(ecgSeries1, formatter);
        xyPlot1.setRangeBoundaries(0, 10, BoundaryMode.FIXED);
        xyPlot1.setDomainBoundaries(0, 1000, BoundaryMode.FIXED);

        // reduce the number of range labels
        xyPlot1.setLinesPerRangeLabel(3);

        xyPlot2.addSeries(ecgSeries2, formatter);
        xyPlot2.setRangeBoundaries(0, 10, BoundaryMode.FIXED);
        xyPlot2.setDomainBoundaries(0, 1000, BoundaryMode.FIXED);

        xyPlot2.setLinesPerRangeLabel(3);
    }

    @Override
    public void onStop() {
        super.onStop();
        redrawer1.finish();
        redrawer2.finish();
    }

    //NFC探测事件
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("On New Intent.");
        System.out.println(intent.getAction());
        if (!prepared) {
            System.out.println("Cancel");
            return;
        }
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
                    //textView.setText("");
                    tag = newtag;
                } else {
                    System.out.println(oldId + '\t' + newId);
                    System.out.println("Same Card.");
                }
            } else {
                tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                //textView.setText("");
            }
            processIntent(tag);
            ecgSeries1.start(new WeakReference<>(xyPlot2.getRenderer(AdvancedLineAndPointRenderer.class)));
            ecgSeries2.start(new WeakReference<>(xyPlot2.getRenderer(AdvancedLineAndPointRenderer.class)));
            redrawer1 = new Redrawer(xyPlot1, 30, true);
            redrawer2 = new Redrawer(xyPlot2, 30, true);
            //handler.postDelayed(runnable, 1000);
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

    //首次读取
    private void processIntent(Tag tag) {
        String[] techlist = tag.getTechList();
        String readData = "";
        boolean decodeable = false;
        //检测支持协议
        for (String tech : nfcTechList) {
            if (Arrays.asList(techlist).contains(tech)) {
                MainActivity.tech = tech;
                decodeable = true;
                userToast(tech);
                readData = readNfc(tech, tag);
                //textView.setText(textView.getText() + "\n" + readData);
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

    //显示消息
    private void userToast(String src) {
        Toast.makeText(this, src, Toast.LENGTH_SHORT).show();
    }

    /**
     * Special {@link AdvancedLineAndPointRenderer.Formatter} that draws a line
     * that fades over time.  Designed to be used in conjunction with a circular buffer model.
     */
    public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

        private final int trailSize;

        MyFadeFormatter(int trailSize) {
            this.trailSize = trailSize;
        }

        @Override
        public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
            // offset from the latest index:
            int offset;
            if (thisIndex > latestIndex) {
                offset = latestIndex + (seriesSize - thisIndex);
            } else {
                offset = latestIndex - thisIndex;
            }

            float scale = 255f / trailSize;
            int alpha = (int) (255 - (offset * scale));
            getLinePaint().setAlpha(Math.max(alpha, 0));
            return getLinePaint();
        }
    }

    /**
     * Primitive simulation of some kind of signal.  For this example,
     * we'll pretend its an ecg.  This class represents the data as a circular buffer;
     * data is added sequentially from left to right.  When the end of the buffer is reached,
     * i is reset back to 0 and simulated sampling continues.
     */
    public static class ECGModel implements XYSeries {

        //private final Number[] data;
        private final Number[] realTimeData1;
        private final Number[] realTimeData2;
        private final long delayMs;
        private final int blipInteral;
        private final Thread thread;
        private boolean keepRunning;
        private int latestIndex;

        private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

        /**
         * @param size         Sample size contained within this model
         * @param updateFreqHz Frequency at which new samples are added to the model
         */
        ECGModel(int size, int updateFreqHz) {
            //data = new Number[size];
            realTimeData1 = new Number[size];
            realTimeData2 = new Number[size];

            //Arrays.fill(data, 0);
            Arrays.fill(realTimeData1, 0);
            Arrays.fill(realTimeData2, 0);

            // translate hz into delay (ms):
            delayMs = 1000 / updateFreqHz;

            // add 7 "blips" into the signal:
            blipInteral = size / 7;

            thread = new Thread(() -> {
                try {
                    while (keepRunning) {

                        //重写0-data.length这段数据
                        if (latestIndex >= realTimeData1.length) {
                            latestIndex = 0;
                        }

                        //理想读取结果    Temperature,20,RTD1,0,RTD2,0
                        //或者            20,0,0
                        //否则还需要编写StringHandler
                        try {
                            //System.out.println(tech);
                            String readData = readNfc(tech, tag);
                            if (!prepared || readData == null || readData.equals("null")) {
                                //System.out.println("null");
                                continue;
                            }

                            temperature.setText(readData.split(",")[0]);
                            realTimeData1[latestIndex] = Float.valueOf(readData.split(",")[1]);
                            realTimeData2[latestIndex] = Float.valueOf(readData.split(",")[2]);
                        } catch (Exception e) {
                            e.getStackTrace();
                            continue;
                        }

                        if (latestIndex < realTimeData1.length - 1) {
                            // null out the point immediately following i, to disable
                            // connecting i and i+1 with a line:
                            realTimeData1[latestIndex + 1] = null;
                            realTimeData2[latestIndex + 1] = null;
                        }

                        if (rendererRef.get() != null) {
                            rendererRef.get().setLatestIndex(latestIndex);
                            Thread.sleep(delayMs);
                        } else {
                            keepRunning = false;
                        }
                        latestIndex++;
                    }
                } catch (InterruptedException e) {
                    keepRunning = false;
                }
            });
        }

        void start(final WeakReference<AdvancedLineAndPointRenderer> rendererRef) {
            this.rendererRef = rendererRef;
            keepRunning = true;
            thread.start();
        }

        @Override
        public int size() {
            return realTimeData1.length;
        }

        @Override
        public Number getX(int index) {
            return index;
        }

        @Override
        public Number getY(int index) {
            return realTimeData1[index];
        }

        @Override
        public String getTitle() {
            return "Signal";
        }
    }
}