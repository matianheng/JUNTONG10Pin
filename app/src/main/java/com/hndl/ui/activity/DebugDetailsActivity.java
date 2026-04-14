package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.LogUtil;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.widget.HintDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DebugDetailsActivity extends BaseActivity {

    private final String INTEND_KEY_TYPE = "TYPE";
    private final String KEY_STATE = "state";


    private final String KEY_NAME1 = "name1";
    private final String KEY_VALUE1 = "value1";
    private final String KEY_NAME2 = "name2";
    private final String KEY_VALUE2 = "value2";

    private LinearLayout llReturn;
    private ListView listView;
    private TextView tvCurrentNumber;
    private Button btnCollect, btnCalculate;

    private List<Map<String,Object>> listData = new ArrayList<>();
    // 0:空钩   1:砝码1     2：砝码2
    private int TYPE = 0;
    private int currentIndex = -1;

    private int hint = -1;
    private HintDialog hintDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_details_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();

        TYPE = Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra(INTEND_KEY_TYPE)));

        initView();
        initData();
        hintDialog = new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm) {
                    Intent intent = new Intent();
//                    intent.putExtra("data","data");
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hintDialog != null) {
            hintDialog.cancel();
            hintDialog = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSendCmdRequestCollectData();
        sendCmdStopRequestWeightData();
    }

    @Override
    public void initView() {
        llReturn = findViewById(R.id.ll_return);
        listView = findViewById(R.id.listView);
        tvCurrentNumber = findViewById(R.id.tv_current_num);
        btnCollect = findViewById(R.id.btn_collect);
        btnCalculate = findViewById(R.id.btn_calculate);

        llReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btnCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnCollect.setEnabled(false);
                sendCmdRequestCollectData(currentIndex+1);
            }
        });

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCmdRequestCalculateWeight();
            }
        });
    }

    private void updateCurrentIndex(int group){
        // 当已经采集完5个数据后，需提示用户数据采集完成
        if ((group >= 6)){
            btnCollect.setEnabled(false);
            btnCalculate.setEnabled(true);
            return;
        }
        if ((currentIndex >= 0) && (currentIndex <= 4)) {
            // 若序号有效，则更新当前索引对应的item
            listData.get(currentIndex).put(KEY_STATE, 0);
        }

        // 根据当前请求的组序号，更新相关UI（序号提示文本、 listview中Item的标识符号）
        currentIndex = group-1;
        //LogUtils.e("------------------test----------------currentIndex"+currentIndex);
        if ((currentIndex >= 0) && (currentIndex <= 4)){

            tvCurrentNumber.setText(String.valueOf(currentIndex+1));
            listData.get(currentIndex).put(KEY_STATE, 1);

            adapter.notifyDataSetChanged();

            btnCollect.setEnabled(true);
            btnCalculate.setEnabled(false);
        }
    }

    @Override
    public void initData() {
        // 初始化 5个数据点
        Map<String,Object> map;
        for (int i=0; i < 5; i++){
            map = new HashMap<>();
            int state = (i == 0) ? 1 : 0;
            map.put(KEY_STATE, state);

            String name1 =  (TYPE == 0) ? ("L"+(i+1)) : ("A"+(i+1));
            map.put(KEY_NAME1,name1);
            map.put(KEY_VALUE1,"");

            String name2 =  (TYPE == 0) ? ("DW"+(i+1)) : ("KW"+(i+1));;
            map.put(KEY_NAME2,name2);
            map.put(KEY_VALUE2,"");
            listData.add(map);
        }

        listView.setAdapter(adapter);

        // 进入页面时，发送一次请求重量调试所需数据
        updateCurrentIndex(1);
        sendCmdStartRequestWeightData();
    }

    private void showDialog(String content) {
        hintDialog.setButtonVisibility(2);
        hintDialog.setContent(content);
        hintDialog.setCancelable(false);
        hintDialog.show();
    }

    CommonAdapter<Map<String, Object>> adapter = new CommonAdapter<Map<String, Object>>(
            this, R.layout.debug_details_item, listData) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            ImageView ivState = viewHolder.getView(R.id.iv_state);
            TextView tvName1 = viewHolder.getView(R.id.tv_name1);
            TextView tvValue1 = viewHolder.getView(R.id.tv_value1);
            TextView tvName2 = viewHolder.getView(R.id.tv_name2);
            TextView tvValue2 = viewHolder.getView(R.id.tv_value2);

            int state = 0;
            String name1 = "";
            String value1 = "";
            String name2 = "";
            String value2 = "";
            try {
                name1 = item.get(KEY_NAME1) + "";
                value1 = item.get(KEY_VALUE1) + "";
                name2 = item.get(KEY_NAME2) + "";
                value2 = item.get(KEY_VALUE2) + "";

                state = (int) item.get(KEY_STATE);
            }catch (Exception e){
                state = 0;
                value1 = "";
                value2 = "";
            }

            // 根据数据，更新listview中的item状态
            if (state == 0){
                ivState.setBackground(null);
            }else{
                ivState.setBackgroundResource(R.drawable.debug_details_current);
            }

            tvName1.setText(name1);
            tvValue1.setText(value1);
            tvName2.setText(name2);
            tvValue2.setText(value2);
        }
    };

    private void sendCmdStartRequestWeightData(){
        byte[] data = new byte[]{(byte) 0x2F, 0x12, 0x10, 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, 0x00};
        //  TYPE 0:空钩   1:砝码1   2：砝码2   |||   0x01-空钩调试   0x02-砝码1调试    0x03-砝码2调试
        data[3] = (byte)(0x01 + TYPE);
        canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+ AppData.nodeID, data));
    }

    private void sendCmdStopRequestWeightData(){
        byte[] data = new byte[]{(byte) 0x2F, 0x12, 0x10, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
        //  TYPE 0:空钩   1:砝码1   2：砝码2   |||   0x01-空钩调试   0x02-砝码1调试    0x03-砝码2调试
        data[3] = (byte)(0x01 + TYPE);
        canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
    }

    private Handler mHandler = new Handler();
    private Runnable mTask = null;
    private void sendCmdRequestCollectData(int group){
        if (mTask != null){
            mHandler.removeCallbacks(mTask);
            mTask = null;
        }

        mTask = new Runnable() {
            @Override
            public void run() {
                byte[] data = new byte[]{(byte) 0x2F, 0x13, 0x10, 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, 0x00};
                //  TYPE 0:空钩   1:砝码1   2：砝码2   |||   0x01-空钩调试   0x02-砝码1调试    0x03-砝码2调试
                data[3] = (byte)(0x01 + TYPE);
                data[4] = (byte)group;
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));

                mHandler.postDelayed(mTask, 300);
            }
        };
        mHandler.post(mTask);
    }

    private void stopSendCmdRequestCollectData(){
        if (mTask != null){
            mHandler.removeCallbacks(mTask);
        }
    }

    private void sendCmdRequestCalculateWeight(){
        byte[] data = new byte[]{(byte) 0x2F, 0x14, 0x10, 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, 0x00};
        //  TYPE 0:空钩   1:砝码1   2：砝码2   |||   0x01-空钩调试   0x02-砝码1调试    0x03-砝码2调试
        data[3] = (byte)(0x01 + TYPE);
        canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
    }

    private byte[] canData_584;
    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
//        LogUtil.d("===============DebugDetails-------test---------","recvData:"+ MyUtils.byteArrayToHexStr(canData));
        // 回复数据会重复，不能屏蔽重复数据
//        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;

                int subIndex = canData[3];
                if (subIndex != (0x01 + TYPE)){
                    return;
                }
                if ((canData[0] == 0x60) && (canData[1] == 0x12) && (canData[2] == 0x10)){
                    // 请求重量调试所需数据的 回复数据

                }else if ((canData[0] == 0x60) && (canData[1] == 0x13) && (canData[2] == 0x10)){
                    // 请求采集一组数据的 回复数据
                    // 当采集完一组数据后，需更新索引相关UI
                    // canData[4] 代表 第 group 组数据采集完毕
                    int group = canData[4];
                    //LogUtils.e("------------------test----------------"+group);
                    stopSendCmdRequestCollectData();
                    btnCollect.setEnabled(true);
                    updateCurrentIndex(group+1);

                }else if ((canData[0] == 0x60) && (canData[1] == 0x14) && (canData[2] == 0x10)){
                    // 请求计算调重参数的 回复数据
                    // 计算完成后，停止请求采集
                    sendCmdStopRequestWeightData();

                    // 提示用户计算完成
                    showDialog(getString(R.string.debug_details_hint_calc));
                }
            });
//        }
    }

    private byte[] canData_1E4;
    @Override
    public void getCanData_1E4(byte[] canData) {
        super.getCanData_1E4(canData);
        // 回复数据会重复，不能屏蔽重复数据
//        if (!Arrays.equals(canData, canData_1E4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_1E4 = canData;
                if (TYPE == 0){ // 空钩调试
                    int codeZero =  MyUtils.get16Data((byte) canData[0], (byte) canData[1]);
                    listData.get(currentIndex).put(KEY_VALUE1, MyUtils.getOneDecimal(codeZero*0.01)+"");

                    int codeTwo =  MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                    listData.get(currentIndex).put(KEY_VALUE2, MyUtils.getOneDecimal(codeTwo*0.01)+"");
                }else{
                    // 砝码调试
                    int codeFour =  MyUtils.get16Data((byte) canData[4], (byte) canData[5]);
                    listData.get(currentIndex).put(KEY_VALUE1, MyUtils.getOneDecimal(codeFour*0.01)+"");

                    int codeSix =  MyUtils.get16Data((byte) canData[6], (byte) canData[7]);
                    listData.get(currentIndex).put(KEY_VALUE2, MyUtils.getOneDecimal(codeSix*0.01)+"");
                }

                adapter.notifyDataSetChanged();
            });
//        }
    }

}
