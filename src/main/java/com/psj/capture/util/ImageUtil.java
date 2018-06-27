package com.psj.capture.util;

/**
 * @author Administrator
 */
public class ImageUtil {

    /**
     * 清理掉yv12格式中的padding
     */
    public static byte[] yv12ClearPadding(byte[] yv12, int width, int height, int yStride, int vStride, int uStride) {
        // y分量的对齐值
        int yPadding = yStride - width;
        // v分量的对齐值
        int vPadding = vStride - width / 2;
        // u分量的对齐值
        int uPadding = uStride - width / 2;

        int size = width * height;
        byte[] yv12New = new byte[size * 3 / 2];
        for (int i = 0, j = 0; i < yv12.length; i++) {
            // 行数
            int row = i / yStride + 1;
            // y分量
            if (row <= height && i >= yStride * row - yPadding && i < yStride * row) {
                continue;
            }
            // v分量
            int vi = i - height * yStride;
            row = vi / vStride + 1;
            if (vi >= vStride * row - vPadding && vi < vStride * row) {
                continue;
            }
            // u分量
            int ui = vi - height * vStride;
            row = ui / uStride + 1;
            if (ui >= uStride * row - uPadding && ui < uStride * row) {
                continue;
            }
            yv12New[j] = yv12[i];
            j++;
        }
        return yv12New;
    }


    /**
     * yv12转rgb24
     * YV12格式一个像素占1.5个字节
     */
    public static byte[] swapYV12ToRGB24(byte[] yv12, int width, int height) {
        if (yv12 == null) {
            return null;
        }
        int nYLen = width * height;
        int halfWidth = width >> 1;
        if (nYLen < 1 || halfWidth < 1) {
            return null;
        }
        byte[] rgb24 = new byte[width * height * 3];
        int[] rgb = new int[3];
        int i, j, m, n, x, y;
        m = -width;
        n = -halfWidth;
        for (y = 0; y < height; y++) {
            m += width;
            if (y % 2 != 0) {
                n += halfWidth;
            }
            for (x = 0; x < width; x++) {
                i = m + x;
                j = n + (x >> 1);
                // r
                rgb[2] = (int) ((yv12[i] & 0xFF) + 1.370705 * ((yv12[nYLen + j] & 0xFF) - 128));
                // g
                rgb[1] = (int) ((yv12[i] & 0xFF) - 0.698001 * ((yv12[nYLen + (nYLen >> 2) + j] & 0xFF) - 128) - 0.703125 * ((yv12[nYLen + j] & 0xFF) - 128));
                // b
                rgb[0] = (int) ((yv12[i] & 0xFF) + 1.732446 * ((yv12[nYLen + (nYLen >> 2) + j] & 0xFF) - 128));

                // 图像是上下颠倒的
                j = nYLen - width - m + x;
                i = (j << 1) + j;

//                j = m + x;
//                i = (j << 1) + j;

                for (j = 0; j < 3; j++) {
                    if (rgb[j] >= 0 && rgb[j] <= 255) {
                        rgb24[i + j] = (byte) rgb[j];
                    } else {
                        rgb24[i + j] = (byte) ((rgb[j] < 0) ? 0 : 255);
                    }
                }
            }
        }
        return rgb24;
    }


    /**
     * NV21转nv12
     */
    public static void swapNV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) {
            return;
        }
        int frameSize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, frameSize);
        for (j = 0; j < frameSize / 2; j += 2) {
            nv12[frameSize + j + 1] = nv21[j + frameSize];
        }
        for (j = 0; j < frameSize / 2; j += 2) {
            nv12[frameSize + j] = nv21[j + frameSize + 1];
        }
    }


    /**
     * YV12转I420
     */
    public static void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height) {
        System.arraycopy(yv12bytes, 0, i420bytes, 0, width * height);
        System.arraycopy(yv12bytes, width * height + width * height / 4, i420bytes, width * height, width * height / 4);
        System.arraycopy(yv12bytes, width * height, i420bytes, width * height + width * height / 4, width * height / 4);
    }

    /**
     * yv12转nv12
     */
    public static void swapYV12toNV12(byte[] yv12bytes, byte[] nv12bytes, int width, int height) {
        int nLenY = width * height;
        int nLenU = nLenY / 4;
        System.arraycopy(yv12bytes, 0, nv12bytes, 0, width * height);
        for (int i = 0; i < nLenU; i++) {
            nv12bytes[nLenY + 2 * i + 1] = yv12bytes[nLenY + i];
            nv12bytes[nLenY + 2 * i] = yv12bytes[nLenY + nLenU + i];
        }
    }

    /**
     * nv12转I420
     */
    public static void swapNV12toI420(byte[] nv12bytes, byte[] i420bytes, int width, int height) {
        int nLenY = width * height;
        int nLenU = nLenY / 4;
        System.arraycopy(nv12bytes, 0, i420bytes, 0, width * height);
        for (int i = 0; i < nLenU; i++) {
            i420bytes[nLenY + i] = nv12bytes[nLenY + 2 * i + 1];
            i420bytes[nLenY + nLenU + i] = nv12bytes[nLenY + 2 * i];
        }
    }

}
