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

public class SystemSettingsActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private TextView tvActualWeight;
    private AutoKeyboardEditText etCoefficientP1;
    private TextView btnSave;

    private int hint = -1;
    private HintDialog hintDialog;

    private String currentCoefficientP1Value = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_settings_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
        hintDialog = new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm && hint == 1) {
                    // 首先判断值是否相同，如果相同，不触发；否则触发修改cmd
                    if (currentCoefficientP1Value.equals(etCoefficientP1.getText().toString())){
                        ToastUtils.showShort(getString(R.string.enter_equals));
                        return;
                    }
                    createLoadingDialog("");
                    canState = 1;
                    startRepeatingTask();
                } else if (hint == 2) {
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });
    }

    private Handler mHandler = new Handler();
    private int canState = 0;
    private static final int DELAY_TIME_MS = 100; // 延迟时间
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            // 在这里执行你的任务
            if (canState == 0) {
                // 协议变更， 0x17->0x18
//                byte[] data = new byte[]{(byte) 0x4B, 0x17, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                byte[] data = new byte[]{(byte) 0x4B, 0x18, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+ AppData.nodeID, data));
            } else if (canState == 1) {
                String strP1 = etCoefficientP1.getText().toString();
                byte[] data = new byte[]{(byte) 0x2B, 0x18, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                // 写数据时，也不再加80， add by chenyaoli,2023.12.28
//                int s = (int) (Integer.parseInt(strP1) + 80);
                int s = Integer.parseInt(strP1);
                data[4] = (byte) (s & 0xff);
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            } else if (canState == 2) {
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
        if (mTask != null) {
            mHandler.removeCallbacks(mTask);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
        mHandler = null;
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvActualWeight = findViewById(R.id.tv_actual_weight);
        etCoefficientP1 = findViewById(R.id.et_coefficient_p1);
        btnSave = findViewById(R.id.btn_save);

        llReturn.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    @Override
    public void initData() {
        startRepeatingTask();
    }

    private byte[] canData_294;
    private byte[] canData_584;

    @Override
    public void getCanData_294(byte[] canData) {
        super.getCanData_294(canData);
        if (!Arrays.equals(canData, canData_294)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_294 = canData;
                int codeTwo = MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                tvActualWeight.setText(MyUtils.getOneDecimal(codeTwo * 0.01) + "");
            });
        }
    }

    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
//                if (canData[0] == 0x60 && canData[1] == 0x17 && canData[2] == 0x20 && canData[3] == 0x01) {
//                    stopRepeatingTask();
//                    int codeFour = getInteger(canData[4]);
//                    etCoefficientP1.setText((codeFour - 80) + "");
//                } else if (canData[0] == 0x60 && canData[1] == 0x18 && canData[2] == 0x20 && canData[3] == 0x01) {
//                    canState = 2;
//                } else if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x10 && canData[3] == 0x01) {
//                    stopRepeatingTask();
//                    dismissLoadingDialog();
//                    showDialog(2, 2, getString(R.string.success_saved));
//                }
                // 修改了协议字段，把0x17->0x18，协议回复数据同样需要变更。add by chenyaoli.2023.12.28
                if (canState == 0){
                    stopRepeatingTask();
                }else if (canState == 1){
                    canState = 2;
                }
                if (canData[0] == 0x60 && canData[1] == 0x18 && canData[2] == 0x20 && canData[3] == 0x01) {
                    int codeFour = getInteger(canData[4]);
                    currentCoefficientP1Value = codeFour + "";
                    etCoefficientP1.setText(currentCoefficientP1Value);
                } else if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x10 && canData[3] == 0x01) {
                    stopRepeatingTask();
                    dismissLoadingDialog();
                    showDialog(2, 2, getString(R.string.success_saved));
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_return: {
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.btn_save: {
                String strP1 = etCoefficientP1.getText().toString();
                if (strP1.equals("") || Integer.parseInt(strP1) < 80 || Integer.parseInt(strP1) > 120) {
                    ToastUtils.showShort(getString(R.string.enter_120));
                    return;
                }
                showDialog(1, 3, getString(R.string.save_or_not));
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
