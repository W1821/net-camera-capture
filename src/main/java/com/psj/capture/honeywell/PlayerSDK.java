/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * HCNetSDK.java
 *
 * Created on 2009-9-14, 19:31:34
 */

/**
 * @author Xubinfeng
 */

package com.psj.capture.honeywell;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.nio.ByteBuffer;

public interface PlayerSDK extends StdCallLibrary {

    PlayerSDK INSTANCE = (PlayerSDK) Native.loadLibrary("DllPlayer", PlayerSDK.class);

    class FRAME_INFO extends Structure {
        public NativeLong nWidth;//视频宽
        public NativeLong nHeight;//视频高
        public NativeLong nStamp;//时间截
        public NativeLong nType;//是否为关键帧
        public NativeLong nFrameRate;//帧率，此值预流备用
        public NativeLong bIsVideo;//是否为视频（0表示音频数据）
        public int[] nLinseSize = new int[4];//当为视频时，此值用于保存格式
    }

    class VIDEO_PARAM extends Structure {
        public char[] codec = new char[256];//H264或是MPEG4，等，表示当前视频编码类型
        public int width;      //视频宽
        public int height;      //视频高
        public int colorbits;  //颜色位数，此处无效，仅用于内部
        public int framerate;  //参考帧率
        public int bitrate;    //参考码率
        public char[] vol_data = new char[256];//扩展参数,解码时必要的数据
        public int vol_length;    //扩展参数长度
    }


    interface OnMediaDataRecvCallback extends StdCallCallback {
        void invoke(NativeLong nPort, ByteByReference pBuf, NativeLong nSize, FRAME_INFO pFrameInfo, Pointer pUser, NativeLong nReserved2);
    }

    interface StatusEventCallBack extends StdCallCallback {
        void invoke(NativeLong nPort, NativeLong nStateCode, String pResponse, Pointer pUser);
    }

    // 设置事件回调
    NativeLong IP_TPS_SetStatusEventCallBack(int nPort, StatusEventCallBack OnPlayerEventRecv, Pointer pUser);

    // 打开流 - nPort由用户自定义一个值
    int IP_TPS_OpenStream(NativeLong nPort, ByteBuffer pParam, int pSize, int isAudioParam, int nBufPoolSize);

    // 关闭流
    int IP_TPS_CloseStream(NativeLong nPort);

    // 设置解码数据回调
    int IP_TPS_SetDecCallBack(NativeLong nPort, OnMediaDataRecvCallback OnMediaDataRecv, Pointer pUser);

    // 开始播放(或解码) 如果在外部播放，则要先调用IP_TPS_SetDecCallBack进行设置回调函数，然后hWnd必须为NULL（即0）
    int IP_TPS_Play(NativeLong nPort, W32API.HWND hWnd);

    // 停止播放
    int IP_TPS_Stop(NativeLong nPort);

    // 传入视频数据
    int IP_TPS_InputVideoData(NativeLong nPort, ByteByReference pBuf, int nSize, int isKey, int timestamp);

    // 传入音频数据
    int IP_TPS_InputAudioData(NativeLong nPort, ByteByReference pBuf, int nSize, int timestamp);

    int IP_TPS_CatchPicByFileName(NativeLong nPort, String sFileName, int isJpg);

    int IP_TPS_CatchPic(NativeLong nPort, String sFileName);

    // 关闭所有
    int IP_TPS_ReleaseAll();

}


