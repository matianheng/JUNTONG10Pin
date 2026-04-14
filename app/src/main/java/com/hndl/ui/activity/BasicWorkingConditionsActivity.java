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
import com.hndl.ui.widget.HintDialog;
import com.hndl.ui.widget.SelecteListDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicWorkingConditionsActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout llReturn;
    private LinearLayout llWorkArm;
    private TextView tvWorkArm;
    private AutoKeyboardEditText etMagnification, etMinAngle, etMaxAngle;
    private LinearLayout llLeg;
    private TextView tvLeg;
    private LinearLayout llFifthLeg;
    private TextView tvFifthLeg;
    private LinearLayout llJibLength;
    private TextView tvJibLength;
    private LinearLayout llJibAngle;
    private TextView tvJibAngle;
    private TextView btnSave;

    private int identifying = -1;
    SelecteListDialog sld;
    private List<Map<String, Object>> listWorkArm = new ArrayList<>();
    private String[] strWorkArm = {AppData.getInstance().getString(R.string.main_arm), AppData.getInstance().getString(R.string.arm_end_pulley), AppData.getInstance().getString(R.string.jib)};
    private List<Map<String, Object>> listLeg = new ArrayList<>();
    private String[] strLeg = {AppData.getInstance().getString(R.string.full_extension), AppData.getInstance().getString(R.string.half_extension), AppData.getInstance().getString(R.string.unextended)};
    private List<Map<String, Object>> listFifthLeg = new ArrayList<>();
    private String[] strFifthLeg = {AppData.getInstance().getString(R.string.full_extension), AppData.getInstance().getString(R.string.unextended)};
    private List<Map<String, Object>> listJibLength = new ArrayList<>();
    private String[] strJibLength = {"6 m", "8 m", "10 m"};
    private List<Map<String, Object>> listJibAngle = new ArrayList<>();
    private String[] strJibAngle = {"0 °", "15 °", "30 °"};
    private int WorkArea = 0;

    private int hint = -1;
    private HintDialog hintDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_working_conditions_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
        startRepeatingTask();
        hintDialog = new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm && hint == 1) {
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
            if (canState == 0) {
                byte[] data = new byte[]{(byte) 0x43, 0x10, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            } else if (canState == 1) {
                byte[] data = new byte[]{(byte) 0x23, 0x10, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                int WorkArm = Integer.parseInt(tvWorkArm.getTag() + "");
                int Magnification = Integer.parseInt(etMagnification.getText() + "");
//                int four = ((WorkArm & 0x01) & 0xff) + ((((WorkArm >> 1) & 0x01) << 1) & 0xff) + (((Magnification & 0x01) << 2) & 0xff)
//                        + ((((Magnification >> 1) & 0x01) << 3) & 0xff) + ((((Magnification >> 2) & 0x01) << 4) & 0xff) + ((((Magnification >> 3) & 0x01) << 5) & 0xff)
//                        + ((((Magnification >> 4) & 0x01) << 6) & 0xff) + ((((Magnification >> 5) & 0x01) << 7) & 0xff);
                int four = WorkArm | (Magnification << 3);
                data[4] = (byte) four;

                int leg = Integer.parseInt(tvLeg.getTag() + "");
                int FifthLeg = Integer.parseInt(tvFifthLeg.getTag() + "");
                int JibLength = Integer.parseInt(tvJibLength.getTag() + "");
                int JibAngle = Integer.parseInt(tvJibAngle.getTag() + "");
                int five = ((leg & 0x01) & 0xff) + ((((leg >> 1) & 0x01) << 1) & 0xff) + (((FifthLeg & 0x01) << 2) & 0xff) + ((((FifthLeg >> 1) & 0x01) << 3) & 0xff)
                        + (((WorkArea & 0x01) << 4) & 0xff) + (((JibLength & 0x01) << 5) & 0xff) + (((JibAngle & 0x01) << 6) & 0xff)
                        + ((((JibAngle >> 1) & 0x01) << 7) & 0xff);
                data[5] = (byte) five;
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            } else if (canState == 2) {
                byte[] data = new byte[]{(byte) 0x23, 0x10, 0x10, 0x01, (byte) 0x73, (byte) 0x61, (byte) 0x76, 0x65};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            }else if (canState == 3) {
                // 读取最大角度、最小角度，add by chenyaoli，2023.12.28
                byte[] data = new byte[]{(byte) 0x43, 0x21, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            }else if (canState == 4) {
                // 修改最大角度、最小角度，add by chenyaoli，2023.12.28
                byte[] data = new byte[]{(byte) 0x23, 0x21, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                int minAngle = Integer.parseInt(etMinAngle.getText() + "");
                int maxAngle = Integer.parseInt(etMaxAngle.getText() + "");
                data[4] = (byte)maxAngle;
                data[5] = (byte)minAngle;
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

    private byte[] canData_584;

    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                if (canData[0] == 0x80 && canData[1] == 0x10 && canData[2] == 0x20 && canData[3] == 0x01) {
                    return;
                } else if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x10 && canData[3] == 0x01) {
                    stopRepeatingTask();
                    dismissLoadingDialog();
                    showDialog(-1, 2, getString(R.string.success_saved));
                } else if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x20 && canData[3] == 0x01){
                    // 如果是读取工况的数据回复
                    if (canState == 0) {
                        int codeFour = getInteger(canData[4]);
//                        int workArm = codeFour & 0x01 + (((codeFour >> 1) & 0x01) << 1);  //工作臂
                        int workArm = codeFour & 0x07;
                        if (workArm == 0) {
                            tvWorkArm.setText(listWorkArm.get(0).get("KEY") + "");
                            tvWorkArm.setTag(listWorkArm.get(0).get("VLAUE") + "");
                        } else if (workArm == 1) {
                            tvWorkArm.setText(listWorkArm.get(1).get("KEY") + "");
                            tvWorkArm.setTag(listWorkArm.get(1).get("VLAUE") + "");
                        } else if (workArm == 2) {
                            tvWorkArm.setText(listWorkArm.get(2).get("KEY") + "");
                            tvWorkArm.setTag(listWorkArm.get(2).get("VLAUE") + "");
                        }
//                        int magnification = (codeFour >> 2) & 0x01 + (((codeFour >> 3) & 0x01) << 1) + (((codeFour >> 4) & 0x01) << 2) + (((codeFour >> 5) & 0x01) << 3)
//                                + (((codeFour >> 6) & 0x01) << 4) + (((codeFour >> 7) & 0x01) << 5);    //倍率
                        // 倍率 取 byte4 的3~7位
                        int magnification = codeFour >> 3 & 0x1F;
                        if (workArm == 0) {
                            etMagnification.setFocusable(true);
                            etMagnification.setFocusableInTouchMode(true);
                            etMagnification.setText(magnification + "");
                        } else {
                            etMagnification.setFocusable(false);
                            etMagnification.setFocusableInTouchMode(false);
                            etMagnification.setText("1");
                        }
                        int codeFive = getInteger(canData[5]);
                        int legState = codeFive & 0x01 + (((codeFive >> 1) & 0x01) << 1);  //支腿状态
                        if (legState == 0) {
                            tvLeg.setText(getString(R.string.full_extension));
                            tvLeg.setTag(0);
                        } else if (legState == 1) {
                            tvLeg.setText(getString(R.string.half_extension));
                            tvLeg.setTag(1);
                        } else {
                            tvLeg.setText(getString(R.string.unextended));
                            tvLeg.setTag(2);
                        }
                        int fifthLegState = (codeFive >> 2) & 0x01 + (((codeFive >> 3) & 0x01) << 1);  //第五支腿状态
                        if (fifthLegState == 0) {
                            tvFifthLeg.setText(getString(R.string.full_extension));
                            tvFifthLeg.setTag(0);
                        } else {
                            tvFifthLeg.setText(getString(R.string.unextended));
                            tvFifthLeg.setTag(2);
                        }
                        WorkArea = (codeFive >> 4) & 0x01;
                        int armLength = (codeFive >> 5) & 0x01;
                        if (armLength == 0) {
                            tvJibLength.setText("6 m");
                            tvJibLength.setTag(0);
                        } else if (armLength == 1) {
                            tvJibLength.setText("8 m");
                            tvJibLength.setTag(1);
                        } else {
                            tvJibLength.setText("10 m");
                            tvJibLength.setTag(2);
                        }
                        int armAngle = (codeFive >> 6) & 0x01 + (((codeFive >> 7) & 0x01) << 1);
                        if (armAngle == 0) {
                            tvJibAngle.setText("0 °");
                            tvJibAngle.setTag(0);
                        } else if (armAngle == 1) {
                            tvJibAngle.setText("15 °");
                            tvJibAngle.setTag(1);
                        } else {
                            tvJibAngle.setText("30 °");
                            tvJibAngle.setTag(2);
                        }
                        canState=3;
                    }else if (canState==1){
                        canState=4;
                    }
                }else if (canData[0] == 0x60 && canData[1] == 0x21 && canData[2] == 0x20 && canData[3] == 0x01){

                    // 如果是读取范围限制的数据回复
                    if (canState == 3) {
                        stopRepeatingTask();

                        int maxAngle = canData[4];
                        int minAngle = canData[5];

                        etMinAngle.setText(minAngle + "");
                        etMaxAngle.setText(maxAngle + "");
                    }else if (canState==4){
                        canState=2;
                    }
                }
            });
        }
    }

    @Override
    public void initView() {
        for (int i = 0; i < strWorkArm.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("KEY", strWorkArm[i] + "");
            map.put("VLAUE", i);
            listWorkArm.add(map);
        }
        for (int i = 0; i < strLeg.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("KEY", strLeg[i] + "");
            map.put("VLAUE", i);
            listLeg.add(map);
        }
        for (int i = 0; i < strFifthLeg.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("KEY", strFifthLeg[i] + "");
            map.put("VLAUE", i);
            listFifthLeg.add(map);
        }
        for (int i = 0; i < strJibLength.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("KEY", strJibLength[i] + "");
            map.put("VLAUE", i);
            listJibLength.add(map);
        }
        for (int i = 0; i < strJibAngle.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("KEY", strJibAngle[i] + "");
            map.put("VLAUE", i);
            listJibAngle.add(map);
        }
        llReturn = findViewById(R.id.ll_return);
        llWorkArm = findViewById(R.id.ll_work_arm);
        tvWorkArm = findViewById(R.id.tv_work_arm);
        etMagnification = findViewById(R.id.et_magnification);
        llLeg = findViewById(R.id.ll_leg);
        tvLeg = findViewById(R.id.tv_leg);
        llFifthLeg = findViewById(R.id.ll_fifth_leg);
        tvFifthLeg = findViewById(R.id.tv_fifth_leg);
        llJibLength = findViewById(R.id.ll_jib_length);
        tvJibLength = findViewById(R.id.tv_jib_length);
        llJibAngle = findViewById(R.id.ll_jib_angle);
        tvJibAngle = findViewById(R.id.tv_jib_angle);
        etMinAngle = findViewById(R.id.et_min_angle);
        etMaxAngle = findViewById(R.id.et_max_angle);
        btnSave = findViewById(R.id.btn_save);

        llReturn.setOnClickListener(this);
        llWorkArm.setOnClickListener(this);
        llLeg.setOnClickListener(this);
        llFifthLeg.setOnClickListener(this);
        llJibLength.setOnClickListener(this);
        llJibAngle.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    @Override
    public void initData() {
        tvWorkArm.setText(listWorkArm.get(0).get("KEY") + "");
        tvWorkArm.setTag(listWorkArm.get(0).get("VLAUE") + "");
        etMagnification.setText("1");
        tvLeg.setText(listLeg.get(0).get("KEY") + "");
        tvLeg.setTag(listLeg.get(0).get("VLAUE") + "");
        tvFifthLeg.setText(listFifthLeg.get(0).get("KEY") + "");
        tvFifthLeg.setTag(listFifthLeg.get(0).get("VLAUE") + "");
        tvJibLength.setText(listJibLength.get(0).get("KEY") + "");
        tvJibLength.setTag(listJibLength.get(0).get("VLAUE") + "");
        tvJibAngle.setText(listJibAngle.get(0).get("KEY") + "");
        tvJibAngle.setTag(listJibAngle.get(0).get("VLAUE") + "");
        etMinAngle.setText("0");
        etMaxAngle.setText("0");
    }

    private void selecteListDialog(final List<Map<String, Object>> lists,
                                   final TextView tv, String title) {
        sld = new SelecteListDialog(this, lists, title,
                new SelecteListDialog.SelecteListListener() {
                    @Override
                    public void onClick(int index) {
                        tv.setText(lists.get(index).get("KEY") + "");
                        tv.setTag(lists.get(index).get("VLAUE") + "");
                        if (identifying == 0) {
                            if (tv.getTag().toString().equals("0")) {
                                etMagnification.setFocusable(true);
                                etMagnification.setFocusableInTouchMode(true);
                            } else {
                                etMagnification.setFocusable(false);
                                etMagnification.setFocusableInTouchMode(false);
                                etMagnification.setText("1");
                            }
                        }
                    }
                });
        sld.show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
        mHandler = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_return: {
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.ll_work_arm: {
                identifying = 0;
                selecteListDialog(listWorkArm, tvWorkArm, getString(R.string.please_select));
                break;
            }
            case R.id.ll_leg: {
                identifying = 1;
                selecteListDialog(listLeg, tvLeg, getString(R.string.please_select));
                break;
            }
            case R.id.ll_fifth_leg: {
                identifying = 2;
                selecteListDialog(listFifthLeg, tvFifthLeg, getString(R.string.please_select));
                break;
            }
            case R.id.ll_jib_length: {
                identifying = 3;
                selecteListDialog(listJibLength, tvJibLength, getString(R.string.please_select));
                break;
            }
            case R.id.ll_jib_angle: {
                identifying = 4;
                selecteListDialog(listJibAngle, tvJibAngle, getString(R.string.please_select));
                break;
            }
            case R.id.btn_save: {
                String strMagnification = etMagnification.getText().toString();
                if (strMagnification.equals("") || Integer.parseInt(strMagnification) < 1 || Integer.parseInt(strMagnification) > 32) {
                    ToastUtils.showShort(getString(R.string.please_enter_the_magnification));
                    return;
                }

                String strMinAngle = etMinAngle.getText().toString();
                if (strMinAngle.equals("") || Integer.parseInt(strMinAngle) < -10 || Integer.parseInt(strMinAngle) > 80) {
                    ToastUtils.showShort(getString(R.string.min_angle_hintinfo));
                    return;
                }

                String strMaxAngle = etMaxAngle.getText().toString();
                if (strMaxAngle.equals("") || Integer.parseInt(strMaxAngle) < -10 || Integer.parseInt(strMaxAngle) > 80) {
                    ToastUtils.showShort(getString(R.string.max_angle_hintinfo));
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
}
