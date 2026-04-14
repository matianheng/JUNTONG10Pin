package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.sdk.kb.AutoKeyboardEditText;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.widget.HintDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VirtualWallActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llRreturn;
    private LinearLayout llVirtualWall;
    private Switch switchVirtualWall;
    private LinearLayout llVirtualWallShow;
    private TextView tvSave;
    private TextView tvHeightRealValue;
    private AutoKeyboardEditText etUpperLimitValue, etLowerLimitingValue, etUpperDecelerationZone, etLowerDecelerationZone;
    private TextView tvHeightZeroValueReset;
    private TextView tvAmplitudeRealValue;
    private AutoKeyboardEditText etMaximumValue, etMinimumValue, etMaximumDecelerationZone, etMinimumDecelerationZone;
    private TextView tvAmplitudeZeroValueReset;

    private byte[] sendData = {0x01, 0x02, 0x03, 0x04};
    private List<byte[]> mModifyList = new ArrayList<>();
    private List<byte[]> mSendList = new ArrayList<>();
    private int hint = -1;
    private HintDialog hintDialog;

    private TextView tv1,tv2,tv3,tv4,tv5,tv6,tv7,tv8,tv9,tv10,tv11,tv12;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.virtual_wall_layout);
        initView();
        initData();
        hintDialog = new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm && hint == 1) {
                    createLoadingDialog("");
                    canState = 2;
                    startRepeatingTask();
                } else if (isConfirm && hint == 2) {
                    createLoadingDialog("");
                    canState = 3;
                    startRepeatingTask();
                } else if (isConfirm && hint == 3) {
                    mSendList.clear();
                    createLoadingDialog("");
                    canState = 4;
                    index = 0;
                    for (int i = 0; i < sendData.length; i++) {
                        byte[] data = new byte[]{(byte) 0x23, 0x27, 0x20, sendData[i], (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                        if (i == 0) {
                            int s = (int) (Double.parseDouble(etUpperLimitValue.getText().toString()) * 10);
                            data[4] = (byte) (s & 0xff);
                            data[5] = (byte) ((s >> 8) & 0xff);

                            s = (int) (Double.parseDouble(etUpperDecelerationZone.getText().toString()) * 10);
                            data[6] = (byte) (s & 0xff);
                            data[7] = (byte) ((s >> 8) & 0xff);
                        } else if (i == 1) {
                            int s = (int) (Double.parseDouble(etLowerLimitingValue.getText().toString()) * 10);
                            data[4] = (byte) (s & 0xff);
                            data[5] = (byte) ((s >> 8) & 0xff);

                            s = (int) (Double.parseDouble(etLowerDecelerationZone.getText().toString()) * 10);
                            data[6] = (byte) (s & 0xff);
                            data[7] = (byte) ((s >> 8) & 0xff);
                        } else if (i == 2) {
                            int s = (int) (Double.parseDouble(etMaximumValue.getText().toString()) * 10);
                            data[4] = (byte) (s & 0xff);
                            data[5] = (byte) ((s >> 8) & 0xff);

                            s = (int) (Double.parseDouble(etMaximumDecelerationZone.getText().toString()) * 10);
                            data[6] = (byte) (s & 0xff);
                            data[7] = (byte) ((s >> 8) & 0xff);
                        } else if (i == 3) {
                            int s = (int) (Double.parseDouble(etMinimumValue.getText().toString()) * 10);
                            data[4] = (byte) (s & 0xff);
                            data[5] = (byte) ((s >> 8) & 0xff);

                            s = (int) (Double.parseDouble(etMinimumDecelerationZone.getText().toString()) * 10);
                            data[6] = (byte) (s & 0xff);
                            data[7] = (byte) ((s >> 8) & 0xff);
                        }
                        mSendList.add(data);
                    }
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
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600 + AppData.nodeID, mModifyList.get(index)));
            } else if (canState == 1) {

            } else if (canState == 2) {
                byte[] data = new byte[]{(byte) 0x2F, 0x26, 0x20, 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600 + AppData.nodeID, data));
            } else if (canState == 3) {
                byte[] data = new byte[]{(byte) 0x2F, 0x26, 0x20, 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600 + AppData.nodeID, data));
            } else if (canState == 4) {
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600 + AppData.nodeID, mSendList.get(index)));
            } else if (canState == 5) {
                byte[] data = new byte[]{(byte) 0x23, 0x10, 0x10, 0x01, (byte) 0x73, (byte) 0x61, (byte) 0x76, 0x65};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600 + AppData.nodeID, data));
            }
            // 重新安排任务以进行下一次执行
            mHandler.postDelayed(mTask, DELAY_TIME_MS);
        }
    };

    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        ThreadUtils.runOnUiThread(() -> {
            if ((canData[0]& 0xFF) == 0x80 && canData[1] == 0x25 && canData[2] == 0x20 && canData[3] == 0x01) {
                if (isVirtualWall) {
                    switchVirtualWall.setChecked(true);
                } else {
                    switchVirtualWall.setChecked(false);
                }
                stopRepeatingTask();
                dismissLoadingDialog();
                ToastUtils.showShort(R.string.fail);
                return;
            }
            if (canData[0] == 0x60 && canData[1] == 0x27 && canData[2] == 0x20 && canData[3] == sendData[index]) {
                int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                int codeSix = getInteger(canData[6]) + (getInteger(canData[7]) << 8);
                if (index == 0) {
                    etUpperLimitValue.setText((codeFour * 0.1) + "");
                    etUpperDecelerationZone.setText((codeSix * 0.1) + "");
                } else if (index == 1) {
                    etLowerLimitingValue.setText((codeFour * 0.1) + "");
                    etLowerDecelerationZone.setText((codeSix * 0.1) + "");
                } else if (index == 2) {
                    etMaximumValue.setText((codeFour * 0.1) + "");
                    etMaximumDecelerationZone.setText((codeSix * 0.1) + "");
                } else if (index == 3) {
                    etMinimumValue.setText((codeFour * 0.1) + "");
                    etMinimumDecelerationZone.setText((codeSix * 0.1) + "");
                    stopRepeatingTask();
                    dismissLoadingDialog();
                }
                if (index < 3) {
                    index += 1;
                }
            } else if (canData[0] == 0x60 && canData[1] == 0x25 && canData[2] == 0x20 && canData[3] == 0x01) {
                if (!isVirtualWall) {
                    canState = 0;
                    index = 0;
                } else {
                    stopRepeatingTask();
                    dismissLoadingDialog();
                }
                if (isVirtualWall) {
                    isVirtualWall = false;
                    llVirtualWallShow.setVisibility(View.GONE);
                    tvSave.setVisibility(View.GONE);
                    switchVirtualWall.setChecked(false);
                } else {
                    isVirtualWall = true;
                    llVirtualWallShow.setVisibility(View.VISIBLE);
                    tvSave.setVisibility(View.VISIBLE);
                    switchVirtualWall.setChecked(true);
                }
                MMKVUtils.getInstance().encode("isVirtualWall", isVirtualWall);
            } else if (canData[0] == 0x60 && canData[1] == 0x26 && canData[2] == 0x20 && canData[3] == 0x01) {
                canState = 0;
                index = 0;
                ToastUtils.showShort(R.string.success);
            } else if (canData[0] == 0x60 && canData[1] == 0x26 && canData[2] == 0x20 && canData[3] == 0x02) {
                canState = 0;
                index = 0;
                ToastUtils.showShort(R.string.success);
            } else if (canData[0] == 0x60 && canData[1] == 0x27 && canData[2] == 0x20 && canData[3] == 0x04) {
                canState = 5;
            } else if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x10 && canData[3] == 0x01) {
                stopRepeatingTask();
                dismissLoadingDialog();
                showDialog(-1, 2, getString(R.string.success_saved));
            }
        });
    }

    private byte[] canData_2A4;

    @Override
    public void getCanData_2A4(byte[] canData) {
        super.getCanData_2A4(canData);
        if (!Arrays.equals(canData, canData_2A4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_2A4 = canData;
                int codeZero = getInteger(canData[0]) + (getInteger(canData[1]) << 8);
                tvAmplitudeRealValue.setText(MyUtils.getOneDecimal(codeZero * 0.01) + "");

                int codeTwo = getInteger(canData[2]) + (getInteger(canData[3]) << 8);
                tvHeightRealValue.setText(MyUtils.getOneDecimal(codeTwo * 0.01) + "");
            });
        }
    }

    private void startRepeatingTask() {
        mHandler.post(mTask);
    }

    private void stopRepeatingTask() {
        if (mTask != null) {
            mHandler.removeCallbacks(mTask);
        }
    }

    private void configureEditText(EditText editText) {
        // 设置键盘类型为数字和小数点
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // 限制输入一位小数
        editText.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    String input = dest.toString() + source.toString();
                    if (input.contains(".")) {
                        String decimalPart = input.substring(input.indexOf(".") + 1);
                        if (decimalPart.length() > 1) {
                            return "";
                        }
                    }
                    return null;
                }
        });
        // 添加范围检查
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.toString().isEmpty()) return;

                try {
                    float value = Float.parseFloat(s.toString());
                    if (value < 0f) {
                        editText.setText(String.valueOf(0f));
                        editText.setSelection(editText.getText().length()); // 设置光标位置到末尾
                    } else if (value > 65535.5) {
                        editText.setText(String.valueOf(65535.5));
                        editText.setSelection(editText.getText().length()); // 设置光标位置到末尾
                    }
                } catch (NumberFormatException e) {
                    // 忽略非数字输入
                }
            }
        });
    }

    @Override
    public void initView() {
        llRreturn = findViewById(R.id.ll_return);
        llVirtualWall = findViewById(R.id.ll_virtual_wall);
        switchVirtualWall = findViewById(R.id.switch_virtual_wall);
        llVirtualWallShow = findViewById(R.id.ll_virtual_wall_show);
        tvSave = findViewById(R.id.tv_save);
        tvHeightRealValue = findViewById(R.id.tv_height_real_value);
        etUpperLimitValue = findViewById(R.id.et_upper_limit_value);
        etLowerLimitingValue = findViewById(R.id.et_lower_limiting_value);
        etUpperDecelerationZone = findViewById(R.id.et_upper_deceleration_zone);
        etLowerDecelerationZone = findViewById(R.id.et_lower_deceleration_zone);
        tvHeightZeroValueReset = findViewById(R.id.tv_height_zero_value_reset);
        tvAmplitudeRealValue = findViewById(R.id.tv_amplitude_real_value);
        etMaximumValue = findViewById(R.id.et_maximum_value);
        etMinimumValue = findViewById(R.id.et_minimum_value);
        etMaximumDecelerationZone = findViewById(R.id.et_maximum_deceleration_zone);
        etMinimumDecelerationZone = findViewById(R.id.et_minimum_deceleration_zone);
        tvAmplitudeZeroValueReset = findViewById(R.id.tv_amplitude_zero_value_reset);
        tv1=findViewById(R.id.tv1);
        tv2=findViewById(R.id.tv2);
        tv3=findViewById(R.id.tv3);
        tv4=findViewById(R.id.tv4);
        tv5=findViewById(R.id.tv5);
        tv6=findViewById(R.id.tv6);
        tv7=findViewById(R.id.tv7);
        tv8=findViewById(R.id.tv8);
        tv9=findViewById(R.id.tv9);
        tv10=findViewById(R.id.tv10);
        tv11=findViewById(R.id.tv11);
        tv12=findViewById(R.id.tv12);

        int languageState = MMKVUtils.getInstance().decodeInt("languageState");
        if (languageState!=0){
            tvSave.setTextSize(9);
            tvHeightRealValue.setTextSize(9);
            etUpperLimitValue.setTextSize(9);
            etLowerLimitingValue.setTextSize(9);
            etUpperDecelerationZone.setTextSize(9);
            etLowerDecelerationZone.setTextSize(9);
            tvHeightZeroValueReset.setTextSize(9);
            tvAmplitudeRealValue.setTextSize(9);
            etMaximumValue.setTextSize(9);
            etMinimumValue.setTextSize(9);
            etMaximumDecelerationZone.setTextSize(9);
            etMinimumDecelerationZone.setTextSize(9);
            tvAmplitudeZeroValueReset.setTextSize(9);
            tv1.setTextSize(9);
            tv2.setTextSize(9);
            tv3.setTextSize(9);
            tv4.setTextSize(9);
            tv5.setTextSize(9);
            tv6.setTextSize(9);
            tv7.setTextSize(9);
            tv8.setTextSize(9);
            tv9.setTextSize(9);
            tv10.setTextSize(9);
            tv11.setTextSize(9);
            tv12.setTextSize(9);
        }

        llRreturn.setOnClickListener(this);
        llVirtualWall.setOnClickListener(this);
        switchVirtualWall.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        tvHeightZeroValueReset.setOnClickListener(this);
        tvAmplitudeZeroValueReset.setOnClickListener(this);

        configureEditText(etUpperLimitValue);
        configureEditText(etLowerLimitingValue);
        configureEditText(etUpperDecelerationZone);
        configureEditText(etLowerDecelerationZone);
        configureEditText(etMaximumValue);
        configureEditText(etMinimumValue);
        configureEditText(etMaximumDecelerationZone);
        configureEditText(etMinimumDecelerationZone);
    }

    private boolean isVirtualWall = false;
    private int index = -1;

    @Override
    public void initData() {
        for (int i = 0; i < sendData.length; i++) {
            byte[] data = new byte[]{(byte) 0x43, 0x27, 0x20, sendData[i], (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            mModifyList.add(data);
        }
        isVirtualWall = MMKVUtils.getInstance().decodeBoolean("isVirtualWall");
        if (isVirtualWall) {
            createLoadingDialog("");
            llVirtualWallShow.setVisibility(View.VISIBLE);
            tvSave.setVisibility(View.VISIBLE);
            switchVirtualWall.setChecked(true);
            canState = 0;
            index = 0;
            startRepeatingTask();
        } else {
            llVirtualWallShow.setVisibility(View.GONE);
            tvSave.setVisibility(View.GONE);
            switchVirtualWall.setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return: {
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.ll_virtual_wall: {
            }
            case R.id.switch_virtual_wall: {
                createLoadingDialog("");
                canState = -1;
                byte[] data = new byte[]{(byte) 0x2F, 0x25, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                if (!isVirtualWall) {
                    data[4] = 0x01;
                }
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600 + AppData.nodeID, data));
                startRepeatingTask();
                break;
            }
            case R.id.tv_save: {
                String upperLimitValue = etUpperLimitValue.getText().toString();
                if (upperLimitValue.equals("")) {
                    ToastUtils.showShort(getString(R.string.enter_set, getString(R.string.upper_limit_value)));
                    return;
                }
                String lowerLimitingValue = etLowerLimitingValue.getText().toString();
                if (lowerLimitingValue.equals("")) {
                    ToastUtils.showShort(getString(R.string.enter_set, getString(R.string.lower_limiting_value)));
                    return;
                }
                String upperDecelerationZone = etUpperDecelerationZone.getText().toString();
                if (upperDecelerationZone.equals("")) {
                    ToastUtils.showShort(getString(R.string.enter_set, getString(R.string.upper_deceleration_zone)));
                    return;
                }
                String lowerDecelerationZone = etLowerDecelerationZone.getText().toString();
                if (lowerDecelerationZone.equals("")) {
                    ToastUtils.showShort(getString(R.string.enter_set, getString(R.string.lower_deceleration_zone)));
                    return;
                }
                String maximumValue = etMaximumValue.getText().toString();
                if (maximumValue.equals("")) {
                    ToastUtils.showShort(getString(R.string.enter_set, getString(R.string.maximum_value)));
                    return;
                }
                String minimumValue = etMinimumValue.getText().toString();
                if (minimumValue.equals("")) {
                    ToastUtils.showShort(getString(R.string.enter_set, getString(R.string.minimum_value)));
                    return;
                }
                String maximumDecelerationZone = etMaximumDecelerationZone.getText().toString();
                if (maximumDecelerationZone.equals("")) {
                    ToastUtils.showShort(getString(R.string.enter_set, getString(R.string.maximum_deceleration_zone)));
                    return;
                }
                String minimumDecelerationZone = etMinimumDecelerationZone.getText().toString();
                if (minimumDecelerationZone.equals("")) {
                    ToastUtils.showShort(getString(R.string.enter_set, getString(R.string.minimum_deceleration_zone)));
                    return;
                }
                if (Double.parseDouble(upperLimitValue) < Double.parseDouble(lowerLimitingValue)) {
                    ToastUtils.showShort(getString(R.string.the_upper_limit_of_height_should_be_greater_than_the_lower_limit_of_height));
                    return;
                }
                if (Double.parseDouble(maximumValue) < Double.parseDouble(minimumValue)) {
                    ToastUtils.showShort(getString(R.string.the_maximum_amplitude_value_should_be_greater_than_the_minimum_amplitude_value));
                    return;
                }
                showDialog(3, 3, getString(R.string.save_or_not));
                break;
            }
            case R.id.tv_height_zero_value_reset: {
                showDialog(1, 3, getString(R.string.is_the_height_of_the_virtual_wall_reset_to_zero));
                break;
            }
            case R.id.tv_amplitude_zero_value_reset: {
                showDialog(2, 3, getString(R.string.is_the_amplitude_of_the_virtual_wall_reset_to_zero));
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
