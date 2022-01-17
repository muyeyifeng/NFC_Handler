package com.example.nfc_handler;

import android.nfc.NdefRecord;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StringHandler {
    public static String baseByteToHexString(byte[] src) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            if (src == null || src.length <= 0) {
                return null;
            }
            char[] buffer = new char[2];
            for (byte b : src) {
                buffer[0] = Character.forDigit((b >>> 4) & 0x0F, 16);
                buffer[1] = Character.forDigit(b & 0x0F, 16);
                //System.out.println(buffer);
                stringBuilder.append(buffer);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String byteToHexString(byte[] src) {
        try {
            String dataHexString = baseByteToHexString(src);
            StringBuilder newString = new StringBuilder();
            for (int i = 0; i < dataHexString.length() - 1; i += 2) {
                //System.out.println(dataHexString.substring(i, i + 2));
                newString.append(dataHexString.substring(i, i + 2)).append(':');
                if ((i / 2 + 1) % 4 == 0) {
                    newString.deleteCharAt(newString.length() - 1);
                    newString.append('\n');
                }
            }
            return newString.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String readId(byte[] src) {
        return baseByteToHexString(src);
    }

    public static String parseTextRecord(NdefRecord ndefRecord) {
        /**
         * 判断数据是否为NDEF格式
         */
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }
        //判断可变的长度的类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            //获得字节数组，然后进行分析
            byte[] payload = ndefRecord.getPayload();
            //下面开始NDEF文本数据第一个字节，状态字节
            //判断文本是基于UTF-8还是UTF-16的，取第一个字节"位与"上16进制的80，16进制的80也就是最高位是1，
            //其他位都是0，所以进行"位与"运算后就会保留最高位
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            //3f最高两位是0，第六位是1，所以进行"位与"运算后获得第六位
            int languageCodeLength = payload[0] & 0x3f;
            //下面开始NDEF文本数据第二个字节，语言编码
            //获得语言编码
            String languageCode = new String(payload, 1, languageCodeLength, StandardCharsets.US_ASCII);
            //下面开始NDEF文本数据后面的字节，解析出文本
            String textRecord = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public static String hexToUtf8(String hex){
        int beginIndex=22;
        String endSymbol="fe000000";
        int endIndex=hex.indexOf(endSymbol);
        hex=hex.substring(beginIndex,endIndex).replaceAll("(0)+$", "");
        byte[] byteArr = new byte[hex.length()/2];
        for(int i=0;i<hex.length()/2;i++){
            String output=hex.substring(i*2,i*2+2);
            Integer hexInt=Integer.decode("0x"+output);
            byteArr[i]=hexInt.byteValue();
        }
        String finalString="";
        try {
            finalString=new String(byteArr,"utf-8");
            //System.out.println(finalString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  finalString;
    }
}
