package com.psj.capture.honeywell;

import com.psj.capture.core.CaptureParam;
import com.psj.capture.util.BMPUtil;
import com.psj.capture.util.ImageUtil;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * @author Administrator
 */
@Slf4j
public class HoneywellSDK {

    private HoneywellParam honeywellParam;      // 登录参数
    private CaptureParam captureParam;          // 抓图参数
    /**
     * 解码次数
     */
    private int decodeTimes = 0;

    private static final int VIDEO_PARAM_SIZE = 536;       // 视频参数大小
    private static final int VIDEO_PARAM_START_INDEX = 36;  // 视频参数开始下标

    private static IPNet264SDK ipNet264SDK = IPNet264SDK.INSTANCE;
    private static PlayerSDK playerSDK = PlayerSDK.INSTANCE;

    private NativeLong userID;                               // 设备句柄
    private NativeLong nPort;                                // 通道号
    private NativeLong lRealHandle;                         // 播放句柄

    private int loginStatus = 0;                            // 登录状态

    private StatusEventCallBack statusEventCallBack = new StatusEventCallBack();                // 状态告警事件回调函数
    private AUXResponseCallBack auxResponseCallBack = new AUXResponseCallBack();                // 辅助通道消息回调函数
    private RealDataCallBack realDataCallBack = new RealDataCallBack();                         // 实时播放回调

    private IPNet264SDK.LPUSRE_VIDEOINFO lpusreVideoinfo = new IPNet264SDK.LPUSRE_VIDEOINFO();  // 连接视频流相关参数
    private OnMediaDataRecvCallback onMediaDataRecvCallback = new OnMediaDataRecvCallback();   // 解码回调

    // 视频图片队列
    private Queue<ImageFrame> imageQueue = new ConcurrentLinkedQueue<>();

    /**
     * 构造
     */
    HoneywellSDK(HoneywellParam honeywellParam, CaptureParam captureParam) {
        this.honeywellParam = honeywellParam;
        this.captureParam = captureParam;

        nPort = new NativeLong(new Random().nextInt());
        initVideoInfo(honeywellParam.getVideoPort());
    }

    /**
     * 初始化连接视频流参数
     */
    private void initVideoInfo(int videoPort) {
        lpusreVideoinfo.nVideoPort = videoPort;
        lpusreVideoinfo.bIsTcp = 1;
        lpusreVideoinfo.nVideoChannle = 0;
        lpusreVideoinfo.pUserData = null;
    }

    /**
     * 初始化
     */
    public boolean init() {
        log.info("初始化开始");
        NativeLong initFlag = ipNet264SDK.IP_NET_DVR_Init();
        if (initFlag.intValue() != 0) {
            log.error("---初始化失败---");
            return false;
        }

        // 读取厂商号，在调用所有函数之前要调用一次此函数
        NativeLong vendorId = ipNet264SDK.IP_NET_DVR_LoadVendorId();
        log.info("厂商号" + vendorId);
        // 设置状态回调函数
        ipNet264SDK.IP_NET_DVR_SetStatusEventCallBack(statusEventCallBack, null);
        // 设置辅助通道消息回调函数
        ipNet264SDK.IP_NET_DVR_SetAUXResponseCallBack(auxResponseCallBack, null);
        log.info("初始化成功");
        return true;
    }

    /**
     * 登录播放
     */
    public boolean loginAndPlay() {
        // 登录
        if (!login()) {
            return false;
        }
        // 打开媒体流播放
        return play();
    }

    /**
     * 打开视频流播放
     */
    private boolean play() {
        log.info("播放开始");
        lRealHandle = ipNet264SDK.IP_NET_DVR_RealPlay(userID, null, realDataCallBack, lpusreVideoinfo, false);
        if (lRealHandle.intValue() == 0) {
            log.error("---播放失败---");
            // 退出登录
            ipNet264SDK.IP_NET_DVR_Logout(userID);
            return false;
        }
        log.info("播放成功");
        return true;
    }

    /**
     * 回到预置点
     */
    public boolean gotoPreset() {
        log.info("回到预置点成功");
        NativeLong result = ipNet264SDK.IP_NET_DVR_PTZPreset(userID, IPNet264SDK.GOTO_PRESET, captureParam.getPresetIndex());
        if (result.intValue() != 0) {
            log.error("回到预置点失败，预置点编号：" + captureParam.getPresetIndex());
            return false;
        }
        sleep(3000L); //等待球机转回预置点
        log.info("回到预置点成功");
        return true;
    }

    /**
     * 抓图
     *
     * @param timeout 超时时间
     */
    public void snap(long timeout) {
        log.info("抓图开始");
        long startTime = System.currentTimeMillis();
        long endTime;
        while (true) {
            endTime = System.currentTimeMillis();
            if (endTime - startTime > timeout) {
                log.error("---抓图超时---");
                return;
            }
            ImageFrame frame = imageQueue.poll();
            if (frame != null) {
                if (savePicture(frame)) {
                    log.info("抓图成功");
                } else {
                    log.error("---抓图失败---");
                }
                return;
            }
        }
    }

    /**
     * 结束
     */
    public void stop() {
        // 退出
        playSdkStop();
        ipSdkStop();
    }

    /**
     * 登录
     */
    private boolean login() {
        log.info("登录开始");
        userID = ipNet264SDK.IP_NET_DVR_Login(honeywellParam.getIp(), honeywellParam.getPort(), honeywellParam.getUserName(), honeywellParam.getPassword(), null);
        if (userID.intValue() == 0) {
            log.error("---登录失败---");
            return false;
        }
        sleep(800L);// 等待登录完成
        if (loginStatus != IPNet264SDK.EVENT_LOGINOK) {
            sleep(1000L);// 等待登录完成
            if (loginStatus != IPNet264SDK.EVENT_LOGINOK) {
                sleep(2200L);// 等待登录完成
                if (loginStatus != IPNet264SDK.EVENT_LOGINOK) {
                    log.error("---登录失败---");
                    return false;
                }
            }
        }
        log.info("登录成功");
        return true;
    }


    /**
     * 退出播放sdk
     */
    private void playSdkStop() {
        playerSDK.IP_TPS_CloseStream(nPort);
        playerSDK.IP_TPS_Stop(nPort);
        playerSDK.IP_TPS_ReleaseAll();
    }

    /**
     * 退出网络sdk
     */
    private void ipSdkStop() {
        // 停止流
        ipNet264SDK.IP_NET_DVR_StopRealPlay(lRealHandle);
        // 退出登录
        ipNet264SDK.IP_NET_DVR_Logout(userID);
        // 清理
        ipNet264SDK.IP_NET_DVR_Cleanup();
    }

    /**
     * 状态告警事件回调函数
     */
    private class StatusEventCallBack implements IPNet264SDK.StatusEventCallBack {
        @Override
        public void invoke(NativeLong lLoginID, NativeLong nStateCode, String pResponse, Pointer pUser) {
            switch (nStateCode.intValue()) {
                case IPNet264SDK.EVENT_CONNECTING:
                    log.info("连接中！");
                    break;
                case IPNet264SDK.EVENT_CONNECTOK:
                    log.info("连接成功,表示连接辅助通过成功，但还没有认证登录！");
                    break;
                case IPNet264SDK.EVENT_CONNECTFAILED:
                    log.error("连接失败！");
                    break;
                case IPNet264SDK.EVENT_SOCKETERROR:
                    log.error("socket失败！");
                    break;
                case IPNet264SDK.EVENT_LOGINOK:
                    log.info("登录成功！");
                    loginStatus = IPNet264SDK.EVENT_LOGINOK;
                    break;
                case IPNet264SDK.EVENT_LOGINFAILED:
                    log.error("登录失败！");
                    break;
                case IPNet264SDK.EVENT_LOGIN_USERERROR:
                    log.error("登录用户或密码错误！");
                    break;
                default:
            }
        }
    }

    /**
     * 辅助通道消息回调函数
     */
    private class AUXResponseCallBack implements IPNet264SDK.AUXResponseCallBack {
        @Override
        public void invoke(NativeLong lUser, NativeLong nType, String pResponse, Pointer pUser) {
            log.info("辅助通道消息回调函数---nType:" + nType + "---pResponse:" + pResponse + "---pUser:" + pUser);
        }
    }

    /**
     * 实时播放回调
     */
    private class RealDataCallBack implements IPNet264SDK.RealDataCallBack {
        @Override
        public void invoke(NativeLong lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, IPNet264SDK.LPFRAME_EXTDATA pExtData) {
            // dwDataType=0表示视频，1表示音频,2为解码参数
            switch (dwDataType) {
                case 0:
                    // 输入视频数据
                    playerSDK.IP_TPS_InputVideoData(nPort, pBuffer, dwBufSize, pExtData.bIsKey, pExtData.timestamp);
                    break;
                case 2:
                    // 结构体中的VIDEO_PARAM
                    byte[] p1 = new byte[VIDEO_PARAM_SIZE];
                    pBuffer.getPointer().read(VIDEO_PARAM_START_INDEX, p1, 0, VIDEO_PARAM_SIZE);
                    ByteBuffer byteBuffer = ByteBuffer.wrap(p1);
                    // 打开流
                    int openStreamResult = playerSDK.IP_TPS_OpenStream(nPort, byteBuffer, VIDEO_PARAM_SIZE, 0, 40);
                    log.info("打开流结果：" + openStreamResult);
                    // 设置解码回调函数
                    int setDecCallBackResult = playerSDK.IP_TPS_SetDecCallBack(nPort, onMediaDataRecvCallback, null);
                    log.info("设置解码回调函数：" + setDecCallBackResult);
                    // 开始解码
                    int playResult = playerSDK.IP_TPS_Play(nPort, null);
                    log.info("开始解码结果：" + playResult);
                    break;
                default:
            }
        }
    }

    /**
     * 解码回调
     */
    class OnMediaDataRecvCallback implements PlayerSDK.OnMediaDataRecvCallback {

        @Override
        public void invoke(NativeLong nPort, ByteByReference pBuf, NativeLong nSize, PlayerSDK.FRAME_INFO pFrameInfo, Pointer pUser, NativeLong nReserved2) {

            if (decodeTimes != 0) {
                return;
            }
            decodeTimes++;

            int width = pFrameInfo.nWidth.intValue();
            int height = pFrameInfo.nHeight.intValue();
            int yStride = pFrameInfo.nLinseSize[0];
            int vStride = pFrameInfo.nLinseSize[1];
            int uStride = pFrameInfo.nLinseSize[2];

            // 对于视频数据，前面有16个字节为YUV行扫描长度,所以要减去16
            int length = 16;
            int imageSize = nSize.intValue() - length;
            byte[] yv12 = new byte[imageSize];
            // 获取内存中的图片数据
            pBuf.getPointer().read(length, yv12, 0, imageSize);

            ImageFrame frame = new ImageFrame(yv12, width, height, yStride, vStride, uStride);
            imageQueue.clear();
            imageQueue.offer(frame);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存图片
     */
    private boolean savePicture(ImageFrame frame) {

        boolean success = true;

        // 清理对齐
        byte[] yb12 = yv12ClearPadding(frame);
        frame.setYv12(null);

        // 图片格式转换YUV转RGB
        byte[] rgb24 = ImageUtil.swapYV12ToRGB24(yb12, frame.getWidth(), frame.getHeight());

        InputStream imageInputStream = null;
        FileOutputStream fos = null;
        try {
            imageInputStream = BMPUtil.getRgb24BmpInputStream(rgb24, frame.getWidth(), frame.getHeight());
            BufferedImage bufferedImage = ImageIO.read(imageInputStream);
            File file = new File(captureParam.getFilePath());
            fos = new FileOutputStream(file);
            ImageIO.write(bufferedImage, "jpg", fos);
        } catch (Exception e) {
            success = false;
            log.error("抓图失败", e);
        } finally {
            try {
                if (imageInputStream != null) {
                    imageInputStream.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * 清理掉yv12格式中的padding
     */
    private byte[] yv12ClearPadding(ImageFrame frame) {
        byte[] yv12 = frame.getYv12();
        int width = frame.getWidth();
        int height = frame.getHeight();
        int yStride = frame.getYStride();
        int vStride = frame.getVStride();
        int uStride = frame.getUStride();

        if (yStride <= width) {
            return frame.getYv12();
        }
        return ImageUtil.yv12ClearPadding(yv12, width, height, yStride, vStride, uStride);
    }

}
