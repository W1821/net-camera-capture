package com.psj.capture.honeywell;


import com.psj.capture.core.CaptureParam;
import com.psj.capture.util.UUIDUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Honeywell摄像头抓图主方法
 *
 * @author Administrator
 */
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class HoneywellMain {

    private HoneywellSDK sdk;

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        HoneywellParam honeywellParam = new HoneywellParam("192.168.1.123", 8091, "admin", "q123456", 554);
        CaptureParam snapParam = new CaptureParam(1, "c:\\snap\\" + UUIDUtil.getUUID() + ".jpg");
        HoneywellSDK sdk = new HoneywellSDK(honeywellParam, snapParam);

        HoneywellMain honeywellMain = new HoneywellMain(sdk);
        honeywellMain.capture();

        long endTime = System.currentTimeMillis();
        log.info("总共消耗时长：" + (endTime - startTime) + "ms");

    }

    /**
     * 抓图
     */
    private void capture() {
        // 1.初始化
        if (!sdk.init()) {
            log.error("初始化失败！");
            return;
        }
        // 2.登录播放
        if (!sdk.loginAndPlay()) {
            log.error("登录播放失败！");
            return;
        }

        // 3.回到预置点
        if (!sdk.gotoPreset()) {
            log.error("回到预置点失败！");
            return;
        }
        // 4.抓图
        long timeout = 10000L;
        sdk.snap(timeout);

        // 5.结束清理
        sdk.stop();
    }

}
