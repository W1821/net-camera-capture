package com.psj.capture.honeywell;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface IPNet264SDK extends StdCallLibrary {

    IPNet264SDK INSTANCE = (IPNet264SDK) Native.loadLibrary("NetSDKDLL", IPNet264SDK.class);

    int EVENT_CONNECTING = 0;               // 连接中
    int EVENT_CONNECTOK = 1;                // 连接成功,表示连接辅助通过成功，但还没有认证
    int EVENT_CONNECTFAILED = 2;           // 连接失败
    int EVENT_SOCKETERROR = 3;             // socket失败
    int EVENT_LOGINOK = 4;                  // 登录成功,只有到这一步才表示认证成功了
    int EVENT_LOGINFAILED = 5;             // 登录失败
    int EVENT_LOGIN_USERERROR = 31;        // 登录用户或密码错误
    int EVENT_LOGOUT_FINISH = 32;          // 登录线程已退出主循环
    int EVENT_LOGIN_RECONNECT = 33;        // 进行重新登录
    int EVENT_LOGIN_HEARTBEAT_LOST = 34;  // 心跳丢失


    int GOTO_PRESET = 39; // 回到预置点

    class LPFRAME_EXTDATA extends Structure {
        public int bIsKey;//是否为关键帧
        public int timestamp;//时间截
        public Pointer pUserData;//用户数据指针，本为函数中pUser所设指针
    }

    class LPUSRE_VIDEOINFO extends Structure {
        public int nVideoPort;            //连接的视频端口
        public int bIsTcp;                //是否使用TCP连接
        /**
         * 视频通道，为32位整数，高16表示类型，可以是0，1，0表示IPC，1表示DVS，低16位表示
         * 视频通道号,当类型为0时，可以是0，1，0表示主码流，1表示子码流
         * 当类型为1时，可以是0，1，2，3，4，分别表示通道1，2，3，4，和CIF输出。
         */
        public int nVideoChannle;
        public Pointer pUserData;//用户指针，用于回调时作为最后一参数
    }

    // 状态告警事件回调函数
    interface StatusEventCallBack extends StdCallCallback {
        void invoke(NativeLong lLoginID, NativeLong nStateCode, String pResponse, Pointer pUser);
    }

    // 辅助通道消息回调函数
    interface AUXResponseCallBack extends StdCallCallback {
        void invoke(NativeLong lUser, NativeLong nType, String pResponse, Pointer pUser);
    }

    interface RealDataCallBack extends StdCallCallback {
        void invoke(NativeLong lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, LPFRAME_EXTDATA pExtData);
    }

    // 初始化 SDK, 在所有的 SDK 函数之前调用
    NativeLong IP_NET_DVR_Init();

    // 读取厂商号，在调用所有函数之前要调用一次此函数
    NativeLong IP_NET_DVR_LoadVendorId();

    // 释放资源
    NativeLong IP_NET_DVR_Cleanup();

    // 获取NETSDK的版本号
    long IP_NET_DVR_GetSDKVersion();

    // 设备注册
    NativeLong IP_NET_DVR_Login(String ipAddr, int port, String userName, String password, String bRelay);

    // 退出
    NativeLong IP_NET_DVR_Logout(NativeLong userID);

    NativeLong IP_NET_DVR_Reconnect(NativeLong lUserID);

    // 设置状态告警事件回调函数
    NativeLong IP_NET_DVR_SetStatusEventCallBack(StatusEventCallBack fStatusEventCallBack, Pointer pUser);

    // 设置辅助通道消息回调函数
    NativeLong IP_NET_DVR_SetAUXResponseCallBack(AUXResponseCallBack fAUXCallBack, Pointer pUser);

    // 云台配置
    NativeLong IP_NET_DVR_GetDVRConfig(NativeLong lUser, int cmd, int i, String j, int k, int l);

    // 云台控制基本
    NativeLong IP_NET_DVR_PTZControl(NativeLong lUser, int dwPTZCommand, int nTspeed, int nPpeed);

    // 云台控制扩展
    NativeLong IP_NET_DVR_PTZControlEx(NativeLong lUser, String pXml);

    // 预置点控制
    NativeLong IP_NET_DVR_PTZPreset(NativeLong lUser, int dwPTZPresetCmd, int dwPresetIndex);

    // 发送命令
    NativeLong IP_NET_DVR_SystemControl(NativeLong lUser, int cmd, int i, String Createiframxml);

    NativeLong IP_NET_DVR_SetDVRConfig(NativeLong lUser, int cmd, int i, String pXml, int length);

    // 连接媒体收流
    NativeLong IP_NET_DVR_RealPlay(NativeLong lUserID, Pointer lpClientInfo, RealDataCallBack cbRealDataCallBack, LPUSRE_VIDEOINFO pUser, boolean bBlocked);

    NativeLong IP_NET_DVR_StopRealPlay(NativeLong lRealHandle);

    NativeLong IP_NET_DVR_RealPlayEx(NativeLong lUserID, String serverip, String user, String pass, RealDataCallBack cbRealDataCallBack, LPUSRE_VIDEOINFO pUser, boolean bBlocked);

    // 获取视频参数
    NativeLong IP_NET_DVR_GetVideoParam(NativeLong lRealHandle, PlayerSDK.VIDEO_PARAM pParam);

}


