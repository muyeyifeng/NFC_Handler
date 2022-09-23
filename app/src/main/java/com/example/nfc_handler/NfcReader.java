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

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcV;

import java.io.IOException;
import java.security.Permission;

public class NfcReader extends StringHandler {
    //读取 NFC V 类卡片--EMPTY
    //ISO 15693, NFC Forum Type 5 tag
    public static String readNfcV(Tag tag) {
        NfcV mNfcV = NfcV.get(tag);
        try {
            //循环读取时如发生意外上次未断开，则关闭连接
            if (mNfcV.isConnected()) {
                mNfcV.close();
            }
            mNfcV.connect();
            byte[] infoRmation = NfcVUtil.getInfoRation(mNfcV);
            int blockNumber = infoRmation[12] + 1;
            int oneBlockSize = infoRmation[13] + 1;
            String AFI = StringHandler.readId(new byte[]{infoRmation[11]});
            String DSFID = StringHandler.readId(new byte[]{infoRmation[10]});
            //String readData = NfcVUtil.readBlocks(mNfcV, 0, blockNumber);
            String readData = NfcVUtil.readOneBlock(mNfcV, 0);
            //System.out.println(readData);
            //System.out.println(StringHandler.hexToUtf8(readData));
            mNfcV.close();
            return readData;
        }catch (Exception e) {
            //System.out.println("Read error");
            System.out.println(e.getMessage());
            if (mNfcV.isConnected()) {
                try {
                    mNfcV.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        //System.out.println(mNfcV.isConnected());
        return null;
    }
}
