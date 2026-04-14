package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.widget.HintDialog;

import java.util.Arrays;

public class WeightActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private TextView tvTitle;
    private TextView tvStartDebug;
    private TextView tvParameterRecovery;
    private TextView tvParameterReset;

    private String TYPE="";   //1:砝码1  2：砝码2

    private int hint = -1;
    private HintDialog hintDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weight_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();

        hintDialog = new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm&&(hint==1||hint==2)){
                    createLoadingDialog("");
                    canState=1;
                    startRepeatingTask();
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
            if (canState == 1) {
                if (hint == 1) {
                    byte[] data = new byte[]{(byte) 0x2F, 0x15, 0x10, 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x00, 0x00};
                    if (TYPE.equals("2")) {
                        data[3] = 0x03;
                    }
                    canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+ AppData.nodeID, data));
                } else if (hint == 2) {
                    byte[] data = new byte[]{(byte) 0x2F, 0x15, 0x10, 0x02, (byte) 0x02, (byte) 0x00, (byte) 0x00, 0x00};
                    if (TYPE.equals("2")) {
                        data[3] = 0x03;
                    }
                    canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
                }
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

    private byte[] canData_584;

    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
//        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                if (TYPE.equals("1")){
                    if (canData[0] == 0x60 && canData[1] == 0x15 && canData[2] == 0x10 && canData[3] == 0x02) {
                        stopRepeatingTask();
                        dismissLoadingDialog();
                        canState=0;
                        showDialog(-1, 2, getString(R.string.success_saved));
                    }
//                    else if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x10 && canData[3] == 0x01) {
//                        stopRepeatingTask();
//                        dismissLoadingDialog();
//                        showDialog(-1, 2, getString(R.string.success_saved));
//                    }
                }else if (TYPE.equals("2")){
                    if (canData[0] == 0x60 && canData[1] == 0x15 && canData[2] == 0x10 && canData[3] == 0x03) {
                        stopRepeatingTask();
                        dismissLoadingDialog();
                        canState=0;
                        showDialog(-1, 2, getString(R.string.success_saved));
                    }
//                    else if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x10 && canData[3] == 0x01) {
//                        stopRepeatingTask();
//                        dismissLoadingDialog();
//                        showDialog(-1, 2, getString(R.string.success_saved));
//                    }
                }
            });
       //}
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvTitle = findViewById(R.id.tv_title);
        tvStartDebug = findViewById(R.id.tv_start_debug);
        tvParameterRecovery = findViewById(R.id.tv_parameter_recovery);
        tvParameterReset = findViewById(R.id.tv_parameter_reset);

        TYPE=getIntent().getStringExtra("TYPE")+"";
        if (TYPE.equals("1")){
            tvTitle.setText(getString(R.string.weight_one));
        }else{
            tvTitle.setText(getString(R.string.weight_two));
        }
        llReturn.setOnClickListener(this);
        tvStartDebug.setOnClickListener(this);
        tvParameterRecovery.setOnClickListener(this);
        tvParameterReset.setOnClickListener(this);
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
            case R.id.tv_start_debug:{
                Intent intent=new Intent();
                intent.putExtra("TYPE",TYPE);
                intent.setClass(this,StartDebugActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_parameter_recovery:{
                showDialog(1,3,getString(R.string.determine_the_parameters_for_the_most_recent_recovery));
                break;
            }
            case R.id.tv_parameter_reset:{
                showDialog(2,3,getString(R.string.determine_reset_parameters));
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
}
