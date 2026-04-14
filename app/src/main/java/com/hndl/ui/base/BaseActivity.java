package com.hndl.ui.base;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.AppData.nodeID;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.activity.SplashActivity;
import com.hndl.ui.data.PersistentData;
import com.hndl.ui.model.OverloadRecordModel;
import com.hndl.ui.receiver.ScreenStatusReceiver;
import com.hndl.ui.server.OnHandleSerialProtocol;
import com.hndl.ui.utils.AdbUtils;
import com.hndl.ui.utils.DataUtils;
import com.hndl.ui.utils.DatabaseManager;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.widget.ParentBaseDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public abstract class BaseActivity extends AppCompatActivity {

    public static boolean isAhdOne = true;
    public static boolean isConfirm = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |View.SYSTEM_UI_FLAG_IMMERSIVE| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//实现状态栏文字颜色为暗色
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
        registSreenStatusReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        canSerialManager.setOnHandleSerialProtocol(new OnHandleSerialProtocol() {
            @Override
            public boolean handleSerialData(byte[] serialData) {
                return parseSerialData(serialData);
            }
        });
    }

    private boolean parseSerialData(byte[] serialData) {
        //LogUtils.e("XJW",ConvertUtils.bytes2HexString(serialData));

        byte[] canData = new byte[8];
        int serialLen = serialData.length;
        // stert、end byte check
        if ((serialData[0] != 0x02) || (serialData[serialLen - 3] != 0x03)) {
            return false;
        }

        // crc check
        //int calcCrcValue = calcCRC(serialData);
//      int crcValue = (serialData[serialLen-1]<<4 | serialData[serialLen-2]);

        byte serialDatum = serialData[1];          //Cmd命令
        if (serialDatum == 0x36) {
            if (AppData.isCanData) {
                byte[] canPkgData = ArrayUtils.subArray(serialData, 4, serialLen - 3);      //截取出Data数组段
                parseCanData(canPkgData);
            }
        } else if (serialDatum == 0x31) {
            boolean isLow = MMKVUtils.getInstance().decodeBoolean("isLow");
            if (AppData.is31 && isLow) {
                AppData.is31 = false;
                AdbUtils.lowPower(0);
                canSerialManager.sendBytes(MyUtils.constructSerialData((byte) 0x32, 0x000, new byte[]{0x0}));
                Intent intent = new Intent();
                intent.setClass(this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                overridePendingTransition(0, 0);
            }
        } else if (serialDatum == 0x30) {
            //LogUtil.e("XJW", "30:" + ConvertUtils.bytes2HexString(serialData));
        } else {
            return false;
        }
        return true;
    }

    private static byte[] canData_2D4s;
    private static byte[] canData_584s;
    private static byte[] canData_194s;
    private static byte[] canData_1A4s;
    private static byte[] canData_1B4s;
    private static byte[] canData_2A4s;
    private static byte[] canData_294s;
    private static byte[] canData_704s;

    private byte[] parseCanData(byte[] canPkgData) {
        // 如果canPkgData的数据长度不等于 26 个字节，则不符合can的8字节，会导致canData数据解析时错误。 add by chenyaoli,2023.12.28
        if (canPkgData.length != 26) {
            return null;
        }

        byte[] canData = new byte[8];

        int canIndex = MyUtils.hexChar2Int(canPkgData[0]);
        int canDataLen = MyUtils.hexChar2Int(canPkgData[1]);
        int canId = 0;
        for (int i = 0; i < 8; i++) {
            canId = canId + (MyUtils.hexChar2Int(canPkgData[i + 2]) << (i * 4));
        }
        int standCanFlag = canId >> 31 & 0x01;  // 0-标准帧、1-扩展帧
        canId = canId & 0x7FFFFFFF; // 去掉CanId 最高位
        int resultCode = MyUtils.hexChar2Int(canPkgData[canPkgData.length - 1]);
        for (int i = 0; i < 8; i++) {
            int startIndex = 10 + i * 2;
            canData[i] = (byte) (MyUtils.hexChar2Int(canPkgData[startIndex]) | (MyUtils.hexChar2Int(canPkgData[startIndex + 1]) << 4));
        }

        String log = "canIndex:" + canIndex + "  canDataLen:" + canDataLen + " canId:" + canId + " flag:" + standCanFlag + " resultCode:" + resultCode + " \n canData:" + ConvertUtils.bytes2HexString(canData);
//        if (canIndex!=1){
//            LogUtil.e("XJW","canId:"+canId);
//        }
        switch (canId) {
            case 0x580 + nodeID: {
                //LogUtils.e("XJW","canId:"+canId);
                getCanData_584(canData);
                if (!Arrays.equals(canData, canData_584s)) {
                    ThreadUtils.runOnUiThread(() -> {
                        canData_584s = canData;
                        if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x20 && canData[3] == 0x01) {
                            // codeFour = getInteger(canData[4]);
                            int workArm = canData[4] & 0x07;
                            int magnification = (canData[4] >> 3) & 0x1F;
                            int codeFive = getInteger(canData[5]);
                            int legState = codeFive & 0x01 + (((codeFive >> 1) & 0x01) << 1);  //支腿状态
                            int fifthLegState = (codeFive >> 2) & 0x01 + (((codeFive >> 3) & 0x01) << 1);  //第五支腿状态
                            int workArea = (codeFive >> 4) & 0x01;
                            PersistentData.work_arm = workArm;
                            PersistentData.magnification = magnification;
                            PersistentData.leg = legState;
                            PersistentData.fifth_leg = fifthLegState;
                            PersistentData.work_area = workArea;
                        } else if (canData[0] == 0x60 && canData[1] == 0x27 && canData[2] == 0x20 && canData[3] == AppData.sendVirtualData[AppData.indexVirtual.get()]) {
                            int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                            int codeSix = getInteger(canData[6]) + (getInteger(canData[7]) << 8);
                            float UpperLimitValue = 0;
                            float etUpperDecelerationZone = 0;
                            float LowerLimitingValue = 0;
                            float LowerDecelerationZone = 0;
                            if (AppData.indexVirtual.get() == 0) {
                                UpperLimitValue = (float) (codeFour * 0.1);
                                etUpperDecelerationZone = (float) (codeSix * 0.1);
                            } else if (AppData.indexVirtual.get() == 1) {
                                AppData.isSendVirtual.set(false);
                                LowerLimitingValue = (float) (codeFour * 0.1);
                                LowerDecelerationZone = (float) (codeSix * 0.1);
                            } else if (AppData.indexVirtual.get() == 2) {
                            } else if (AppData.indexVirtual.get() == 3) {
                            }
                            if (AppData.indexVirtual.get() < 1) {
                                AppData.indexVirtual.set(AppData.indexVirtual.get() + 1);
                            }
                            if (AppData.heightValue > UpperLimitValue || AppData.heightValue > etUpperDecelerationZone ||
                                    AppData.heightValue < LowerLimitingValue || AppData.heightValue < LowerDecelerationZone) {
                                AppData.isVirtualAlarm = true;
                            } else {
                                AppData.isVirtualAlarm = false;
                            }
                            if (AppData.isVirtualAlarm) {
                                if (AppData.getInstance().voiceState == 0) {
                                    AdbUtils.buzzerSwitch(1);
                                } else {
                                    AdbUtils.buzzerSwitch(0);
                                }
                            }
                        }
                    });
                }
                break;
            }
            case 0x1C0 + nodeID:
                getCanData_1C4(canData);
                break;
            case 0x1E0 + nodeID:
                // add 0x1E4, add by chenyaoli,2023.12.27
                getCanData_1E4(canData);
                break;
            case 0x290 + nodeID: {
//                if (!Arrays.equals(canData, canData_294s)) {
                ThreadUtils.runOnUiThread(() -> {
                    canData_294s = canData;
                    int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                    double moment_percentage = codeFour * 0.01;
                    if (canData_Alarm != null && moment_percentage > 100 && canData_Alarm[1] == 0x0 && canData_Alarm[2] == 0x0) {
                        if (!AppData.isOverload) {
                            AppData.isOverload = true;
                            int codeZero = getInteger(canData[0]) + (getInteger(canData[1]) << 8);
                            PersistentData.frontal_weight = MyUtils.getOneDecimal(codeZero * 0.01) + "";

                            int codeTwo = getInteger(canData[2]) + (getInteger(canData[3]) << 8);
                            PersistentData.actual_weight = MyUtils.getOneDecimal(codeTwo * 0.01) + "";

                            PersistentData.moment_percentage = MyUtils.getOneDecimal(moment_percentage) + "%";
                            PersistentData.start_date = TimeUtils.getNowString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")) + "";
                            AppData.isOneOverload = true;
                            PersistentData.overloadRecordModel = new OverloadRecordModel();
                            stopOverloadTask();
                            startOverloadTask();
                        }
                    } else {
                        PersistentData.overloadRecordModel = null;
                        AppData.isOverload = false;
                        stopOverloadTask();
                    }
                });
                //}
                getCanData_294(canData);
                break;
            }
            case 0x2A0 + nodeID: {
                if (!Arrays.equals(canData, canData_2A4s)) {
                    ThreadUtils.runOnUiThread(() -> {
                        canData_2A4s = canData;
                        int codeZero = getInteger(canData[0]) + (getInteger(canData[1]) << 8);
                        PersistentData.amplitude = MyUtils.getOneDecimal(codeZero * 0.01) + "";
                        AppData.amplitudeValue = (float) (codeZero * 0.01);
                        int codeTwo = getInteger(canData[2]) + (getInteger(canData[3]) << 8);
                        AppData.heightValue = (float) (codeTwo * 0.01);
                    });
                }
                getCanData_2A4(canData);
                break;
            }
            case 0x2B0 + nodeID:
                getCanData_2B4(canData);
                break;
            case 0x2C0 + nodeID:
                getCanData_2C4(canData);
                break;
            case 0x190 + nodeID: {
                if (!Arrays.equals(canData, canData_194s)) {
                    ThreadUtils.runOnUiThread(() -> {
                        canData_194s = canData;
                        int codeTwo = getInteger(canData[2]) + (getInteger(canData[3]) << 8);
                        PersistentData.l1 = MyUtils.getOneDecimal(codeTwo * 0.01) + "";
                    });
                }
                getCanData_194(canData);
                break;
            }
            case 0x1A0 + nodeID: {
                if (!Arrays.equals(canData, canData_1A4s)) {
                    ThreadUtils.runOnUiThread(() -> {
                        canData_1A4s = canData;
                        int codeTwo = MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                        PersistentData.a = MyUtils.getOneDecimal(codeTwo * 0.01) + "";
                    });
                }
                getCanData_1A4(canData);
                break;
            }
            case 0x1B0 + nodeID: {
                if (!Arrays.equals(canData, canData_1B4s)) {
                    ThreadUtils.runOnUiThread(() -> {
                        canData_1B4s = canData;
                        int codeTwo = getInteger(canData[2]) + (getInteger(canData[3]) << 8);
                        PersistentData.p1 = MyUtils.getOneDecimal(codeTwo * 0.01) + "";
                        int codeSix = getInteger(canData[6]) + (getInteger(canData[7]) << 8);
                        PersistentData.p2 = MyUtils.getOneDecimal(codeSix * 0.01) + "";
                    });
                }
                getCanData_1B4(canData);
                break;
            }
            case 0x2D0 + nodeID:
                getCanData_2D4(canData);
                break;
            case 0x3C0 + nodeID:
                getCanData_3C4(canData);
                break;
            case 0x3D0 + nodeID:
                getCanData_3D4(canData);
                break;
            case 0x3E0 + nodeID:
                getCanData_3E4(canData);
                break;
            case 0x3F0 + nodeID:
                getCanData_3F4(canData);
                break;
            case 0x700 + nodeID: {
                AppData.reckonByTime = 0;
                if (!Arrays.equals(canData, canData_704s)) {
                    ThreadUtils.runOnUiThread(() -> {
                        canData_704s = canData;
                        if (canData[0] != 0x05&&isConfirm) {
                            byte[] data = new byte[]{(byte) 0x01, 0x00, 0x00, 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x01, data));
                        }
                    });
                }
                Log.e("XJW","AppData.reckonByTime:"+AppData.reckonByTime+"   log:"+log);
                getCanData_704(canData);
                break;
            }
            case 0x0CF00400:
                getCanData_0CF00400(canData);
                break;
            case 0x18FEEF00:
                getCanData_18FEEF00(canData);
                break;
            case 0x18FEEE00:
                getCanData_18FEEE00(canData);
                break;
            case 0x1DFF32D1:
                getCanData_1DFF32D1(canData);
                break;
            case 0x1DFF33D1: {
                getCanData_1DFF33D1(canData);
                int codeZero = getInteger(canData[0]);
                int codeOne = getInteger(canData[1]);
                if (((codeZero - 40) > 155) || ((codeOne - 40) > 85)) {
                    AdbUtils.buzzerSwitch(1);
                } else {
                    if (!AppData.isFaultAlarm) {
                        AdbUtils.buzzerSwitch(0);
                    }
                }
                break;
            }
            case 0x1F0 + nodeID:
                getCanData_1F4(canData);
                break;
            case 0x4E0 + nodeID:
                getCanData_4E4(canData);
                break;
            case 0x2E0 + nodeID:
                getCanData_2E8(canData);
                break;
            default:
                break;
        }
        return canData;
    }

    public void getCanData_584(byte[] canData) {

    }

    public void getCanData_1C4(byte[] canData) {

    }

    /**
     * add by chenyaoli,2023.12.27
     *
     * @param canData
     */
    public void getCanData_1E4(byte[] canData) {

    }

    public void getCanData_294(byte[] canData) {

    }

    public void getCanData_2A4(byte[] canData) {

    }

    public void getCanData_2B4(byte[] canData) {

    }

    public void getCanData_2C4(byte[] canData) {

    }

    public void getCanData_194(byte[] canData) {

    }

    public void getCanData_1A4(byte[] canData) {

    }

    public void getCanData_1B4(byte[] canData) {

    }

    public static byte[] canData_Alarm;

    public void getFaultAlarm(byte[] canData) {
        if (!Arrays.equals(canData, canData_Alarm)) {
            int languageState = MMKVUtils.getInstance().decodeInt("languageState");
            canData_Alarm = canData;
            int codeZero = getInteger(canData[0]);
            int overload = codeZero & 0x01;
            if (overload == 1) {
                DataUtils.addList(getString(R.string.overload) + "");
            } else {
                DataUtils.removeList(getString(R.string.overload) + "");
            }

            int overrollAlarm = (codeZero >> 2) & 0x01;
            if (overrollAlarm == 1) {
                DataUtils.addList(getString(R.string.overroll_alarm) + "");
            } else {
                DataUtils.removeList(getString(R.string.overroll_alarm) + "");
            }

            int overDischargeAlarm = (codeZero >> 3) & 0x01;
            if (overDischargeAlarm == 1) {
                DataUtils.addList(getString(R.string.over_discharge_alarm) + "");
            } else {
                DataUtils.removeList(getString(R.string.over_discharge_alarm) + "");
            }

            int fiveLegOverpressureAlarm = (codeZero >> 4) & 0x01;
            if (fiveLegOverpressureAlarm == 1) {
                DataUtils.addList(getString(R.string.five_leg_overpressure_alarm) + "");
            } else {
                DataUtils.removeList(getString(R.string.five_leg_overpressure_alarm) + "");
            }

            int angleUpperLimitAlarm = (codeZero >> 5) & 0x01;
            if (angleUpperLimitAlarm == 1) {
                DataUtils.addList(getString(R.string.angle_upper_limit_alarm) + "");
            } else {
                DataUtils.removeList(getString(R.string.angle_upper_limit_alarm) + "");
            }

            int compulsoryTermination = (codeZero >> 7) & 0x01;
            String alarmStr = getString(R.string.compulsory_termination) + getString(R.string.open) + "";
            if (compulsoryTermination == 1) {
                DataUtils.addList(alarmStr);
            } else {
                DataUtils.removeList(alarmStr);
            }

            int angleLowerLimitAlarm = (codeZero >> 6) & 0x01;
            if (angleLowerLimitAlarm == 1) {
                DataUtils.addList(getString(R.string.angle_lower_limit_alarm) + "");
            } else {
                DataUtils.removeList(getString(R.string.angle_lower_limit_alarm) + "");
            }

            int codeOne = getInteger(canData[1]);
            int length1Fault = codeOne & 0x01 + (((codeOne >> 1) & 0x01) << 1);
            String lAlarmStr = getString(R.string.length_1_fault) + getString(R.string.ad_below_lower_limit) + "";
            if (languageState==2){
                lAlarmStr="Прерывание сигнала датчика длины 1";
            }
            String hAlarmStr = getString(R.string.length_1_fault) + getString(R.string.ad_below_upper_limit) + "";
            if (length1Fault == 1) {
                DataUtils.addList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            } else if (length1Fault == 2) {
                DataUtils.addList(hAlarmStr);
                DataUtils.removeList(lAlarmStr);
            } else {
                DataUtils.removeList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            }

            int length2Fault = ((codeOne >> 2) & 0x01) & 0x01 + (((codeOne >> 3) & 0x01) << 1);
            lAlarmStr = getString(R.string.length_2_fault) + getString(R.string.ad_below_lower_limit) + "";
            if (languageState==2){
                lAlarmStr="Прерывание сигнала датчика длины 2";
            }
            hAlarmStr = getString(R.string.length_2_fault) + getString(R.string.ad_below_upper_limit) + "";
            if (length2Fault == 1) {
                DataUtils.addList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            } else if (length2Fault == 2) {
                DataUtils.addList(hAlarmStr);
                DataUtils.removeList(lAlarmStr);
            } else {
                DataUtils.removeList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            }

            int angle1Fault = ((codeOne >> 4) & 0x01) & 0x01 + (((codeOne >> 5) & 0x01) << 1);
            lAlarmStr = getString(R.string.angle_1_fault) + getString(R.string.ad_below_lower_limit) + "";
            if (languageState==2){
                lAlarmStr="Прерывание сигнала датчика угла 1";
            }
            hAlarmStr = getString(R.string.angle_1_fault) + getString(R.string.ad_below_upper_limit) + "";
            if (angle1Fault == 1) {
                DataUtils.addList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            } else if (angle1Fault == 2) {
                DataUtils.addList(hAlarmStr);
                DataUtils.removeList(lAlarmStr);
            } else {
                DataUtils.removeList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            }

            int angle2Fault = ((codeOne >> 6) & 0x01) & 0x01 + (((codeOne >> 7) & 0x01) << 1);
            lAlarmStr = getString(R.string.angle_2_fault) + getString(R.string.ad_below_lower_limit) + "";
            if (languageState==2){
                lAlarmStr="Прерывание сигнала датчика угла 2";
            }
            hAlarmStr = getString(R.string.angle_2_fault) + getString(R.string.ad_below_upper_limit) + "";
            if (angle2Fault == 1) {
                DataUtils.addList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            } else if (angle2Fault == 2) {
                DataUtils.addList(hAlarmStr);
                DataUtils.removeList(lAlarmStr);
            } else {
                DataUtils.removeList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            }

            int codeTwo = getInteger(canData[2]);
            int pressure1Fault = codeTwo & 0x01 + (((codeTwo >> 1) & 0x01) << 1);
            lAlarmStr = getString(R.string.pressure_1_fault) + getString(R.string.ad_below_lower_limit) + "";
            if (languageState==2){
                lAlarmStr="Прерывание сигнала датчика давления в большой полости";
            }
            hAlarmStr = getString(R.string.pressure_1_fault) + getString(R.string.ad_below_upper_limit) + "";
            if (pressure1Fault == 1) {
                DataUtils.addList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            } else if (pressure1Fault == 2) {
                DataUtils.addList(hAlarmStr);
                DataUtils.removeList(lAlarmStr);
            } else {
                DataUtils.removeList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            }

            int pressure2Fault = ((codeTwo >> 2) & 0x01) & 0x01 + (((codeTwo >> 3) & 0x01) << 1);
            lAlarmStr = getString(R.string.pressure_2_fault) + getString(R.string.ad_below_lower_limit) + "";
            if (languageState==2){
                lAlarmStr="Прерывание сигнала датчика давления в маленькой полости";
            }
            hAlarmStr = getString(R.string.pressure_2_fault) + getString(R.string.ad_below_upper_limit) + "";
            if (pressure2Fault == 1) {
                DataUtils.addList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            } else if (pressure2Fault == 2) {
                DataUtils.addList(hAlarmStr);
                DataUtils.removeList(lAlarmStr);
            } else {
                DataUtils.removeList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            }

            int anemometerMalfunction = ((codeTwo >> 4) & 0x01) & 0x01 + (((codeTwo >> 5) & 0x01) << 1);
            lAlarmStr = getString(R.string.anemometer_malfunction) + getString(R.string.ad_below_lower_limit) + "";
            hAlarmStr = getString(R.string.anemometer_malfunction) + getString(R.string.ad_below_upper_limit) + "";
            if (anemometerMalfunction == 1) {
                DataUtils.addList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            } else if (anemometerMalfunction == 2) {
                DataUtils.addList(hAlarmStr);
                DataUtils.removeList(lAlarmStr);
            } else {
                DataUtils.removeList(lAlarmStr);
                DataUtils.removeList(hAlarmStr);
            }

            int codeThree = getInteger(canData[3]);
            int engineFault = codeThree & 0x01;
            if (engineFault == 1) {
                DataUtils.addList(getString(R.string.engine_fault) + "");
            } else {
                DataUtils.removeList(getString(R.string.engine_fault) + "");
            }

            int excessiveCoolantTemperature = (codeThree >> 1) & 0x01;
            if (excessiveCoolantTemperature == 1) {
                DataUtils.addList(getString(R.string.excessive_coolant_temperature) + "");
            } else {
                DataUtils.removeList(getString(R.string.excessive_coolant_temperature) + "");
            }

            int lowOilPressuse = (codeThree >> 2) & 0x01;
            if (lowOilPressuse == 1) {
                DataUtils.addList(getString(R.string.low_oil_pressuse) + "");
            } else {
                DataUtils.removeList(getString(R.string.low_oil_pressuse) + "");
            }

            int lowFuelVolume = (codeThree >> 3) & 0x01;
            if (lowFuelVolume == 1) {
                DataUtils.addList(getString(R.string.low_fuel_volume) + "");
            } else {
                DataUtils.removeList(getString(R.string.low_fuel_volume) + "");
            }
        }
    }

    public void getCanData_3C4(byte[] canData) {

    }

    public void getCanData_3D4(byte[] canData) {

    }

    public void getCanData_3E4(byte[] canData) {

    }

    public void getCanData_3F4(byte[] canData) {

    }

    public void getCanData_0CF00400(byte[] canData) {

    }

    public void getCanData_18FEEF00(byte[] canData) {

    }

    public void getCanData_18FEEE00(byte[] canData) {

    }

    public void getCanData_1DFF32D1(byte[] canData) {

    }

    public void getCanData_1DFF33D1(byte[] canData) {

    }

    public void getCanData_1F4(byte[] canData) {

    }

    public void getCanData_4E4(byte[] canData) {

    }

    public void getCanData_2E8(byte[] canData) {

    }

    public void getCanData_704(byte[] canData) {
        AppData.dataSrc = canData[2];
    }

    private void getCanData_2D4(byte[] canData) {
        getFaultAlarm(canData);
        if (canData[0] != 0x00 || canData[1] != 0x00 || canData[2] != 0x00 || canData[3] != 0x00
                || canData[4] != 0x00 || canData[5] != 0x00 || canData[6] != 0x00) {
            AppData.isFaultAlarm = true;
            if (AppData.getInstance().voiceState == 0) {
                AdbUtils.buzzerSwitch(1);
            } else {
                AdbUtils.buzzerSwitch(0);
            }
//            if (canData[0] != 0x0 && canData[1] == 0x0 && canData[2] == 0x0) {
//                if (!Arrays.equals(canData, canData_2D4s)) {
//                    ThreadUtils.runOnUiThread(() -> {
//                        canData_2D4s = canData;
//                        int codeZero = getInteger(canData[0]);
//                        int overload = codeZero & 0x01;
//                        if (overload == 1) {
//                        } else {
//                            if (AppData.isOverload) {
//                                //PersistentData.end_date=TimeUtils.getNowString(new SimpleDateFormat("yyyy-MM-dd HH:mm")) + "";
//                                PersistentData.overloadRecordModel = null;
//                                AppData.isOverload = false;
//                                stopOverloadTask();
//                            }
//                        }
//                    });
//                }
//            } else {
//                if (AppData.isOverload) {
//                    //PersistentData.end_date=TimeUtils.getNowString(new SimpleDateFormat("yyyy-MM-dd HH:mm")) + "";
//                    PersistentData.overloadRecordModel = null;
//                    AppData.isOverload = false;
//                    stopOverloadTask();
//                }
//            }
        } else {
//            if (AppData.isOverload) {
//                //PersistentData.end_date=TimeUtils.getNowString(new SimpleDateFormat("yyyy-MM-dd HH:mm")) + "";
//                PersistentData.overloadRecordModel = null;
//                AppData.isOverload = false;
//                stopOverloadTask();
//            }
            AdbUtils.buzzerSwitch(0);
            AppData.isFaultAlarm = false;
        }
    }

    private Handler mHandlerOverload = new Handler();
    private static final int OVER_TIME_MS = 10 * 1000; // 延迟时间

    private void startOverloadTask() {
        mHandlerOverload.post(runnable_Overload);
    }

    private void stopOverloadTask() {
        if (runnable_Overload != null) {
            mHandlerOverload.removeCallbacks(runnable_Overload);
        }
    }

    Runnable runnable_Overload = new Runnable() {
        @Override
        public void run() {
            //要做的事情，这里再次调用此Runnable对象，以实现每两秒实现一次的定时器操作
            if (AppData.isOverload) {
                if (AppData.isOneOverload) {
                    AppData.isOneOverload = false;
                    PersistentData.overloadRecordModel.setStart_date(PersistentData.start_date);
                    PersistentData.overloadRecordModel.setWork_arm(PersistentData.work_arm);
                    PersistentData.overloadRecordModel.setMagnification(PersistentData.magnification);
                    PersistentData.overloadRecordModel.setLeg(PersistentData.leg);
                    PersistentData.overloadRecordModel.setFifth_leg(PersistentData.fifth_leg);
                    PersistentData.overloadRecordModel.setWork_area(PersistentData.work_area);
                    PersistentData.overloadRecordModel.setL1(PersistentData.l1);
                    PersistentData.overloadRecordModel.setA(PersistentData.a);
                    PersistentData.overloadRecordModel.setP1(PersistentData.p1);
                    PersistentData.overloadRecordModel.setP2(PersistentData.p2);
                    PersistentData.overloadRecordModel.setAmplitude(PersistentData.amplitude);
                    PersistentData.overloadRecordModel.setFrontal_weight(PersistentData.frontal_weight);
                    PersistentData.overloadRecordModel.setActual_weight(PersistentData.actual_weight);
                    PersistentData.overloadRecordModel.setMoment_percentage(PersistentData.moment_percentage);
                }
                PersistentData.end_date = TimeUtils.getNowString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")) + "";
                PersistentData.overloadRecordModel.setEnd_date(PersistentData.end_date);
                //插入一条数据
                DatabaseManager.getInstance().insert(PersistentData.overloadRecordModel);
            }
            // 重新安排任务以进行下一次执行
            mHandlerOverload.postDelayed(runnable_Overload, OVER_TIME_MS);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    // 初始化控件
    public abstract void initView();

    // 初始化数据
    public abstract void initData();

    ParentBaseDialog prossDialog = null;

    public void createLoadingDialog(String msg) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.progress_diaolog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                this, R.anim.loading_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        if (msg.equals("")) {
            tipTextView.setText(R.string.loading);// 设置加载信息
        } else {
            tipTextView.setText(msg);
        }

        prossDialog = new ParentBaseDialog(this, R.style.loading_dialog);// 创建自定义样式dialog

//        prossDialog.setCancelable(false);// 不可以用“返回键”取消
//        prossDialog.setCanceledOnTouchOutside(false);
        prossDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));// 设置布局
        prossDialog.show();
    }

    public void dismissLoadingDialog() {
        if (prossDialog != null && prossDialog.isShowing()) {
            prossDialog.dismiss();
            prossDialog = null;
        }
    }

    //只是关闭软键盘
    public void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    protected int dip2pxx(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    protected int dip2px(Context context, float dipValue) {
        return (int) this.dip2pxx(context, dipValue);
    }

    private ScreenStatusReceiver mScreenStatusReceiver;

    private void registSreenStatusReceiver() {
        mScreenStatusReceiver = new ScreenStatusReceiver();
        IntentFilter screenStatusIF = new IntentFilter();
        screenStatusIF.addAction(Intent.ACTION_SCREEN_ON);
        screenStatusIF.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStatusReceiver, screenStatusIF);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (prossDialog != null && prossDialog.isShowing()) {
            prossDialog.dismiss();
            prossDialog = null;
        }
    }

    /**
     * 重写Acitivty的onWindowFocusChanged方法
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        /**
         * 当hasFocus为true的时候，说明Activity的Window对象已经获取焦点，进而Activity界面已经加载绘制完成
         */
        if (hasFocus) {
            //MyUtils.getScreenshot(this);
        }
    }
}
