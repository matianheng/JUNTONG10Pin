package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.MyUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParametersActivity extends BaseActivity {

    private LinearLayout llReturn;
    private TextView tvTitle;
    private ListView listView1;
    private ListView listView2;

    private String[] strName={"K1","K2","K3","K4","K5"};
    private List<Map<String,Object>> listK=new ArrayList<>();

    private String[] strName1={AppData.getInstance().getString(R.string.debugging_weight),AppData.getInstance().getString(R.string.maximum_arm_length),
            AppData.getInstance().getString(R.string.minimum_arm_length),"",""};
    private String[] strName2={AppData.getInstance().getString(R.string.debugging_weight),AppData.getInstance().getString(R.string.maximum_angle),
            AppData.getInstance().getString(R.string.minimum_angle),"",""};
    private List<Map<String,Object>> listData=new ArrayList<>();

    private String TYPE="";   //0:空钩 1:砝码1  2：砝码2

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parameters_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();

        TYPE = getIntent().getStringExtra("TYPE");
        initView();
        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();

        sendCmdStartReadWeightParams();
    }

    @Override
    protected void onStop() {
        super.onStop();

        sendCmdStopReadWeightParams();
    }

    /**
     * onStart 时，发送开始读取调重参数 cmd, add by chenyaoli,2023.12.27
     */
    private void sendCmdStartReadWeightParams(){
        for (int i=0;i<3;i++){
            byte[] data = new byte[]{(byte) 0x2F, 0x20, 0x20, 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, 0x00};
            if (TYPE.equals("1")){
                data[3] = 0x02;
            }else if (TYPE.equals("2")){
                data[3] = 0x03;
            }
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
        }
    }

    /**
     * onSop 时，发送停止读取调重参数 cmd, add by chenyaoli,2023.12.27
     */
    private void sendCmdStopReadWeightParams(){
        for (int i=0;i<3;i++){
            byte[] data = new byte[]{(byte) 0x2F, 0x20, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            if (TYPE.equals("1")){
                data[3] = 0x02;
            }else if (TYPE.equals("2")){
                data[3] = 0x03;
            }
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
        }
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvTitle = findViewById(R.id.tv_title);
        listView1 = findViewById(R.id.listView1);
        listView2 = findViewById(R.id.listView2);

        llReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public void initData() {
        TYPE=getIntent().getStringExtra("TYPE")+"";
        for (int i=0;i<strName.length;i++){
            Map<String,Object> map=new HashMap<>();
            map.put("name",strName[i]);
            map.put("value","-");
            listK.add(map);
        }
        if (TYPE.equals("0")) {
            tvTitle.setText(getString(R.string.empty_hook_parameters));
            for (int i = 0; i < strName1.length; i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", strName1[i]);
                if (i<3) {
                    map.put("value", "-");
                }else {
                    map.put("value", "");
                }
                listData.add(map);
            }
        }else{
            if (TYPE.equals("1")){
                tvTitle.setText(getString(R.string.weight_one_parameters));
            }else {
                tvTitle.setText(getString(R.string.weight_two_parameters));
            }
            for (int i = 0; i < strName2.length; i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", strName2[i]);
                if (i<3) {
                    map.put("value", "-");
                }else {
                    map.put("value", "");
                }
                listData.add(map);
            }
        }
        listView1.setAdapter(adapter1);
        listView2.setAdapter(adapter2);
    }

    CommonAdapter adapter1 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.parameters_item, listK) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            TextView tvValue = viewHolder.getView(R.id.tv_value);

            String name=item.get("name")+"";
            tvName.setText(name);

            String ad=item.get("value")+"";
            tvValue.setText(ad);
        }
    };
    CommonAdapter adapter2 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.parameters_item, listData) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            TextView tvValue = viewHolder.getView(R.id.tv_value);

            String name=item.get("name")+"";
            tvName.setText(name);

            String ad=item.get("value")+"";
            tvValue.setText(ad);
        }
    };

    private byte[] canData_3C4;
    private byte[] canData_3D4;
    private byte[] canData_3E4;
    private byte[] canData_3F4;
    @Override
    public void getCanData_3C4(byte[] canData) {
        super.getCanData_3C4(canData);
        if (!Arrays.equals(canData, canData_3C4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_3C4 = canData;
                DecimalFormat df = new DecimalFormat("#0.000000");
                int codeZero = MyUtils.get32Data(canData[0],canData[1],canData[2],canData[3]);
                listK.get(0).put("value",df.format(codeZero/1000000.0f)+"");

                int codeFour = MyUtils.get32Data(canData[4],canData[5],canData[6],canData[7]);
                listK.get(1).put("value",df.format(codeFour/1000000.0f)+"");
                adapter1.notifyDataSetChanged();
            });
        }
    }
    @Override
    public void getCanData_3D4(byte[] canData) {
        super.getCanData_3D4(canData);
        if (!Arrays.equals(canData, canData_3D4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_3D4 = canData;
                DecimalFormat df = new DecimalFormat("#0.000000");
                int codeZero = MyUtils.get32Data(canData[0],canData[1],canData[2],canData[3]);
                listK.get(2).put("value",df.format(codeZero/1000000.0f)+"");

                int codeFour = MyUtils.get32Data(canData[4],canData[5],canData[6],canData[7]);
                listK.get(3).put("value",df.format(codeFour/1000000.0f)+"");
                adapter1.notifyDataSetChanged();
            });
        }
    }
    @Override
    public void getCanData_3E4(byte[] canData) {
        super.getCanData_3E4(canData);
        if (!Arrays.equals(canData, canData_3E4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_3E4 = canData;
                DecimalFormat df = new DecimalFormat("#0.000000");
                int codeZero = MyUtils.get32Data(canData[0],canData[1],canData[2],canData[3]);
                listK.get(4).put("value",df.format(codeZero/1000000.0f)+"");
                adapter1.notifyDataSetChanged();
            });
        }
    }
    @Override
    public void getCanData_3F4(byte[] canData) {
        super.getCanData_3F4(canData);
        if (!Arrays.equals(canData, canData_3F4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_3F4 = canData;
                int codeZero = getInteger(canData[0]) + (getInteger(canData[1]) << 8);
                listData.get(0).put("value",MyUtils.getOneDecimal(codeZero*0.01)+"");
                int codeTwo = getInteger(canData[2]) + (getInteger(canData[3]) << 8);
                listData.get(1).put("value",MyUtils.getOneDecimal(codeTwo*0.01)+"");
                int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                listData.get(2).put("value",MyUtils.getOneDecimal(codeFour*0.01)+"");
                adapter2.notifyDataSetChanged();
            });
        }
    }
}
