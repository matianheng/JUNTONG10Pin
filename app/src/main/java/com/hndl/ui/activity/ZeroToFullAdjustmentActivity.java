package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.widget.HintDialog;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZeroToFullAdjustmentActivity extends BaseActivity {
    private LinearLayout llReturn;
    private ListView listView;
    private TextView tvTitle;

    private String[] strName = {"L1", "L2", "A"};
    private List<Map<String, Object>> listData = new ArrayList<>();

    private int hint = -1;
    private HintDialog hintDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zero_to_full_adjustment_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
        hintDialog = new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm) {
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
            if (canState==1){
                if (hint == 1) {
                    byte[] data = new byte[]{(byte) 0x2F, 0x16, 0x20, 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, 0x00};
                    if (listData.get(index).get("name").equals("L1")) {
                        data[3] = (byte) 0x01;
                    } else if (listData.get(index).get("name").equals("L2")) {
                        data[3] = (byte) 0x02;
                    } else if (listData.get(index).get("name").equals("A")) {
                        data[3] = (byte) 0x03;
                    }
                    canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+ AppData.nodeID, data));
                } else if (hint == 2) {
                    byte[] data = new byte[]{(byte) 0x2F, 0x17, 0x20, 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, 0x00};
                    if (listData.get(index).get("name").equals("L1")) {
                        data[3] = (byte) 0x01;
                    } else if (listData.get(index).get("name").equals("L2")) {
                        data[3] = (byte) 0x02;
                    } else if (listData.get(index).get("name").equals("A")) {
                        data[3] = (byte) 0x03;
                    }
                    canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
                }
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

    int languageState=0;
    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        listView = findViewById(R.id.listView);
        tvTitle=findViewById(R.id.tv_title);

        llReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        languageState = MMKVUtils.getInstance().decodeInt("languageState");
        if (languageState!=0){
            tvTitle.setTextSize(16);
        }
    }

    @Override
    public void initData() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "");
        map.put("ad", getString(R.string.ad_name));
        map.put("actual", getString(R.string.actual));
        listData.add(map);

        for (int i = 0; i < strName.length; i++) {
            map = new HashMap<>();
            map.put("name", strName[i]);
            map.put("ad", "");
            map.put("actual", "");
            listData.add(map);
        }

        listView.setAdapter(adapter);
    }

    private byte[] canData_194;
    private byte[] canData_1A4;
    CommonAdapter adapter = new CommonAdapter<Map<String, Object>>(
            this, R.layout.zero_to_full_adjustment_item, listData) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            TextView tvAd = viewHolder.getView(R.id.tv_ad);
            TextView tvActual = viewHolder.getView(R.id.tv_actual);
            LinearLayout llZeroAdjustment = viewHolder.getView(R.id.ll_zero_adjustment);
            LinearLayout llFillUp = viewHolder.getView(R.id.ll_fill_up);
            TextView tvZeroAdjustment = viewHolder.getView(R.id.tv_zero_adjustment);
            TextView tvFillUp = viewHolder.getView(R.id.tv_fill_up);

            String name = item.get("name") + "";
            tvName.setText(name);

            String ad = item.get("ad") + "";
            tvAd.setText(ad);

            String actual = item.get("actual") + "";
            tvActual.setText(actual);

            if (position == 0) {
                llZeroAdjustment.setVisibility(View.INVISIBLE);
                llFillUp.setVisibility(View.INVISIBLE);
            } else {
                llZeroAdjustment.setVisibility(View.VISIBLE);
                llFillUp.setVisibility(View.VISIBLE);
            }
            llZeroAdjustment.setTag(position);
            llFillUp.setTag(position);
            llZeroAdjustment.setOnClickListener(new onZeroAdjustment());
            llFillUp.setOnClickListener(new onFillUp());

            if (languageState!=0){
                tvZeroAdjustment.setTextSize(11);
                tvFillUp.setTextSize(7);
            }
        }
    };

    int index = -1;

    class onZeroAdjustment implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            index = (int) view.getTag();
            showDialog(1, 3, listData.get(index).get("name") + getString(R.string.whether_or_not_zero_adjustment));
        }
    }

    class onFillUp implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            index = (int) view.getTag();
            showDialog(2, 3, listData.get(index).get("name") + getString(R.string.whether_or_not_fill_up));
        }
    }

    @Override
    public void getCanData_194(byte[] canData) {
        super.getCanData_194(canData);
        if (!Arrays.equals(canData, canData_194)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_194 = canData;
                int codeZero = getInteger(canData[0]) + (getInteger(canData[1]) << 8);
                int codeTwo = MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                listData.get(1).put("ad", codeZero + "");
                listData.get(1).put("actual", MyUtils.getOneDecimal(codeTwo * 0.01) + " m");

                int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                int codeSix =  MyUtils.get16Data((byte) canData[6], (byte) canData[7]);
                listData.get(2).put("ad", codeFour + "");
                listData.get(2).put("actual", MyUtils.getOneDecimal(codeSix * 0.01) + " m");
                adapter.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void getCanData_1A4(byte[] canData) {
        super.getCanData_1A4(canData);
        if (!Arrays.equals(canData, canData_1A4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_1A4 = canData;
                int codeZero = getInteger(canData[0]) + (getInteger(canData[1]) << 8);
                int codeTwo =  MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                listData.get(3).put("ad", codeZero + "");
                listData.get(3).put("actual", MyUtils.getOneDecimal(codeTwo * 0.01) + " °");
                adapter.notifyDataSetChanged();
            });
        }
    }

    private byte[] canData_584;

    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                //LogUtil.e("XJW", ConvertUtils.bytes2HexString(canData));
                if (canData[0] == 0x60 && canData[1] == 0x16 && canData[2] == 0x20) {
                    canState = 2;
                }else if (canData[0] == 0x60 && canData[1] == 0x17 && canData[2] == 0x20) {
                    canState = 2;
                }else if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x10 && canData[3] == 0x01) {
                    stopRepeatingTask();
                    dismissLoadingDialog();
                    ToastUtils.showShort(getString(R.string.success_saved));
                }
            });
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
