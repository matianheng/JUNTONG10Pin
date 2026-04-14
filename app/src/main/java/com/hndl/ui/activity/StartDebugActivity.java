package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import com.android.sdk.kb.AutoKeyboardEditText;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.DecimalDigitsInputFilter;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.widget.HintDialog;

import java.util.Arrays;

public class StartDebugActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout llReturn;
    private TextView tvWeight;
    private AutoKeyboardEditText etWeight;
    private TextView btnSave;
    private String TYPE = "";   //0:空钩 1:砝码1  2：砝码2

    private int hint = -1;
    private HintDialog hintDialog;

    private String currentWeightValue = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_debug_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
        hintDialog = new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm) {
                    if (hint == 1) {
                        // 首先判断值是否相同，如果相同，则直接跳转到调试详情页面；否则触发修改中条调试砝码值的cmd
                        if (currentWeightValue.equals(etWeight.getText().toString())){
                            jumpToDebugTailsActivity();
                            return;
                        }
                        createLoadingDialog("");
                        canState=1;
                        startRepeatingTask();
                    } else if (hint == 2) {
                        // 跳转到下一个页面（调试详情页面），不再直接关闭该activity, add by chenyaoli,2023.12.27
//                        finish();
//                        overridePendingTransition(0, 0);
                        jumpToDebugTailsActivity();
                    }
                }
            }
        });
    }

    /**
     * 替代 startActivityForResult 方法，接收来自 DebugTailsActivity 的回复数据
     */
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
//                    String data = result.getData().getStringExtra("data");
                    finish();
                    overridePendingTransition(0, 0);
                }
            });

    private void jumpToDebugTailsActivity(){
        Intent intent = new Intent();
        intent.putExtra("TYPE", TYPE);
        intent.setClass(StartDebugActivity.this, DebugDetailsActivity.class);
//        startActivity(intent);
        activityResultLauncher.launch(intent);

        overridePendingTransition(0, 0);
    }

    private Handler mHandler = new Handler();
    private int canState = 0;
    private static final int DELAY_TIME_MS = 100; // 延迟时间
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            // 在这里执行你的任务
            if (canState == 0) {
                byte[] data = new byte[]{(byte) 0x4B, 0x19, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                TYPE = getIntent().getStringExtra("TYPE") + "";
                if (TYPE.equals("1")) {
                    tvWeight.setText(getString(R.string.weight_one_weight) + "(t)");
                    data[3] = 0x02;
                } else if (TYPE.equals("2")) {
                    tvWeight.setText(getString(R.string.weight_two_weight) + "(t)");
                    data[3] = 0x03;
                } else {
                    tvWeight.setText(getString(R.string.empty_hook_weight) + "(t)");
                }
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+ AppData.nodeID, data));
            } else if (canState == 1) {
                String strWeight = etWeight.getText().toString();
                byte[] data = new byte[]{(byte) 0x2B, 0x19, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                if (TYPE.equals("1")) {
                    data[3] = 0x02;
                } else if (TYPE.equals("2")) {
                    data[3] = 0x03;
                }
                int s = (int) (Double.parseDouble(strWeight) * 100);
                data[4] = (byte) (s & 0xff);
                data[5] = (byte) ((s >> 8) & 0xff);
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
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvWeight = findViewById(R.id.tv_weight);
        etWeight = findViewById(R.id.et_weight);
        btnSave = findViewById(R.id.btn_save);

        llReturn.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        etWeight.setFilters(DecimalDigitsInputFilter.getFilters(new DecimalDigitsInputFilter(5, 2)));
    }

    @Override
    public void initData() {
        // 将启动循环获取，放到了onStart()函数中，modify by chenyaoli，2023.12.27
//        startRepeatingTask();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 当UI可见时，循环获取重量调试砝码值，add by chenyaoli，2023.12.27
        canState = 0;
        startRepeatingTask();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 当UI可见时，停止循环获取，add by chenyaoli，2023.12.27
        stopRepeatingTask();
    }

    private byte[] canData_584;

    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
//        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                //Log.e("XJW","onDataSent:"+ ConvertUtils.bytes2HexString(canData_584));
                if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x10 && canData[3] == 0x01) {
                    stopRepeatingTask();
//                    String strWeight = etWeight.getText().toString();
//                    if (TYPE.equals("1")) {
//                        MMKVUtils.getInstance().encode("weight_one", Double.parseDouble(strWeight) * 100);
//                    } else if (TYPE.equals("2")) {
//                        MMKVUtils.getInstance().encode("weight_two", Double.parseDouble(strWeight) * 100);
//                    } else {
//                        MMKVUtils.getInstance().encode("empty_hook", Double.parseDouble(strWeight) * 100);
//                    }
                    dismissLoadingDialog();
                    showDialog(2, 2, getString(R.string.success_saved));
                } else if (canData[0] == 0x60 && canData[1] == 0x19 && canData[2] == 0x20){
                    if (canState==0){
                        stopRepeatingTask();
                    }else if (canState==1){
                        canState=2;
                    }
                    if (TYPE.equals("1")) {
                        if (canData[0] == 0x60 && canData[1] == 0x19 && canData[2] == 0x20 && canData[3] == 0x02) {
                            int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                            currentWeightValue = MyUtils.getOneDecimal(codeFour * 0.01) + "";
                            etWeight.setText(currentWeightValue);
                        }
                    } else if (TYPE.equals("2")) {
                        if (canData[0] == 0x60 && canData[1] == 0x19 && canData[2] == 0x20 && canData[3] == 0x03) {
                            int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                            currentWeightValue = MyUtils.getOneDecimal(codeFour * 0.01) + "";
                            etWeight.setText(currentWeightValue);
                        }
                    } else {
                        if (canData[0] == 0x60 && canData[1] == 0x19 && canData[2] == 0x20 && canData[3] == 0x01) {
                            int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                            currentWeightValue = MyUtils.getOneDecimal(codeFour * 0.01) + "";
                            etWeight.setText(currentWeightValue);
                        }
                    }
                }
            });
//        }
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
                String strMainArmK = etWeight.getText().toString();
                if (strMainArmK.equals("")) {
                    ToastUtils.showShort(getString(R.string.enter));
                    return;
                }
                showDialog(1, 3, getString(R.string.save_or_not));
                break;
            }
            default:
                break;
        }
    }

    private void showDialog(int h, int type, String content) {
        hint = h;
        hintDialog.setButtonVisibility(type);
        hintDialog.setContent(content);
        hintDialog.setCancelable(false);
        hintDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hintDialog != null) {
            hintDialog.cancel();
            hintDialog = null;
        }
        stopRepeatingTask();
        mHandler = null;
    }
}
