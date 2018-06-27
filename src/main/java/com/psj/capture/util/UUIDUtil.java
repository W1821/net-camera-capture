package com.psj.capture.util;

import java.util.UUID;

/**
 * @author saiya
 * @date 2018/6/27 0027
 */
public class UUIDUtil {

    /**
     * 生成uuid
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
