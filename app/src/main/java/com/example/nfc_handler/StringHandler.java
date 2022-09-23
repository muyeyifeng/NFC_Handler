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

import android.nfc.NdefRecord;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Objects;

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
            for (int i = 0; i < Objects.requireNonNull(dataHexString).length() - 1; i += 2) {
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
            return new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public static double[] hexToUtf8(String hex) {
        if(hex.length()!=1024)
            return null;
        int len=128;    //只需要读取前128字节
        double[] tempandadc= new double[2*len/8];

        hex=hex.substring(0,len);
            for (int i = 0; i < len; i += 8) {
                String hTemp = hex.substring(i, i + 4);
                String hADC = hex.substring(i + 4, i + 8);
                byte bTemph = (byte) Integer.parseInt(hTemp.substring(0, 2), 16);
                byte bTempl = (byte) Integer.parseInt(hTemp.substring(2, 4), 16);
                byte bADCh = (byte) Integer.parseInt(hADC.substring(0, 2), 16);
                byte bADCl = (byte) Integer.parseInt(hADC.substring(2, 4), 16);
                double dTemp = (int) ((bTemph << 8) | (0x00FF & bTempl)) / 256.0 + 40;
                double dADC = (int) ((bADCh << 8) | (0x00FF & bADCl)) * 3.3 / 4096;
                //System.out.println(dADC + dTemp);
                tempandadc[i/4]=dTemp;
                tempandadc[i/4+1]=dADC;
            }

        return tempandadc;
    }
}
