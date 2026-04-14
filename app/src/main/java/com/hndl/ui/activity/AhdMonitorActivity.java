package com.hndl.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.camera.CameraManager;
import com.hndl.ui.camera.CameraView;
import com.hndl.ui.utils.LogUtil;
import com.quectel.qcarapi.cb.IQCarCamInStatusCB;
import com.quectel.qcarapi.helper.QCarCamInDetectHelper;

public class AhdMonitorActivity extends BaseActivity {
    //implements IQCarCamInStatusCB

    private LinearLayout llReturn;
    private FrameLayout fl1;
    private FrameLayout container10;
    private FrameLayout fl2;
    private FrameLayout container20;
    private LinearLayout llAhdSwitch;
    private TextView tvAdh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ahd_monitor_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        tvAdh.setText((AppData.ahdChannel+1)+"");
        //initData();
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        fl1 = findViewById(R.id.fl1);
        container10 = findViewById(R.id.container1_0);
        fl2 = findViewById(R.id.fl2);
        container20 = findViewById(R.id.container2_0);
        llAhdSwitch=findViewById(R.id.ll_ahd_switch);
        tvAdh=findViewById(R.id.tv_adh);

        llReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        llAhdSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppData.ahdChannel==0){
                    AppData.ahdChannel=1;
                }else {
                    AppData.ahdChannel=0;
                }
                tvAdh.setText((AppData.ahdChannel+1)+"");
                surfaceview0 = new CameraView(mQuectelCameraManager.getCarCamera(2), AppData.ahdChannel);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container1_0, surfaceview0)
                        .commit();
            }
        });
    }

    CameraManager mQuectelCameraManager = CameraManager.getInstance();
    CameraView surfaceview0;

    @Override
    public void initData() {
        mQuectelCameraManager.openCamera(2, 2, 2);
        surfaceview0 = new CameraView(mQuectelCameraManager.getCarCamera(2), AppData.ahdChannel);
        surfaceview0.setPreviewSize(1280, 720);

//        surfaceview1 = new CameraView(mQuectelCameraManager.getCarCamera(2), 1);
//        surfaceview1.setPreviewSize(1280, 720);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container1_0, surfaceview0)
                //.replace(R.id.container2_0, surfaceview1)
                .commit();
//        if (false) {
//            QCarCamInDetectHelper.InputParam inputParam = new QCarCamInDetectHelper.InputParam();
//            inputParam.qCarCamera = mQuectelCameraManager.getCarCamera(2);
//            inputParam.inputNum = 2;
//            QCarCamInDetectHelper.getInstance(this).setInputParam(inputParam);
//            QCarCamInDetectHelper.getInstance(this).startDetectThread();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThreadUtils.runOnUiThreadDelayed(() -> {
            initData();
        }, 50);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //QCarCamInDetectHelper.getInstance(this).stopDetectThread();
        //closeCamera();
    }

    private void closeCamera() {
        mQuectelCameraManager.closeCamera(2);
    }

//    @Override
//    public void statusCB(int i, int i1, int i2, boolean b) {
//        if (i1 == 1) {
//            isAhdOne=false;
//            AppData.isAhdB=b;
//            if (b) {
//                ThreadUtils.runOnUiThread(() -> {
//                    fl2.setVisibility(View.GONE);
//                });
//            } else {
//                ThreadUtils.runOnUiThread(() -> {
//                    fl2.setVisibility(View.GONE);
//                });
//            }
//        }
//    }
}
