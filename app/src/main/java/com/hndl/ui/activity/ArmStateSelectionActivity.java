package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmStateSelectionActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private GridView gridView;
    private TextView tvFiveSectionArm;
    private TextView tvSixSectionArm;
    private TextView tvPushRodContraction;
    private TextView tvTips;
    private TextView tv1, tv2, tv3;

    private String[] strName = {AppData.getInstance().getString(R.string.proximity_switch),
            AppData.getInstance().getString(R.string.push_rod_in_place), AppData.getInstance().getString(R.string.push_rod_retracted_into_place)
    };
    private List<Map<String, Object>> listData = new ArrayList<>();
    private int buttomType = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arm_state_selection_layout);
        initView();
        initData();
        //startRepeatingTask();
    }

    private Handler mHandler = new Handler();
    private int canState = 0;
    private static final int DELAY_TIME_MS = 100; // 延迟时间
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            // 在这里执行你的任务
            if (canState == 0) {
                byte[] data = new byte[]{(byte) 0x4F, 0x28, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600 + AppData.nodeID, data));
            } else if (canState == 1) {
                byte[] data = new byte[]{(byte) 0x2F, 0x28, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                if (buttomType == 6) {
                    data[4] = 0x01;
                }
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600 + AppData.nodeID, data));
            } else if (canState == 2) {
                byte[] data = new byte[]{(byte) 0x2F, 0x17, 0x10, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                if (buttomType == 5) {
                    data[4] = 0x01;
                }
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600 + AppData.nodeID, data));
            } else if (canState == 3) {
                byte[] data = new byte[]{(byte) 0x23, 0x10, 0x10, 0x01, (byte) 0x73, (byte) 0x61, (byte) 0x76, 0x65};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600 + AppData.nodeID, data));
            }
            // 重新安排任务以进行下一次执行
            mHandler.postDelayed(mTask, DELAY_TIME_MS);
        }
    };

    private void startRepeatingTask() {
        stopRepeatingTask();
        mHandler.post(mTask);
    }

    private void stopRepeatingTask() {
        if (mTask != null) {
            mHandler.removeCallbacks(mTask);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
        mHandler = null;
    }

    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        ThreadUtils.runOnUiThread(() -> {
            if (canData[0] == 0x60 && canData[1] == 0x28 && canData[2] == 0x20 && canData[3] == 0x01) {
                if (canState == 0) {
                    stopRepeatingTask();
                    tvTips.setText("");
                    dismissLoadingDialog();
                } else if (canState == 1) {
                    canState = 3;
                }
//                int codeFour = getInteger(canData[4]);
//                AppData.sectionArm=codeFour;
//                if (codeFour==0){
//                    tvFiveSectionArm.setBackgroundResource(R.drawable.blue_bg_circle);
//                    tvSixSectionArm.setBackgroundResource(R.drawable.grey_bg_circle);
//                    tvPushRodContraction.setText(getString(R.string.five_section_arm));
//                }else if (codeFour==1){
//                    tvFiveSectionArm.setBackgroundResource(R.drawable.grey_bg_circle);
//                    tvSixSectionArm.setBackgroundResource(R.drawable.blue_bg_circle);
//                    tvPushRodContraction.setText(getString(R.string.six_section_arm));
//                }else {
//                    tvFiveSectionArm.setBackgroundResource(R.drawable.grey_bg_circle);
//                    tvSixSectionArm.setBackgroundResource(R.drawable.grey_bg_circle);
//                    tvPushRodContraction.setText(getString(R.string.arm_joint_mode_error));
//                }
            } else if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x10 && canData[3] == 0x01) {
                canState = 0;
                stopRepeatingTask();
                tvTips.setText("");
                showState();
                adapter.notifyDataSetChanged();
                dismissLoadingDialog();
            }
        });
    }

    private int proximitySwitch = 0;
    private byte[] canData_1F4;

    @Override
    public void getCanData_1F4(byte[] canData) {
        super.getCanData_1F4(canData);
        if (!Arrays.equals(canData, canData_1F4)) {
            canData_1F4 = canData;
            int codeSeven = getInteger(canData[7]);
            proximitySwitch = codeSeven & 0x01;
            listData.get(0).put("state", (codeSeven & 0x01) + "");
            listData.get(1).put("state", ((codeSeven >> 1) & 0x01) + "");
            listData.get(2).put("state", ((codeSeven >> 2) & 0x01) + "");
            if (canState==0){
                ThreadUtils.runOnUiThread(() -> {
                    showState();
                    adapter.notifyDataSetChanged();
                });
            }else {
                if (((codeSeven >> 2) & 0x01) == 1 || ((codeSeven >> 1) & 0x01) == 1) {
                    canState = 1;
                }else {
                    ThreadUtils.runOnUiThreadDelayed(() -> {
                        showState();
                        adapter.notifyDataSetChanged();
                    }, 50);
                }
            }
        }
    }

    private byte[] canData_194;
    private double twoArmLength = 0;

    @Override
    public void getCanData_194(byte[] canData) {
        super.getCanData_194(canData);
        if (!Arrays.equals(canData, canData_194)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_194 = canData;
                int codeSix = MyUtils.get16Data((byte) canData[6], (byte) canData[7]);
                twoArmLength = Double.parseDouble(MyUtils.getOneDecimal(codeSix * 0.01));
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void initView() {
        llReturn = findViewById(R.id.ll_return);
        gridView = findViewById(R.id.gridView);
        tvFiveSectionArm = findViewById(R.id.tv_five_section_arm);
        tvSixSectionArm = findViewById(R.id.tv_six_section_arm);
        tvPushRodContraction = findViewById(R.id.tv_push_rod_contraction);
        tvTips = findViewById(R.id.tv_tips);
        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        tv3 = findViewById(R.id.tv3);

        llReturn.setOnClickListener(this);

        tvFiveSectionArm.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (proximitySwitch == 1 && twoArmLength <= 0.3 && canState != 3) {
                            tvFiveSectionArm.setBackgroundResource(R.drawable.blue_bg_circle);
                            buttomType = 5;
                            if (languageState == 2) {
                                tvTips.setText("В процессе переключение  пятисекционной стрелы");
                            } else {
                                tvTips.setText(getString(R.string.switching_in_progress) + " " + getString(R.string.five_section_arm));
                            }
                            canState = 2;
                            startRepeatingTask();
                        } else {
                            ToastUtils.showShort(getString(R.string.illegal_operation));
                        }
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        tvFiveSectionArm.setBackgroundResource(R.drawable.grey_bg_circle);
                        if (canState!=1||canState!=3) {
                            stopRepeatingTask();
                        }
                        tvTips.setText("");
                        //showState();
                        return true;
                    }
                }
                return false;
            }
        });
        tvSixSectionArm.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (proximitySwitch == 1 && twoArmLength <= 0.3 && canState != 3) {
                            tvSixSectionArm.setBackgroundResource(R.drawable.blue_bg_circle);
                            buttomType = 6;
                            if (languageState == 2) {
                                tvTips.setText("В процессе переключение шестисекционной стрелы");
                            } else {
                                tvTips.setText(getString(R.string.switching_in_progress) + " " + getString(R.string.six_section_arm));
                            }
                            canState = 2;
                            startRepeatingTask();
                        } else {
                            ToastUtils.showShort(getString(R.string.illegal_operation));
                        }
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        tvSixSectionArm.setBackgroundResource(R.drawable.grey_bg_circle);
                        if (canState!=1||canState!=3) {
                            stopRepeatingTask();
                        }
                        tvTips.setText("");
                        //showState();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void showState() {
        if (listData.get(1).get("state").equals("1")) {
//            tvFiveSectionArm.setBackgroundResource(R.drawable.grey_bg_circle);
//            tvSixSectionArm.setBackgroundResource(R.drawable.blue_bg_circle);
            tvPushRodContraction.setText(getString(R.string.six_section_arm));
            tvPushRodContraction.setTextColor(getColor(R.color.white));
            AppData.sectionArm = 1;
        } else if (listData.get(2).get("state").equals("1")) {
//            tvFiveSectionArm.setBackgroundResource(R.drawable.blue_bg_circle);
//            tvSixSectionArm.setBackgroundResource(R.drawable.grey_bg_circle);
            tvPushRodContraction.setText(getString(R.string.five_section_arm));
            tvPushRodContraction.setTextColor(getColor(R.color.white));
            AppData.sectionArm = 0;
        } else {
            AppData.sectionArm = 2;
//            tvFiveSectionArm.setBackgroundResource(R.drawable.grey_bg_circle);
//            tvSixSectionArm.setBackgroundResource(R.drawable.grey_bg_circle);
            tvPushRodContraction.setText(getString(R.string.arm_joint_mode_error));
            tvPushRodContraction.setTextColor(getColor(R.color.red));
        }
    }

    private int languageState = 0;

    @Override
    public void initData() {
        languageState = MMKVUtils.getInstance().decodeInt("languageState");
        if (languageState != 0) {
            tvFiveSectionArm.setTextSize(12);
            tvSixSectionArm.setTextSize(12);
            tvPushRodContraction.setTextSize(10);
            tvTips.setTextSize(12);
            tv1.setTextSize(10);
            tv2.setTextSize(10);
            tv3.setTextSize(10);
        }
        for (int i = 0; i < strName.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", strName[i]);
            map.put("state", "0");
            listData.add(map);
        }
        gridView.setAdapter(adapter);
    }

    CommonAdapter adapter = new CommonAdapter<Map<String, Object>>(
            this, R.layout.input_output_status_item, listData) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            ImageView ivState = viewHolder.getView(R.id.iv_state);
            String name = item.get("name") + "";
            tvName.setText(name);

            if (languageState != 0) {
                tvName.setTextSize(9);
            }
            String state = item.get("state") + "";
            if (state.equals("1")) {
                ivState.setImageResource(R.drawable.grenn_round_bg);
            } else {
                ivState.setImageResource(R.drawable.white_round_bg);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return: {
                finish();
                overridePendingTransition(0, 0);
                break;
            }
        }
    }
}
