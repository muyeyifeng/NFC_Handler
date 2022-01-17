package com.example.nfc_handler;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcV;

import java.io.IOException;

public class NfcReader extends StringHandler {

    //读取 MifareUltraight 类卡片--copy
    public static String readMifareUltralight(Tag tag) {
        MifareUltralight light = MifareUltralight.get(tag);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            light.connect();
            System.out.println(light.getType());
            System.out.println(light.getMaxTransceiveLength());
            for (int i = 4, j = 4; i < 15; i++, j += 4) {
                byte[] buffer = light.readPages(j);
                stringBuilder.append("Page ").append(i).append(" :\n\t").append(byteToHexString(buffer)).append("\n");
            }
            return stringBuilder.toString();
            //return  new String(bytes, Charset.forName("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //读取 MifareClassic 类卡片--copy
    public static String readMifareClassic(Tag tag) {
        boolean auth;
        MifareClassic mfc = MifareClassic.get(tag);
        try {
            if (mfc.isConnected())
                mfc.close();
            mfc.connect();

            StringBuilder metaInfo = new StringBuilder();
            int type = mfc.getType();
            int sectorCount = mfc.getSectorCount();

            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo.append("卡片类型:").append(typeS).append("\n共").append(sectorCount).append("个扇区\n共").append(mfc.getBlockCount()).append("个块\n存储空间:").append(mfc.getSize()).append("B\n");
            for (int j = 0; j < sectorCount; j++) {
                auth = mfc.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo.append("Sector").append(j).append(":验证成功\n");

                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo.append("Block ").append(bIndex).append(":\n").append(byteToHexString(data)).append("\n\n");
                        bIndex++;
                    }
                } else {
                    metaInfo.append("Sector").append(j).append(":验证失败\n");
                }
            }
            mfc.close();
            return metaInfo.toString();
        } catch (Exception e) {
            if (mfc.isConnected()) {
                try {
                    mfc.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            System.out.println(mfc.isConnected());
            e.printStackTrace();
        }
        return null;
    }

    //读取 IsoDep 类卡片--copy
    public static String readIsoDep(Tag tag) {
        return null;
    }

    //读取 NFC A 类卡片--EMPTY
    public static String readNfcA(Tag tag) {
        return null;
    }

    //读取 NFC B 类卡片--EMPTY
    public static String readNfcB(Tag tag) {
        return null;
    }

    //读取 NFC F 类卡片--EMPTY
    public static String readNfcF(Tag tag) {
        return null;
    }

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
            byte[] infoRmation = NfcVUtil.getInfoRmation(mNfcV);
            int blockNumber = infoRmation[12] + 1;
            int oneBlockSize = infoRmation[13] + 1;
            String AFI = StringHandler.readId(new byte[]{infoRmation[11]});
            String DSFID = StringHandler.readId(new byte[]{infoRmation[10]});
            String readData = NfcVUtil.readBlocks(mNfcV, 0, blockNumber);
            //System.out.println(readData);
            //System.out.println(StringHandler.hexToUtf8(readData));
            mNfcV.close();
            return readData;
        } catch (Exception e) {
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

    ////读取 NDEF 类卡片--目前0位置文本
    public static String readNdef(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        try {
            if (ndef.isConnected())
                ndef.close();
            ndef.connect();
            NdefMessage temp = ndef.getNdefMessage();
            NdefRecord ndefRecord = temp.getRecords()[0];
            String mTagContent = parseTextRecord(ndefRecord);
            ndef.close();
            return mTagContent;
        } catch (Exception e) {
            if (ndef.isConnected()) {
                try {
                    ndef.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            System.out.println(ndef.isConnected());
            return null;
        }
    }

    //读取 NdefFormatable 类卡片--EMPTY
    public static String readNdefFormatable(Tag tag) {
        return null;
    }
}
