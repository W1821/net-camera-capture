package com.psj.capture.util;


import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

public class PathUtil {

    private static final String ROOT_PATH = "/";
    private static final String JAR = ".jar";
    private static final String UTF8 = "utf-8";


    public static String getRootPath() {
        URL url = PathUtil.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath = null;
        try {
            // 转化为utf-8编码
            filePath = URLDecoder.decode(url.getPath(), UTF8);

            // 可执行jar包运行的结果里包含".jar"
            if (isJarFile(filePath)) {
                // 截取路径中的jar包名
                filePath = filePath.substring(0, filePath.lastIndexOf(ROOT_PATH) + 1);
            }
            File file = new File(filePath);
            //得到windows下的正确路径
            filePath = file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private static boolean isJarFile(String filePath) {
        return filePath.endsWith(JAR);
    }


    public static String getFileName(File file) {
        if (file != null) {
            String fileName = file.getName();
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return null;
    }

    public static String listToString(String[] list, String separator) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append(separator);
        }
        return sb.substring(0, sb.lastIndexOf(separator));
    }


}
