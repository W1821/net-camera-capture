package com.psj.capture.util;

import java.io.*;

/**
 * @author Administrator
 */
public class BMPUtil {

    public static InputStream getRgb24BmpInputStream(byte[] rgb24, int width, int height) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        saveBmpTop(os, width, height);
        saveBmpInfo(os, width, height);
        os.write(rgb24);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        os.close();
        return is;
    }

    /**
     * rgb24数组保存为bmp文件
     */
    public static void saveBmp(String path, byte[] rgb24, int width, int height) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            saveBmpTop(fos, width, height);
            saveBmpInfo(fos, width, height);
            fos.write(rgb24);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存BMP图片头部信息的方法
     */
    private static void saveBmpTop(OutputStream ops, int width, int height) throws IOException {
        ops.write('B');
        ops.write('M');
        // 位图文件的大小
        int size = 14 + 40 + height * width * 3 + (4 - width * 3 % 4) * 256;
        writeInt(ops, size);
        writeShort(ops, (short) 0);
        writeShort(ops, (short) 0);
        writeInt(ops, 54);
    }

    /**
     * 保存BMP图片位图信息头部分的方法
     */
    private static void saveBmpInfo(OutputStream ops, int width, int height) throws IOException {
        writeInt(ops, 40);
        writeInt(ops, width);
        writeInt(ops, height);
        writeShort(ops, (short) 1);
        writeShort(ops, (short) 24);
        writeInt(ops, 0);
        writeInt(ops, height * width * 3 + (4 - width * 3 % 4) * height);
        writeInt(ops, 0);
        writeInt(ops, 0);
        writeInt(ops, 0);
        writeInt(ops, 0);
    }

    /**
     * 由于写入的是字节，所以要将整型进行转换
     */
    private static void writeInt(OutputStream ops, int t) throws IOException {
        int a = (t >> 24) & 0xff;
        int b = (t >> 16) & 0xff;
        int c = (t >> 8) & 0xff;
        int d = t & 0xff;
        ops.write(d);
        ops.write(c);
        ops.write(b);
        ops.write(a);
    }


    /**
     * 由于写入的是字节，所以要将短整型进行转换
     *
     * @param ops 输出流
     * @param t   短整形值
     */
    private static void writeShort(OutputStream ops, short t) throws IOException {
        int c = (t >> 8) & 0xff;
        int d = t & 0xff;
        ops.write(d);
        ops.write(c);
    }


}