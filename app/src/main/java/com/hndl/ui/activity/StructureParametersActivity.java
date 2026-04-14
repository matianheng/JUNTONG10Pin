package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.sdk.kb.AutoKeyboardEditText;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.widget.HintDialog;

import java.util.Arrays;

public class StructureParametersActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout llReturn;
    private AutoKeyboardEditText etSpacing;
    private AutoKeyboardEditText etChassisHeight;
    private TextView btnSave;

    private int hint = -1;
    private HintDialog hintDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.structure_parameters_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
        hintDialog=new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm&&hint==1){
                    createLoadingDialog("");
                    canState=1;
                    startRepeatingTask();
                }else if (hint==2){
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });
    }

    private Handler mHandler = new Handler();
    private int canState=0;
    private static final int DELAY_TIME_MS = 100; // 延迟时间
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            // 在这里执行你的任务
            if (canState==0){
                byte[] data = new byte[]{(byte) 0x43, 0x11, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+ AppData.nodeID, data));
            }else if (canState==1){
                byte[] data = new byte[]{(byte) 0x23, 0x11, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                String strSpacing = etSpacing.getText().toString();
                int s= (int) (Double.parseDouble(strSpacing)*100);
                data[4] = (byte) (s & 0xff);
                data[5] = (byte) ((s >> 8) & 0xff);
                String strChassisHeight = etChassisHeight.getText().toString();
                int c= (int) (Double.parseDouble(strChassisHeight)*100);
                data[6] = (byte) (c & 0xff);
                data[7] = (byte) ((c >> 8) & 0xff);
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            }else if (canState==2){
                byte[] data = new byte[]{(byte) 0x23, 0x10, 0x10, 0x01, (byte) 0x73, (byte) 0x61, (byte) 0x76, 0x65};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            }
            // 重新安排任务以进行下一次执行
            mHandler.postDelayed(mTask, DELAY_TIME_MS);
        }
    };
    private void startRepeatingTask() {
        mHandler.post(mTask);
    }

    private void stopRepeatingTask() {
        if (mTask!=null) {
            mHandler.removeCallbacks(mTask);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
        mHandler=null;
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        etSpacing = findViewById(R.id.et_spacing);
        etChassisHeight = findViewById(R.id.et_chassis_height);
        btnSave = findViewById(R.id.btn_save);

        llReturn.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    @Override
    public void initData() {
        startRepeatingTask();
    }

    private byte[] canData_584;
    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                if (canData[0]==0x60&&canData[1]==0x11&&canData[2]==0x20&&canData[3]==0x01){
                    int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                    etSpacing.setText(MyUtils.getOneDecimal(codeFour*0.01)+"");

                    int codeSix = getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                    etChassisHeight.setText(MyUtils.getOneDecimal(codeSix*0.01)+"");
                    if (canState==1){
                        canState=2;
                    }else {
                        stopRepeatingTask();
                    }
                }else if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x10 && canData[3] == 0x01) {
                    stopRepeatingTask();
                    dismissLoadingDialog();
                    showDialog(-1,2,getString(R.string.success_saved));
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_return:{
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.btn_save:{
                String strSpacing=etSpacing.getText().toString();
                if (strSpacing.equals("")){
                    ToastUtils.showShort(getString(R.string.spacing)+getString(R.string.enter));
                    return;
                }
                String strChassisHeight=etChassisHeight.getText().toString();
                if (strChassisHeight.equals("")){
                    ToastUtils.showShort(getString(R.string.chassis_height)+getString(R.string.enter));
                    return;
                }
                showDialog(1,3,getString(R.string.save_or_not));
                break;
            }
        }
    }

    private void showDialog(int h, int type, String content) {
        hint = h;
        hintDialog.setButtonVisibility(type);
        hintDialog.setContent(content);
        hintDialog.setCancelable(false);
        hintDialog.show();
    }
}
