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

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

public class SecondActivity extends AppCompatActivity {
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
    private Button button;
    private TextView textView, techSupport;
    private PendingIntent pendingIntent;
    private NfcAdapter nfcAdapter;
    private Tag tag;
    private String tech;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int refreshTime = 100;       //ms
            //要做的事情，这里再次调用此Runnable对象，以实现定时器操作
            System.out.println(tech);
            String readData = readNfc(tech, tag);
            try {
                if (readData == null || readData.equals("null")) {
                    System.out.println("null");
                    handler.removeCallbacks(runnable);
                    return;
                }
                String stringdata = StringHandler.hexToUtf8(readData);
                textView.setText(textView.getText() + "\n" + stringdata);
            } catch (Exception e) {
                userToast("Error data", Toast.LENGTH_SHORT);
                textView.setText(textView.getText() + "\n" + readData);
            }
            handler.postDelayed(this, refreshTime);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        techSupport = findViewById(R.id.techSupport);
        techSupport.setMovementMethod(ScrollingMovementMethod.getInstance());

        button = findViewById(R.id.clear_text);
        button.setOnClickListener(view -> {
            textView.setText("");
            //techSupport.setText("");
        });
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
                    textView.setText("");
                    tag = newtag;
                } else {
                    System.out.println(oldId + '\t' + newId);
                    System.out.println("Same Card.");
                }
            } else {
                tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                textView.setText("");
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

    //选择对应协议读取，按前设列表优选
    private String readNfc(String tech, Tag tag) {
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

    //首次读取
    private void processIntent(Tag tag) {
        String[] techlist = tag.getTechList();
        StringBuilder stringBuilder = new StringBuilder(NfcReader.readId(tag.getId()));
        stringBuilder.insert(0, "ID: ").append('\n');
        for (String techspt : techlist) {
            stringBuilder.append(techspt).append('\n');
        }
        techSupport.setText(stringBuilder.toString());

        String readData;
        boolean decodeable = false;
        //检测支持协议
        for (String tech : nfcTechList) {
            if (Arrays.asList(techlist).contains(tech)) {
                this.tech = tech;
                decodeable = true;
                //userToast(tech);
                readData = readNfc(tech, tag);
                try {
                    String stringdata = StringHandler.hexToUtf8(readData);
                    textView.setText(textView.getText() + "\n" + stringdata);
                } catch (Exception e) {
                    userToast("Error data", Toast.LENGTH_SHORT);
                    textView.setText(textView.getText() + "\n" + readData);
                }
                break;
            }
        }
        /*
        //NFC是否可读
        if (decodeable) {
            userToast("success.");
        } else {
            userToast("fail.");
        }
         */
    }

    //显示消息
    private void userToast(String src, int type) {
        Toast.makeText(this, src, type).show();
    }
}