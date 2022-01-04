package com.example.nfc_handler;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;

import java.io.IOException;

public class NfcReader extends StringHandler {

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
            if(mfc.isConnected()){
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

    public static String readIsoDep(Tag tag) {
        return null;
    }

    public static String readNfcA(Tag tag) {
        return null;
    }

    public static String readNfcB(Tag tag) {
        return null;
    }

    public static String readNfcF(Tag tag) {
        return null;
    }

    public static String readNfcV(Tag tag) {
        return null;
    }

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

    public static String readNdefFormatable(Tag tag) {
        return null;
    }
}
