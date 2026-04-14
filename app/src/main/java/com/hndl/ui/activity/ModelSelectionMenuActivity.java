package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.AppData.serialPort;
import static com.hndl.ui.utils.FileCopyUtilsKt.copyBootAnimation;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.IpGetUtil;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.widget.SelecteListDialog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ModelSelectionMenuActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private TextView tvModelSelection;
    private TextView tvTimeDate;
    private TextView tvLanguage;
    private TextView tvCan1;
    private TextView tvCan2;

    private int languageState = 0; //语音切换 0：中文 1：英文 2：俄文
    private String[] strLanguage = {AppData.getInstance().getString(R.string.Chinese), AppData.getInstance().getString(R.string.English),AppData.getInstance().getString(R.string.Russian)};
    private List<Map<String, Object>> listLanguage = new ArrayList<>();

    private int can1Str = 250;
    private int can2Str = 250;
    private int[] strCan = {100,125,250,500,666,1000};
    private List<Map<String, Object>> listCan = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.model_selection_menu_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvModelSelection = findViewById(R.id.tv_model_selection);
        tvTimeDate = findViewById(R.id.tv_time_date);
        tvLanguage = findViewById(R.id.tv_language);
        tvCan1 = findViewById(R.id.tv_can1_selection);
        tvCan2 = findViewById(R.id.tv_can2_selection);

        llReturn.setOnClickListener(this);
        tvModelSelection.setOnClickListener(this);
        tvTimeDate.setOnClickListener(this);
        tvLanguage.setOnClickListener(this);
        tvCan1.setOnClickListener(this);
        tvCan2.setOnClickListener(this);
    }

    @Override
    public void initData() {
        languageState = MMKVUtils.getInstance().decodeInt("languageState");
        tvLanguage.setText(getString(R.string.language));
        can1Str = MMKVUtils.getInstance().decodeInt("can1Baud");
        can2Str = MMKVUtils.getInstance().decodeInt("can2Baud");
        if (can1Str==0){
            can1Str=250;
        }
        if (can2Str==0){
            can2Str=250;
        }
        tvCan1.setText(getResources().getString(R.string.CAN1_baud_rate)+"("+can1Str+")");
        tvCan2.setText(getResources().getString(R.string.CAN2_baud_rate)+"("+can2Str+")");

        byte[] data = new byte[]{(byte) 0x4F, 0x22, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
        canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));

        for (int i = 0; i < strLanguage.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("KEY", strLanguage[i] + "");
            map.put("VLAUE", i);
            listLanguage.add(map);
        }
        for (int i = 0; i < strCan.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("KEY", strCan[i] + "K");
            map.put("VLAUE", strCan[i]);
            listCan.add(map);
        }
    }

    private byte[] canData_584;
    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                if (canData[0] == 0x60 && canData[1] == 0x22 && canData[2] == 0x20 && canData[3] == 0x01) {
                    AppData.modelType=getInteger(canData[4]);
                    MMKVUtils.getInstance().encode("modelType", AppData.modelType);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_return:{
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_model_selection:{
                ActivityUtils.startActivity(ModelSelectionActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_time_date:{
                ActivityUtils.startActivity(TimeDateActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_language:{
                SelecteListDialog sld = new SelecteListDialog(this, listLanguage, getString(R.string.please_select),
                        new SelecteListDialog.SelecteListListener() {
                            @Override
                            public void onClick(int index) {
                                int type = (int) listLanguage.get(index).get("VLAUE");
                                if (languageState!=type){
                                    if (type == 0) {
                                        languageState = 0;
                                        setLanguage(Locale.CHINESE);
                                    } else if (type == 1) {
                                        languageState = 1;
                                        setLanguage(Locale.ENGLISH);
                                    } else if (type == 2) {
                                        languageState = 2;
                                        Locale russianLocale = new Locale("ru", "RU");
                                        setLanguage(russianLocale);
                                    }
                                    if (languageState == 2) {
                                        copyBootAnimation(ModelSelectionMenuActivity.this, "bootanimation_ru.zip");
                                    } else {
                                        copyBootAnimation(ModelSelectionMenuActivity.this, "bootanimation.zip");
                                    }
                                    MMKVUtils.getInstance().encode("languageState", languageState);
                                    ThreadUtils.runOnUiThreadDelayed(() -> {
                                        Intent intent = new Intent();
                                        intent.setClass(ModelSelectionMenuActivity.this, SplashActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(0, 0);
                                    }, 1800);
                                }
                            }
                        });
                sld.show();
                break;
            }
            case R.id.tv_can1_selection:{
                SelecteListDialog sld = new SelecteListDialog(this, listCan, getString(R.string.please_select),
                        new SelecteListDialog.SelecteListListener() {
                            @Override
                            public void onClick(int index) {
                                int type = (int) listCan.get(index).get("VLAUE");
                                if (can1Str!=type){
                                    if (canSerialManager.updateCanBaud(0, type)) {
                                        can1Str=type;
                                        tvCan1.setText(getResources().getString(R.string.CAN1_baud_rate)+"("+can1Str+")");
                                        ToastUtils.showShort(getString(R.string.success_saved));
                                    } else {
                                        ToastUtils.showShort(getString(R.string.fail));
                                    }
                                }
                            }
                        });
                sld.show();
                break;
            }
            case R.id.tv_can2_selection:{
                SelecteListDialog sld = new SelecteListDialog(this, listCan, getString(R.string.please_select),
                        new SelecteListDialog.SelecteListListener() {
                            @Override
                            public void onClick(int index) {
                                int type = (int) listCan.get(index).get("VLAUE");
                                if (can2Str!=type){
                                    if (canSerialManager.updateCanBaud(1, type)) {
                                        can2Str=type;
                                        tvCan2.setText(getResources().getString(R.string.CAN2_baud_rate)+"("+can2Str+")");
                                        ToastUtils.showShort(getString(R.string.success_saved));
                                    } else {
                                        ToastUtils.showShort(getString(R.string.fail));
                                    }
                                }
                            }
                        });
                sld.show();
                break;
            }
        }
    }


    private void setLanguage(Locale mLocale) {
        try {
            Class localPicker = Class.forName("com.android.internal.app.LocalePicker");
            Method updateLocale = localPicker.getDeclaredMethod("updateLocale",
                    Locale.class);
            updateLocale.invoke(null, mLocale);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
                 | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
