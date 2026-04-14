package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.camera.CameraManager;
import com.hndl.ui.camera.CameraView;
import com.hndl.ui.data.DifferenceData;
import com.hndl.ui.server.VirtualWallService;
import com.hndl.ui.utils.AdbUtils;
import com.hndl.ui.utils.AppUtil;
import com.hndl.ui.utils.DataUtils;
import com.hndl.ui.utils.LogUtil;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.widget.EditDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity implements View.OnClickListener {


    private ConstraintLayout clTop;
    private ProgressBar progressMini;
    private TextView tvMomentPercentage;
    private TextView tvDate;
    private FrameLayout clBottom;
    private LinearLayout llLegState;
    private ImageView ivLegState;
    private LinearLayout llWorkArm;
    private ImageView ivWorkArm;
    private LinearLayout llMagnification;
    private TextView tvMagnification;
    private LinearLayout llQuickView;
    private LinearLayout llVoice;
    private ImageView ivVoice;
    private LinearLayout llState;
    private LinearLayout llSetUp;
    private TextView tvLegState;
    private TextView tvFifthLegState;
    private TextView tvArmLength;
    private TextView tvWorkArea;
    private TextView tvActualWeight;
    private TextView tvFrontalWeight;
    private LinearLayout llFaultAlarm;
    private TextView tvFaultAlarm;
    private LinearLayout llJib;
    private TextView tvJibLength;
    private TextView tvJibAngle;
    private TextView tvHeight;
    private TextView tvLength;
    private TextView tvAngle;
    private TextView tvCounterweight;
    private TextView tvAmplitude;
    private ImageView ivBlueWorkArm;
    private TextView tvWorkArm;
    private TextView tvEngineSpeed;
    private TextView tvFuelVolume;
    private TextView tvCoolingWaterTemperature;
    private TextView tvOilPressure;
    private LinearLayout llSectionArm;
    private LinearLayout llSectionArmState;
    private TextView tvSectionArmState;
    private ImageView ivSectionArm;
    private ImageView ivSectionArmState;
    private LinearLayout llAdh;
    private TextView tvMotorSpeed;
    private LinearLayout llCounterWeight;
    private TextView tvCounterWeight;
    private ImageView ivAmplitude;
    private ProgressBar progressBarAmplitude;
    private TextView tvPercentageAmplitude;
    private ImageView ivVirtualWallAlarm;

    private FrameLayout container1;

    private Timer timerDate = new Timer();
    private TimerTask taskDate;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                if (tvDate != null) {
                    Date currentDate = new Date(System.currentTimeMillis());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    tvDate.setText(dateFormat.format(currentDate) + "");
                }
            }
        }
    };

    private int workArm = 0; //工作臂
    private int hostMagnification = 1; //主臂倍率

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        byte[] data = new byte[]{(byte) 0x01, 0x00, 0x00, 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
        canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x01, data));
        initView();
        initData();
        runCan("224");
        runCan("WorkHours");
        runCan("can");
        startService(new Intent(this, VirtualWallService.class));

        MyUtils.setTextSizeByLanguage(tvFaultAlarm);
        int languageState = MMKVUtils.getInstance().decodeInt("languageState");
        if (languageState!=0){
            tvSectionArmState.setTextSize(9);
        }
    }

    @Override
    public void initView() {

        clTop = findViewById(R.id.cl_top);
        progressMini = findViewById(R.id.progress_mini);
        tvMomentPercentage = findViewById(R.id.tv_moment_percentage);
        tvDate = findViewById(R.id.tv_date);
        clBottom = findViewById(R.id.cl_bottom);
        llLegState = findViewById(R.id.ll_leg_state);
        ivLegState = findViewById(R.id.iv_leg_state);
        llWorkArm = findViewById(R.id.ll_work_arm);
        ivWorkArm = findViewById(R.id.iv_work_arm);
        llMagnification = findViewById(R.id.ll_magnification);
        tvMagnification = findViewById(R.id.tv_magnification);
        llQuickView = findViewById(R.id.ll_quick_view);
        llVoice = findViewById(R.id.ll_voice);
        ivVoice = findViewById(R.id.iv_voice);
        llState = findViewById(R.id.ll_state);
        llSetUp = findViewById(R.id.ll_set_up);
        tvLegState = findViewById(R.id.tv_leg_state);
        tvFifthLegState = findViewById(R.id.tv_fifth_leg_state);
        tvArmLength = findViewById(R.id.tv_arm_length);
        tvWorkArea = findViewById(R.id.tv_work_area);
        tvActualWeight = findViewById(R.id.tv_actual_weight);
        tvFrontalWeight = findViewById(R.id.tv_frontal_weight);
        llFaultAlarm = findViewById(R.id.ll_fault_alarm);
        tvFaultAlarm = findViewById(R.id.tv_fault_alarm);
        llJib = findViewById(R.id.ll_jib);
        tvJibLength = findViewById(R.id.tv_jib_length);
        tvJibAngle = findViewById(R.id.tv_jib_angle);
        tvHeight = findViewById(R.id.tv_height);
        tvLength = findViewById(R.id.tv_length);
        tvAngle = findViewById(R.id.tv_angle);
        tvCounterweight = findViewById(R.id.tv_counterweight);
        tvAmplitude = findViewById(R.id.tv_amplitude);
        ivBlueWorkArm = findViewById(R.id.iv_blue_work_arm);
        tvWorkArm = findViewById(R.id.tv_work_arm);
        tvEngineSpeed = findViewById(R.id.tv_engine_speed);
        tvFuelVolume = findViewById(R.id.tv_fuel_volume);
        tvCoolingWaterTemperature = findViewById(R.id.tv_cooling_water_temperature);
        tvOilPressure = findViewById(R.id.tv_oil_pressure);
        llAdh = findViewById(R.id.ll_adh);
        container1=findViewById(R.id.container1_0);
        tvMotorSpeed=findViewById(R.id.tv_motor_speed);
        llSectionArm=findViewById(R.id.ll_section_arm);
        llSectionArmState=findViewById(R.id.ll_section_arm_state);
        ivSectionArm=findViewById(R.id.iv_section_arm);
        ivSectionArmState=findViewById(R.id.iv_section_arm_state);
        tvSectionArmState=findViewById(R.id.tv_section_arm_state);
        llCounterWeight=findViewById(R.id.ll_counter_weight);
        tvCounterWeight=findViewById(R.id.tv_counter_weight);
        ivAmplitude=findViewById(R.id.iv_amplitude);
        progressBarAmplitude=findViewById(R.id.progressBar_amplitude);
        tvPercentageAmplitude=findViewById(R.id.tv_percentage_amplitude);
        ivVirtualWallAlarm=findViewById(R.id.iv_virtual_wall_alarm);

        llQuickView.setOnClickListener(this);
        llVoice.setOnClickListener(this);
        llSetUp.setOnClickListener(this);
        llState.setOnClickListener(this);
        llMagnification.setOnClickListener(this);
        llLegState.setOnClickListener(this);
        llFaultAlarm.setOnClickListener(this);
        llWorkArm.setOnClickListener(this);
        container1.setOnClickListener(this);
    }

    CameraManager mQuectelCameraManager = CameraManager.getInstance();
    CameraView surfaceview1;
    @Override
    public void initData() {
        AppData.getInstance().voiceState = MMKVUtils.getInstance().decodeInt("voiceState");
        if (AppData.getInstance().voiceState == 0) {
            ivVoice.setImageResource(R.drawable.voice_on_icon);
        } else {
            ivVoice.setImageResource(R.drawable.voice_off_icon);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppData.indexVirtual.set(0);
        if (timerDate==null){
            timerDate=new Timer();
        }
        taskDate = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 100;
                handler.sendMessage(message);
            }
        };
        timerDate.schedule(taskDate, 0, 1000);
        if (AppData.modelType==15 || AppData.modelType==18||AppData.modelType==27|| AppData.modelType==29|| AppData.modelType==32){
            ivSectionArm.setVisibility(View.VISIBLE);
            llSectionArmState.setVisibility(View.VISIBLE);
            if (AppData.modelType==15||AppData.modelType==29) {
                llCounterWeight.setVisibility(View.VISIBLE);
                if (AppData.CounterWeight == 0) {
                    tvCounterWeight.setText("1.2T");
                } else if (AppData.CounterWeight == 1) {
                    tvCounterWeight.setText("4.0T");
                }
            }
            llSectionArm.setOnClickListener(this);
        }else {
            ivSectionArm.setVisibility(View.INVISIBLE);
            llSectionArmState.setVisibility(View.INVISIBLE);
            llCounterWeight.setVisibility(View.INVISIBLE);
        }
        clickCount = 0;
        canState = 0;
        runCan("604");
        if (AppData.isFaultAlarm) {
            llFaultAlarm.setVisibility(View.VISIBLE);
        } else {
            llFaultAlarm.setVisibility(View.GONE);
        }
    }

    private void openAhd(){
        mQuectelCameraManager.openCamera(2, 2, 2);
        surfaceview1 = new CameraView(mQuectelCameraManager.getCarCamera(2), AppData.ahdChannel);
        surfaceview1.setPreviewSize(1280, 720);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container1_0, surfaceview1)
                .commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (scheduled604 != null) {
            scheduled604.cancel(true);
        }
        stopDisplayAlarmInfo();
        taskDate.cancel();
        timerDate.cancel();
        timerDate=null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        openAhd();
        isAhdOne=true;
        startDisplayAlarmInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scheduled604 != null) {
            scheduled604.cancel(true);
        }
        if (scheduled224 != null) {
            scheduled224.cancel(true);
        }
        if (scheduledWorkHours != null) {
            scheduledWorkHours.cancel(true);
        }
        if (scheduledCan != null) {
            scheduledCan.cancel(true);
        }
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            try {
                if (!scheduledExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduledExecutorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutorService.shutdownNow();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //onBackPressed();
            return true;
        } else {// 如果不是back键正常响应
            return super.onKeyDown(keyCode, event);
        }
    }

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);
    private ScheduledFuture scheduled604;
    private ScheduledFuture scheduled224;
    private ScheduledFuture scheduledWorkHours;
    private ScheduledFuture scheduledCan;

    private int canState = 0;

    private void runCan(String canId) {
        if (canId.equals("604")) {
            if (scheduled604 != null) {
                scheduled604.cancel(true);
            }
            scheduled604 = scheduledExecutorService.scheduleAtFixedRate(runnable_604, 0, 100, TimeUnit.MILLISECONDS);
        } else if (canId.equals("224")) {
            if (scheduled224 != null) {
                scheduled224.cancel(true);
            }
            scheduled224 = scheduledExecutorService.scheduleAtFixedRate(runnable_224, 0, 5 * 100, TimeUnit.MILLISECONDS);
        } else if (canId.equals("WorkHours")) {
            if (scheduledWorkHours != null) {
                scheduledWorkHours.cancel(true);
            }
            scheduledWorkHours = scheduledExecutorService.scheduleAtFixedRate(runnable_WorkHours, 10, 100 * 100, TimeUnit.MILLISECONDS);
        } else if (canId.equals("can")) {
            if (scheduledCan != null) {
                scheduledCan.cancel(true);
            }
            scheduledCan = scheduledExecutorService.scheduleAtFixedRate(runnable_can, 0, 10 * 100, TimeUnit.MILLISECONDS);
        }
    }

    Runnable runnable_604 = new Runnable() {
        @Override
        public void run() {
            if (AppData.isVirtualAlarm){
                ivVirtualWallAlarm.setVisibility(View.VISIBLE);
            }else {
                ivVirtualWallAlarm.setVisibility(View.GONE);
            }
            setCanData_604();
        }
    };
    Runnable runnable_224 = new Runnable() {
        @Override
        public void run() {
            setCanData_224();
            //LogUtil.e("XJW","reckonByTime:"+AppData.reckonByTime);
            if (busData == 0x00 || AppData.reckonByTime > 7) {
                if (!AppData.isBusFailure) {
                    AppData.isBusFailure = true;
                }
            } else {
                AppData.isBusFailure = false;
            }
            if (AppData.isBusFailure) {
                byte[] data = new byte[]{(byte) 0x01, 0x00, 0x00, 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x01, data));
                DataUtils.addList(getString(R.string.bus_communication_failure) + "");
            } else {
                DataUtils.removeList(getString(R.string.bus_communication_failure) + "");
            }
        }
    };

    Runnable runnable_WorkHours = new Runnable() {
        @Override
        public void run() {
            AppData.workHours += 10;
            MMKVUtils.getInstance().encode("workHours", AppData.workHours);
        }
    };

    Runnable runnable_can = new Runnable() {
        @Override
        public void run() {
            ThreadUtils.runOnUiThread(() -> {
                tvEngineSpeed.setText(engineSpeed);
            });
            AppData.reckonByTime = AppData.reckonByTime + 1;
            byte[] data = new byte[]{(byte) 0x01, 0x00, 0x00, 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x710+AppData.nodeID, data));
        }
    };

    private void setCanData_604() {
        byte[] data = new byte[]{(byte) 0x43, 0x10, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
        canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
    }

    private void setCanData_224() {
        byte[] data = new byte[]{(byte) 0x00, 0x00, 0x00, 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        data[0] = (byte) (year & 0xff);
        data[1] = (byte) ((month + 1) & 0xff);
        data[2] = (byte) (day & 0xff);

        int week = cal.get(Calendar.DAY_OF_WEEK);
        int dayWeek = 0;
        if (week == Calendar.MONDAY) {
            dayWeek = 1;
        } else if (week == Calendar.TUESDAY) {
            dayWeek = 2;
        } else if (week == Calendar.WEDNESDAY) {
            dayWeek = 3;
        } else if (week == Calendar.THURSDAY) {
            dayWeek = 4;
        } else if (week == Calendar.FRIDAY) {
            dayWeek = 5;
        } else if (week == Calendar.SATURDAY) {
            dayWeek = 6;
        }
        data[3] = (byte) (dayWeek & 0xff);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        data[4] = (byte) (hour & 0xff);
        data[5] = (byte) (minute & 0xff);
        data[6] = (byte) (second & 0xff);
        canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x220+AppData.nodeID, data));
    }

    private byte[] canData_584;
    private byte[] canData_1C4;
    private byte[] canData_294;
    private byte[] canData_2A4;
    private byte[] canData_2B4;
    private byte[] canData_2C4;
    private byte[] canData_194;
    private byte[] canData_1A4;
    private byte[] canData_0CF00400;
    private byte[] canData_18FEEF00;
    private byte[] canData_18FEEE00;
    private byte[] canData_1DFF32D1;
    private byte[] canData_2E8;
    private byte[] mModifyWorkingCanData = new byte[]{0x23, 0x10, 0x20, 0x01, 0x00, 0x00, 0x00, 0x00};
    private byte busData = 0x01;

    @Override
    public void getCanData_704(byte[] canData) {
        super.getCanData_704(canData);
        busData = canData[1];
    }

    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
//        if (!Arrays.equals(canData, canData_584)) {
        ThreadUtils.runOnUiThread(() -> {
            canData_584 = canData;

            if (canData[0] == 0x80 && canData[1] == 0x10 && canData[2] == 0x20 && canData[3] == 0x01) {
                return;
            }else if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x20 && canData[3] == 0x01) {
                // 每次获取到工况信息后，将工况信息更新到 待修改的列表中
                int codeFour = getInteger(canData[4]);
                int codeFive = getInteger(canData[5]);
                workArm = codeFour & 0x07;
                if (workArm == 0) {
                    llJib.setVisibility(View.GONE);
                    ivBlueWorkArm.setImageResource(R.drawable.big_hook_blue_icon);
                    tvWorkArm.setText(getString(R.string.big_hook));
                    ivWorkArm.setImageResource(R.drawable.big_hook_icon);
                } else if (workArm == 1) {
                    llJib.setVisibility(View.GONE);
                    ivBlueWorkArm.setImageResource(R.drawable.small_hook_blue_icon);
                    tvWorkArm.setText(getString(R.string.small_hook));
                    ivWorkArm.setImageResource(R.drawable.small_hook_icon);
                } else if (workArm == 2) {
                    llJib.setVisibility(View.VISIBLE);
                    int armAngle = (codeFive >> 6) & 0x01 + (((codeFive >> 7) & 0x01) << 1);
                    if (armAngle == 0) {
                        tvJibAngle.setText("0");
                        ivBlueWorkArm.setImageResource(R.drawable.jib1_blue_icon);
                        tvWorkArm.setText("0°");
                        ivWorkArm.setImageResource(R.drawable.jib1_icon);
                    } else if (armAngle == 1) {
                        tvJibAngle.setText("15");
                        ivBlueWorkArm.setImageResource(R.drawable.jib2_blue_icon);
                        tvWorkArm.setText("15°");
                        ivWorkArm.setImageResource(R.drawable.jib2_icon);
                    } else {
                        tvJibAngle.setText("30");
                        ivBlueWorkArm.setImageResource(R.drawable.jib3_blue_icon);
                        tvWorkArm.setText("30°");
                        ivWorkArm.setImageResource(R.drawable.jib3_icon);
                    }
                }
//                int magnification = (codeFour >> 2) & 0x01 + (((codeFour >> 3) & 0x01) << 1) + (((codeFour >> 4) & 0x01) << 2) + (((codeFour >> 5) & 0x01) << 3)
//                        + (((codeFour >> 6) & 0x01) << 4) + (((codeFour >> 7) & 0x01) << 5);    //倍率
                // 倍率 取 byte4 的3~7位
                int magnification = (codeFour >> 3) & 0x1F;
                tvMagnification.setText(magnification + "");

                int legState = codeFive & 0x01 + (((codeFive >> 1) & 0x01) << 1);  //支腿状态
                if (legState == 0) {
                    tvLegState.setText(getString(R.string.full_extension));
                    ivLegState.setImageResource(R.drawable.full_state_icon);
                } else if (legState == 1) {
                    tvLegState.setText(getString(R.string.half_extension));
                    ivLegState.setImageResource(R.drawable.half_state_icon);
                } else {
                    tvLegState.setText(getString(R.string.unextended));
                    ivLegState.setImageResource(R.drawable.half_state_icon);
                }
                int fifthLegState = (codeFive >> 2) & 0x01 + (((codeFive >> 3) & 0x01) << 1);  //第五支腿状态
                if (fifthLegState == 0) {
                    tvFifthLegState.setText(getString(R.string.full_extension));
                } else {
                    tvFifthLegState.setText(getString(R.string.unextended));
                }
                int WorkArea = (codeFive >> 4) & 0x01;
                if (WorkArea == 0) {
                    tvWorkArea.setText(getString(R.string.rear_side));
                } else {
                    tvWorkArea.setText(getString(R.string.the_front));
                }
                int armLength = (codeFive >> 5) & 0x01;
                if (armLength == 0) {
                    if (AppData.modelType==15||AppData.modelType==29){
                        tvJibLength.setText("8.3");
                    }else {
                        tvJibLength.setText("0");
                    }
                } else if (armLength == 1) {
                    tvJibLength.setText("15");
                } else {
                    tvJibLength.setText("10");
                }
                int codeSix = getInteger(canData[6]);
                int counterweight = (codeSix >> 0) & 0x01 + (((codeSix >> 1) & 0x01) << 1) + (((codeSix >> 2) & 0x01) << 2);
                AppData.CounterWeight=counterweight;
                if (counterweight == 0) {
                    if (AppData.modelType==15||AppData.modelType==29){
                        tvCounterWeight.setText("1.2T");
                    }
                    tvCounterweight.setText("5.0 t");
                } else if (counterweight == 1) {
                    if (AppData.modelType==15||AppData.modelType==29){
                        tvCounterWeight.setText("4.0T");
                    }
                    tvCounterweight.setText("8.0 t");
                } else {
                    tvCounterweight.setText("10.0 t");
                }
                int codeSeven = getInteger(canData[7]);

                for (int i = 4; i < 8; i++) {
                    mModifyWorkingCanData[i] = canData[i];
                }
                if (canState == 0) {
                    if (scheduled604 != null) {
                        scheduled604.cancel(true);
                    }
                    AppData.isSendVirtual.set(true);
//                    byte[] data = new byte[]{(byte) 0x4F, 0x28, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
//                    canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
                } else if (canState == 1) {
                    byte[] data = new byte[]{(byte) 0x23, 0x10, 0x10, 0x01, (byte) 0x73, (byte) 0x61, (byte) 0x76, 0x65};
                    canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
                    canState = 0;
                }
            }else if (canData[0] == 0x60 && canData[1] == 0x28 && canData[2] == 0x20 && canData[3] == 0x01) {
//                int codeFour = getInteger(canData[4]);
//                AppData.sectionArm=codeFour;
//                if (codeFour==0){
//                    ivSectionArmState.setImageResource(R.drawable.five_section_arm_state_icon);
//                    tvSectionArmState.setText(getString(R.string.five_section_arm));
//                    //ivSectionArm.setImageResource(R.drawable.five_section_arm_icon);
//                }else if (codeFour==1){
//                    ivSectionArmState.setImageResource(R.drawable.six_section_arm_state_icon);
//                    tvSectionArmState.setText(getString(R.string.six_section_arm));
//                    //ivSectionArm.setImageResource(R.drawable.six_section_arm_icon);
//                }
//                if (canState == 1) {
//                    byte[] data = new byte[]{(byte) 0x23, 0x10, 0x10, 0x01, (byte) 0x73, (byte) 0x61, (byte) 0x76, 0x65};
//                    canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
//                    canState = 0;
//                }
            }else if (canData[0] == 0x60 && canData[1] == 0x17 && canData[2] == 0x10 && canData[3] == 0x01) {
//                byte[] data = new byte[]{(byte) 0x23, 0x10, 0x10, 0x01, (byte) 0x73, (byte) 0x61, (byte) 0x76, 0x65};
//                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
//                canState = 0;
            }
        });
        //  }
    }

    @Override
    public void getCanData_2E8(byte[] canData) {
        super.getCanData_2E8(canData);
        if (!Arrays.equals(canData, canData_2E8)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_2E8 = canData;
                int codeTwo = getInteger(canData[2]);
                tvPercentageAmplitude.setText(codeTwo+"%");
                if (codeTwo>100){
                    codeTwo=100;
                }
                progressBarAmplitude.setProgress(codeTwo);
                LayerDrawable layerDrawable = (LayerDrawable) progressBarAmplitude.getProgressDrawable();
                Drawable progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress);
                if (codeTwo > 90) {
                    progressDrawable.setTint(getResources().getColor(android.R.color.holo_red_light));
                } else {
                    progressDrawable.setTint(getResources().getColor(R.color.c4EB20E));
                }
                int codeThree = getInteger(canData[3]);
                if (codeThree==1){
                    ivAmplitude.setVisibility(View.VISIBLE);
                    if (AppData.getInstance().voiceState == 0) {
                        AdbUtils.buzzerSwitch(1);
                    } else {
                        AdbUtils.buzzerSwitch(0);
                    }
                }else {
                    ivAmplitude.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void getCanData_1C4(byte[] canData) {
        super.getCanData_1C4(canData);
        if (!Arrays.equals(canData, canData_1C4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_1C4 = canData;
                int codeTwo = MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                //tvWindSpeed.setText(MyUtils.getOneDecimal(codeTwo * 0.01) + " m/s");
            });
        }
    }

    private byte[] canData_1F4;
    @Override
    public void getCanData_1F4(byte[] canData) {
        super.getCanData_1F4(canData);
        if (!Arrays.equals(canData, canData_1F4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_1F4 = canData;
                int codeSeven= getInteger(canData[7]);
                if (((codeSeven >> 2) & 0x01)==1){
                    ivSectionArmState.setVisibility(View.VISIBLE);
                    ivSectionArmState.setImageResource(R.drawable.five_section_arm_state_icon);
                    tvSectionArmState.setText(getString(R.string.five_section_arm));
                    tvSectionArmState.setTextColor(getColor(R.color.white));
                }else if(((codeSeven >> 1) & 0x01)==1){
                    ivSectionArmState.setVisibility(View.VISIBLE);
                    ivSectionArmState.setImageResource(R.drawable.six_section_arm_state_icon);
                    tvSectionArmState.setText(getString(R.string.six_section_arm));
                    tvSectionArmState.setTextColor(getColor(R.color.white));
                }else {
                    ivSectionArmState.setVisibility(View.GONE);
                    tvSectionArmState.setText(getString(R.string.arm_joint_mode_error));
                    tvSectionArmState.setTextColor(getColor(R.color.red));
                }
            });
        }
    }

    @Override
    public void getCanData_294(byte[] canData) {
        super.getCanData_294(canData);
        if (!Arrays.equals(canData, canData_294)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_294 = canData;
                int codeZero = MyUtils.get16Data((byte) canData[0], (byte) canData[1]);
                tvFrontalWeight.setText(MyUtils.getOneDecimal(codeZero * 0.01) + "");

                int codeTwo = MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                tvActualWeight.setText(MyUtils.getOneDecimal(codeTwo * 0.01) + "");

                int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                double moment_percentage = codeFour * 0.01;
                tvMomentPercentage.setText(MyUtils.getOneDecimal(moment_percentage) + "%");
                progressMini.setProgress((int) moment_percentage);
            });
        }
    }

    @Override
    public void getCanData_2A4(byte[] canData) {
        super.getCanData_2A4(canData);
        if (!Arrays.equals(canData, canData_2A4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_2A4 = canData;
                int codeZero = MyUtils.get16Data((byte) canData[0], (byte) canData[1]);
                tvAmplitude.setText(MyUtils.getOneDecimal(codeZero * 0.01) + " m");

                int codeTwo = MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                tvHeight.setText(MyUtils.getOneDecimal(codeTwo * 0.01) + " m");
            });
        }
    }

    @Override
    public void getCanData_2B4(byte[] canData) {
        super.getCanData_2B4(canData);
        if (!Arrays.equals(canData, canData_2B4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_2B4 = canData;
                int codeFour = getInteger(canData[4]);
                tvFuelVolume.setText(MyUtils.getOneDecimal(codeFour * 0.1) + " %");
            });
        }
    }

    @Override
    public void getCanData_2C4(byte[] canData) {
        super.getCanData_2C4(canData);
        if (!Arrays.equals(canData, canData_2C4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_2C4 = canData;
                int codeTwo = getInteger(canData[3]) + (getInteger(canData[2]) << 8);
                //tvAmbientTemperature.setText((codeTwo) + " ℃");
            });
        }
    }

    @Override
    public void getCanData_194(byte[] canData) {
        super.getCanData_194(canData);
        if (!Arrays.equals(canData, canData_194)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_194 = canData;
                int codeTwo = MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                tvLength.setText(MyUtils.getOneDecimal(codeTwo * 0.01) + " m");

                int codeSix = MyUtils.get16Data((byte) canData[6], (byte) canData[7]);
                tvArmLength.setText(MyUtils.getOneDecimal(codeSix * 0.01) + " m");
            });
        }
    }

    @Override
    public void getCanData_1A4(byte[] canData) {
        super.getCanData_1A4(canData);
        if (!Arrays.equals(canData, canData_1A4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_1A4 = canData;
                int codeTwo = MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                tvAngle.setText(MyUtils.getOneDecimal(codeTwo * 0.01) + " °");
            });
        }
    }

    @Override
    public void getCanData_1DFF32D1(byte[] canData) {
        super.getCanData_1DFF32D1(canData);
        if (!Arrays.equals(canData, canData_1DFF32D1)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_1DFF32D1 = canData;
                int codeTwo =  getInteger(canData_1DFF32D1[2]) + (getInteger(canData_1DFF32D1[3]) << 8);
                tvMotorSpeed.setText(MyUtils.getOneDecimal((codeTwo*0.125)-4000)+" rpm");
            });
        }
    }

    private byte[] canData_this;

    private Handler mAlarmHandler = new Handler();
    private Runnable mAlarmTask;
    private int mAlarmCurIndex = 0;

    private void startDisplayAlarmInfo() {
        stopDisplayAlarmInfo();
        mAlarmTask = new Runnable() {
            @Override
            public void run() {
                if (AppData.mAlarmingList.isEmpty()) {
                    tvFaultAlarm.setText("");
                    llFaultAlarm.setVisibility(View.GONE);
                } else {
                    llFaultAlarm.setVisibility(View.VISIBLE);
                    if (mAlarmCurIndex >= AppData.mAlarmingList.size()) {
                        mAlarmCurIndex = 0;
                    }
                    tvFaultAlarm.setText(AppData.mAlarmingList.get(mAlarmCurIndex));
                    mAlarmCurIndex++;
                }
                mAlarmHandler.postDelayed(mAlarmTask, 2 * 1000);
            }
        };

        mAlarmHandler.post(mAlarmTask);
    }

    private void stopDisplayAlarmInfo() {
        if (mAlarmTask != null) {
            mAlarmHandler.removeCallbacks(mAlarmTask);
        }
    }

    @Override
    public void getFaultAlarm(byte[] canData) {
        super.getFaultAlarm(canData);
        ThreadUtils.runOnUiThread(() -> {
            if (AppData.isFaultAlarm) {
                //llFaultAlarm.setVisibility(View.VISIBLE);
            } else {
                llFaultAlarm.setVisibility(View.GONE);
                AppData.mAlarmingList.clear();
                tvFaultAlarm.setText("");
            }
        });
    }

    private String engineSpeed="0";
    @Override
    public void getCanData_0CF00400(byte[] canData) {
        super.getCanData_0CF00400(canData);
        if (!Arrays.equals(canData, canData_0CF00400)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_0CF00400 = canData;
                int codeThree = getInteger(canData[3]) + (getInteger(canData[4]) << 8);
                engineSpeed=(codeThree / 8) + "";
            });
        }
    }

    @Override
    public void getCanData_18FEEF00(byte[] canData) {
        super.getCanData_18FEEF00(canData);
        if (!Arrays.equals(canData, canData_18FEEF00)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_18FEEF00 = canData;
                int codeThree = getInteger(canData[3]);
                tvOilPressure.setText(MyUtils.getOneDecimal(codeThree * 0.004) + "Mpa");
            });
        }
    }

    @Override
    public void getCanData_18FEEE00(byte[] canData) {
        super.getCanData_18FEEE00(canData);
        if (!Arrays.equals(canData, canData_18FEEE00)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_18FEEE00 = canData;
                int codeZero = getInteger(canData[0]);
                tvCoolingWaterTemperature.setText((codeZero - 40) + " ℃");
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_leg_state: {
                if (mModifyWorkingCanData != null) {
                    int legState = mModifyWorkingCanData[5] & 0x03;
                    int state = (mModifyWorkingCanData[5] >> 2) & 0x3F;
                    if (legState == 0) {
                        legState = 1;
                        tvLegState.setText(getString(R.string.half_extension));
                        ivLegState.setImageResource(R.drawable.half_state_icon);
                    } else if (legState == 1) {
                        legState = 0;
                        tvLegState.setText(getString(R.string.full_extension));
                        ivLegState.setImageResource(R.drawable.full_state_icon);
                    }
                    mModifyWorkingCanData[5] = (byte) (legState | (byte) (state << 2));
                    canState = 1;
                    canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, mModifyWorkingCanData));
                    break;
                }
            }
            case R.id.ll_quick_view: {
                ActivityUtils.startActivity(QuickViewActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.ll_voice: {
                if (AppData.getInstance().voiceState == 0) {
                    AppData.getInstance().voiceState = 1;
                    AdbUtils.buzzerSwitch(0);
//                    ivVoice.setImageResource(R.drawable.voice_on_icon);
                    ivVoice.setImageResource(R.drawable.voice_off_icon);
                } else {
                    AppData.getInstance().voiceState = 0;
                    //AdbUtils.buzzerSwitch(1);
                    ivVoice.setImageResource(R.drawable.voice_on_icon);
                }
                MMKVUtils.getInstance().encode("voiceState", AppData.getInstance().voiceState);
                break;
            }
            case R.id.ll_set_up: {
                ActivityUtils.startActivity(FunctionMenuActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.ll_state: {
                ActivityUtils.startActivity(StatusQueryActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.ll_fault_alarm: {
                ActivityUtils.startActivity(FaultAlarmActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.ll_magnification: {
                if (workArm == 0) {
                    workArm = mModifyWorkingCanData[4] & 0x07;
                    int magnification = (mModifyWorkingCanData[4] >> 3) & 0x1F;
                    //if (magnification != 0) {
                    // 倍率自加1，若达到最大值6，则从1开始
                    if (magnification >= DifferenceData.getMagnification()) {
                        magnification = 1;
                    } else {
                        magnification += 1;
                    }
                    mModifyWorkingCanData[4] = (byte) (workArm | (byte) (magnification << 3));
                    //}
                    tvMagnification.setText(magnification + "");
                    canState = 1;
                    canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, mModifyWorkingCanData));
                }
                break;
            }
            case R.id.ll_work_arm: {
                if (mModifyWorkingCanData != null) {
                    workArm = mModifyWorkingCanData[4] & 0x07;
                    int magnification = (mModifyWorkingCanData[4] >> 3) & 0x1F;
                    int armAngle = (mModifyWorkingCanData[5] >> 6) & 0x03;
                    int angleState = (mModifyWorkingCanData[5]) & 0x3f;
                    if (workArm == 0) {
                        hostMagnification = magnification;
                        magnification = 1;
                        llJib.setVisibility(View.GONE);
                        ivBlueWorkArm.setImageResource(R.drawable.small_hook_blue_icon);
                        tvWorkArm.setText(getString(R.string.small_hook));
                        ivWorkArm.setImageResource(R.drawable.small_hook_icon);
                        workArm = 1;
                    } else if (workArm == 1) {
                        magnification = 1;
                        llJib.setVisibility(View.VISIBLE);
                        tvJibAngle.setText("0");
                        ivBlueWorkArm.setImageResource(R.drawable.jib1_blue_icon);
                        tvWorkArm.setText("0°");
                        ivWorkArm.setImageResource(R.drawable.jib1_icon);
                        workArm = 2;
                        armAngle = 0;
                    } else if (workArm == 2) {
                        llJib.setVisibility(View.VISIBLE);
                        if (armAngle == 0) {
                            magnification = 1;
                            tvJibAngle.setText("15");
                            ivBlueWorkArm.setImageResource(R.drawable.jib2_blue_icon);
                            tvWorkArm.setText("15°");
                            ivWorkArm.setImageResource(R.drawable.jib2_icon);
                            armAngle = 1;
                        } else if (armAngle == 1) {
                            magnification = 1;
                            tvJibAngle.setText("30");
                            ivBlueWorkArm.setImageResource(R.drawable.jib3_blue_icon);
                            tvWorkArm.setText("30°");
                            ivWorkArm.setImageResource(R.drawable.jib3_icon);
                            armAngle = 2;
                        } else {
                            workArm = 0;
                            magnification = hostMagnification;
                            llJib.setVisibility(View.GONE);
                            ivBlueWorkArm.setImageResource(R.drawable.big_hook_blue_icon);
                            tvWorkArm.setText(getString(R.string.big_hook));
                            ivWorkArm.setImageResource(R.drawable.big_hook_icon);
                        }
                    }
                    tvMagnification.setText(magnification+"");
                    mModifyWorkingCanData[4] = (byte) (workArm | (byte) (magnification << 3));
                    mModifyWorkingCanData[5] = (byte) (angleState | (byte) (armAngle << 6));
                    canState = 1;
                    canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, mModifyWorkingCanData));
                }
                break;
            }
            case R.id.ll_section_arm: {
                ActivityUtils.startActivity(ArmStateSelectionActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.container1_0:{
                ActivityUtils.startActivity(AhdMonitorActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            default:
                break;
        }
    }

    private void closeCamera(){
        mQuectelCameraManager.closeCamera(2);
    }

    private int clickCount = 0;

    float y1 = 0;
    float y2 = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            //y1 = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //当手指离开的时候
            y2 = event.getY();
            if (y2 - y1 > 220 & y2 < 300) {
            }
        }
        return super.onTouchEvent(event);
    }
}
