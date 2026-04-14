package com.hndl.ui.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AdbUtils {

    /**
     * 喇叭开关 0：关 1：开
     * @param type
     */
    public static void hornSwitch(int type){
        List commnandList = new ArrayList();
        commnandList.add("cd /sys/devices/virtual/pwrcfg_class/dl_amp_mute");
        if (type==0){
            commnandList.add("echo 0 > state");
        }else {
            commnandList.add("echo 1 > state");
        }
        CommandResult result = ShellUtils.execCommand(commnandList, false);
        //LogUtil.e("XJW","result:"+result.result+"errorMsg:"+result.errorMsg+"successMsg:"+result.successMsg);
    }

    /**
     * th1520声卡芯片赋权
     */
    public static void asoundSwitch(){
        String catStr="cat /proc/asound/cards";
        CommandResult result = ShellUtils.execCommand(catStr, false);
        String successMsg=result.successMsg+"";
        if (!(successMsg.equals("")||successMsg.equals("null"))){
            if(successMsg.indexOf("USB-Audio") != -1) {
                catStr="ls /dev/snd/";
                result = ShellUtils.execCommand(catStr, false);
                //LogUtil.e("XJW","result:"+result.result+"errorMsg:"+result.errorMsg+"successMsg:"+result.successMsg);
                catStr="chmod 666 /dev/snd/pcmC0D0c";
                result = ShellUtils.execCommand(catStr, false);
            }
        }
    }

    /**
     * 获取CPU温度
     */
    public static String cpuTemperature(){
        String catStr="cat /sys/class/thermal/thermal_zone0/temp";
        CommandResult result = ShellUtils.execCommand(catStr, false);
        String cpuStr=result.successMsg;
        return cpuStr;
    }

    /**
     * 蜂鸣器开关 0：关 1：开
     * @param type
     */
    public static void buzzerSwitch(int type){
        List commnandList = new ArrayList();
        commnandList.add("cd /sys/devices/virtual/pwrcfg_class/dl_do_beep");
        if (type==0){
            commnandList.add("echo 0 > state");
        }else {
            commnandList.add("echo 1 > state");
        }
        CommandResult result = ShellUtils.execCommand(commnandList, false);
        //LogUtil.e("XJW","result:"+result.result+"errorMsg:"+result.errorMsg+"successMsg:"+result.successMsg);
    }

    /**
     * 关闭虚拟导航栏 0：打开 1：关闭  设置完需重启生效 废弃
     * @param type
     */
    public static void switchNavigationBar(int type){
        List commnandList = new ArrayList();
        if (type==0){
            commnandList.add("setprop qemu.hw.mainkeys 0");
        }else {
            commnandList.add("setprop qemu.hw.mainkeys 1");
        }
        CommandResult result = ShellUtils.execCommand(commnandList, false);
    }

    public static void setHome(){
        List commnandList = new ArrayList();
        commnandList.add("pm disable-user --user 0 com.android.launcher3");
        CommandResult result = ShellUtils.execCommand(commnandList, false);
    }

    public static boolean isDeviceRooted() {
        String[] paths = {"/system/bin/su", "/system/xbin/su" };
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    public static void executeRootCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream outputStream = process.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
            outputStream.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 低功耗
     * @param type
     */
    public static void lowPower(int type){
        List commnandList = new ArrayList();
        if (type==0){
            commnandList.add("cd /sys/devices/virtual/pwrcfg_class/dl_amp_mute");
            commnandList.add("echo 0 > state");
            commnandList.add("cd ../");
            commnandList.add("cd dl_pwrcfg_eth");
            commnandList.add("echo 0 > state");
            commnandList.add("cd ../");
            commnandList.add("cd dl_pwrcfg_fm");
            commnandList.add("echo 0 > state");
            commnandList.add("cd ../");
            commnandList.add("cd dl_pwrcfg_th1520");
            commnandList.add("echo 0 > state");
            commnandList.add("cd ../");
            commnandList.add("cd dl_pwrcfg_5v");
            commnandList.add("echo 0 > state");
        }else {
            commnandList.add("cd /sys/devices/virtual/pwrcfg_class/dl_pwrcfg_5v");
            commnandList.add("echo 1 > state");
            commnandList.add("cd ../");
            commnandList.add("cd dl_amp_mute");
            commnandList.add("echo 1 > state");
            commnandList.add("cd ../");
            commnandList.add("cd dl_pwrcfg_eth");
            commnandList.add("echo 1 > state");
            commnandList.add("cd ../");
            commnandList.add("cd dl_pwrcfg_fm");
            commnandList.add("echo 1 > state");
            commnandList.add("cd ../");
            commnandList.add("cd dl_pwrcfg_th1520");
            commnandList.add("echo 1 > state");
        }
        CommandResult result = ShellUtils.execCommand(commnandList, false);
        //LogUtil.e("XJW","result:"+result.result+"errorMsg:"+result.errorMsg+"successMsg:"+result.successMsg);
    }

    /**
     * 禁止home、返回等虚拟键 0：禁止 1：允许
     * @param type
     */
    public static void switchVirtualKey(int type){
        List commnandList = new ArrayList();
        if (type==0){
            commnandList.add("settings put secure user_setup_complete 0");
        }else {
            commnandList.add("settings put secure user_setup_complete 1");
        }
        CommandResult result = ShellUtils.execCommand(commnandList, false);
    }

    public static void adbDebugging(){
        List commnandList = new ArrayList();
        commnandList.add("setprop service.adb.tcp.port  5555");
        commnandList.add("stop adbd");
        commnandList.add("start adbd");
        CommandResult result = ShellUtils.execCommand(commnandList, true);
    }

    public static void adblock(){
        List commnandList = new ArrayList();
        commnandList.add("hwclock --hctosys --utc");
        CommandResult result = ShellUtils.execCommand(commnandList, true);
        //LogUtil.e("XJW","result:"+result.result+"errorMsg:"+result.errorMsg+"successMsg:"+result.successMsg);
    }
}
