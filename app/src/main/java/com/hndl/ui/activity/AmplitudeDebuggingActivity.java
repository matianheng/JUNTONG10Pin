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

public class AmplitudeDebuggingActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private TextView tvCentreToCentreSpacing;
    private TextView tvAmplitude;
    private AutoKeyboardEditText etMainArmK;
    private AutoKeyboardEditText etAquxiliaryArmK;
    private TextView btnSave;

    private int hint = -1;
    private HintDialog hintDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.amplitude_debugging_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
        startRepeatingTask();
        hintDialog=new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm&&hint==1){
                    createLoadingDialog("");
                    canState=1;
                    startRepeatingTask();
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
                String strMainArmK=etMainArmK.getText().toString();
                String strAquxiliaryArmK=etAquxiliaryArmK.getText().toString();
                byte[] data = new byte[]{(byte) 0x2B, 0x12, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                data[4] = (byte) (Integer.parseInt(strMainArmK) & 0xff);
                data[5] = (byte) (Integer.parseInt(strAquxiliaryArmK) & 0xff);
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            }else if (canState==2){
                byte[] data = new byte[]{(byte) 0x23, 0x10, 0x10, 0x01, (byte) 0x73, (byte) 0x61, (byte) 0x76, 0x65};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            }else if (canState==3){
                byte[] data = new byte[]{(byte) 0x4B, 0x12, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
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

    private byte[] canData_584;
    private byte[] canData_2A4;
    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                if (canData[0] == 0x60 && canData[1] == 0x11 && canData[2] == 0x20 && canData[3] == 0x01) {
                    int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                    tvCentreToCentreSpacing.setText(MyUtils.getOneDecimal(codeFour*0.01)+" m");
                    canState=3;
                }else if (canData[0] == 0x60 && canData[1] == 0x12 && canData[2] == 0x20 && canData[3] == 0x01) {
                    if (canState==3){
                        stopRepeatingTask();
                        int codeFour = getInteger(canData[4]);
                        etMainArmK.setText(codeFour+"");
                        int codeFive = getInteger(canData[5]);
                        etAquxiliaryArmK.setText(codeFive+"");
                    }else {
                        canState = 2;
                    }
                }else if (canData[0]==0x60&&canData[1]==0x10&&canData[2]==0x10&&canData[3]==0x01){
                    stopRepeatingTask();
                    dismissLoadingDialog();
                    showDialog(-1,2,getString(R.string.success_saved));
                }
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
                tvAmplitude.setText(MyUtils.getOneDecimal(codeZero*0.01)+" m");
            });
        }
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvCentreToCentreSpacing = findViewById(R.id.tv_centre_to_centre_spacing);
        tvAmplitude = findViewById(R.id.tv_amplitude);
        etMainArmK = findViewById(R.id.et_main_arm_K);
        etAquxiliaryArmK = findViewById(R.id.et_aquxiliary_arm_K);
        btnSave = findViewById(R.id.btn_save);

        llReturn.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    @Override
    public void initData() {
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
                String strMainArmK=etMainArmK.getText().toString();
                if (strMainArmK.equals("")||Integer.parseInt(strMainArmK)<-20||Integer.parseInt(strMainArmK)>100){
                    ToastUtils.showShort(getString(R.string.main_arm_K)+getString(R.string.enter_100));
                    return;
                }
                String strAquxiliaryArmK=etAquxiliaryArmK.getText().toString();
                if (strAquxiliaryArmK.equals("")||Integer.parseInt(strAquxiliaryArmK)<-20||Integer.parseInt(strAquxiliaryArmK)>100){
                    ToastUtils.showShort(getString(R.string.aquxiliary_arm_K)+getString(R.string.enter_100));
                    return;
                }
                showDialog(1,3,getString(R.string.save_or_not));
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hintDialog!=null){
            hintDialog.cancel();
            hintDialog=null;
        }
        if (mTask!=null){
            stopRepeatingTask();
            mTask=null;
            mHandler=null;
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
