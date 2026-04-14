package com.hndl.ui;

import android.app.Application;
import android.provider.Settings;
import android.view.Surface;

import com.hndl.serialport.SerialPortUtils;
import com.hndl.ui.server.CanSerialManager;
import com.hndl.ui.utils.AdbUtils;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AppData extends Application {

    private static AppData instance;
    public static AppData getInstance() {
        if (instance == null) {
            synchronized (AppData.class) {
                if (instance == null) {
                    instance = new AppData();
                }
            }
        }
        return instance;
    }

    public static int versionNumber=67;

    public static int voiceState=0;  //静音开关 0：关静音 1：开静音

    public static boolean isFaultAlarm = false;   //显示故障报警信息

    public static boolean is31=true;  //接收低功耗判断

    public static boolean isShowAdmin=false;  //是否显示管理员入口

    //public static byte[] canData_Overload;  //超载数据解析
    public static boolean isOverload=false; //超载变量
    //public static OverloadRecordModel overloadRecordModel;
    public static int modelType=2;   //车型类型

    public static int workHours=0;      //工作时间 单位 s
    public static int reckonByTime=0;  //心跳包计时
    public static boolean isBusFailure=false;  //总线通讯故障

    public static int dataSrc=0;   //数据来源 1:IO  2：40点控制器

    public static List<String> mAlarmingList = new ArrayList<>(); //报警提示列表
    public static boolean isOneOverload=true;  //超载持续时间记录

    public static int ahdChannel=0;  //AHD渠道
    public final static int nodeID=0x08;
    public static int baudRate=115200; //单片机波特率
    public static int sectionArm=0;   //0 --- 五节臂; 1 --- 6 节臂
    public static int CounterWeight=0;  //配重 1.2T 和 4.0T对应的索引值分别是0和1
    public static AtomicInteger indexVirtual=new AtomicInteger(-1);
    public static AtomicBoolean isSendVirtual=new AtomicBoolean(false);
    public static float amplitudeValue=0;  //当前幅度.
    public static float heightValue=0;  //当前高度
    public static boolean isVirtualAlarm=false;
    public static byte[] sendVirtualData = {0x01, 0x02, 0x03, 0x04};

    public static CanSerialManager canSerialManager = new CanSerialManager();
    public static SerialPortUtils serialPort = new SerialPortUtils();

    public static boolean isCanData=false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //MMKV存储初始化
        MMKV.initialize(this);
        AdbUtils.setHome();
        Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);

        canSerialManager.setContext(this);
        canSerialManager.start();
    }
}
