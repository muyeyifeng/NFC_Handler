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

import android.nfc.tech.NfcV;

import java.io.IOException;

public class NfcVUtil {
    /**
     * 取得标签信息
     */
    public static byte[] getInfoRation(NfcV mNfcV) throws IOException {
        byte[] cmd = new byte[10];
        cmd[0] = (byte) 0x22; // flag
        cmd[1] = (byte) 0x2B; // command
        byte[] ID = mNfcV.getTag().getId();
        System.arraycopy(ID, 0, cmd, 2, ID.length); // UID
        return mNfcV.transceive(cmd);
    }


    /**
     * 读取一个位置在position的block
     *
     * @param position 要读取的block位置
     * @return 返回内容字符串
     * @throws IOException
     */
    public static String readOneBlock(NfcV mNfcV, int position) throws IOException {
        byte[] cmd = new byte[11];
        cmd[0] = (byte) 0x22;
        cmd[1] = (byte) 0x20;
        byte[] ID = mNfcV.getTag().getId();
        System.arraycopy(ID, 0, cmd, 2, ID.length); // UID
        cmd[10] = (byte) position;
        byte[] res = mNfcV.transceive(cmd);
        if (res[0] == 0x00) {
            byte[] block = new byte[res.length - 1];
            System.arraycopy(res, 1, block, 0, res.length - 1);
            return StringHandler.baseByteToHexString(block);
        }
        return null;
    }

    /**
     * 读取从begin开始end个block
     * begin + count 不能超过blockNumber
     *
     * @param begin block开始位置
     * @param count 读取block数量
     * @return 返回内容字符串
     * @throws IOException
     */
    public static String readBlocks(NfcV mNfcV, int begin, int count) throws IOException {
        /*
        if ((begin + count) > blockNumber) {
            count = blockNumber - begin;
        }
        */
        StringBuilder data = new StringBuilder();
        for (int i = begin; i < count + begin; i++) {
            data.append(readOneBlock(mNfcV, i));
        }
        return data.toString();
    }


    /**
     * 将数据写入到block,
     *
     * @param position 要写内容的block位置
     * @param data     要写的内容,必须长度为blockOneSize
     * @return false为写入失败，true为写入成功
     * @throws IOException
     */
    public static boolean writeBlock(NfcV mNfcV, int position, byte[] data) throws IOException {
        byte[] cmd = new byte[15];
        cmd[0] = (byte) 0x22;
        cmd[1] = (byte) 0x21;
        byte[] ID = mNfcV.getTag().getId();
        System.arraycopy(ID, 0, cmd, 2, ID.length); // UID
        //block
        cmd[10] = (byte) 0x02;
        //value
        System.arraycopy(data, 0, cmd, 11, data.length);
        byte[] rsp = mNfcV.transceive(cmd);
        return rsp[0] == 0x00;
    }
}
