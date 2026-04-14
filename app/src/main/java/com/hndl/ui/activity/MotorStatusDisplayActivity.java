package com.hndl.ui.activity;

import static com.hndl.ui.utils.MyUtils.getInteger;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ThreadUtils;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.widget.MyListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MotorStatusDisplayActivity extends BaseActivity {

    private String[] strName1={
            AppData.getInstance().getString(R.string.running_stat),
            AppData.getInstance().getString(R.string.working_mode),
            AppData.getInstance().getString(R.string.work_direction),
            AppData.getInstance().getString(R.string.fault_conditions)};
    private List<Map<String,Object>> listData1=new ArrayList<>();

    private String[] strName2={
            AppData.getInstance().getString(R.string.motor_torque),
            AppData.getInstance().getString(R.string.motor_speed),
            AppData.getInstance().getString(R.string.motor_DC_current),
            AppData.getInstance().getString(R.string.motor_temperature),
            AppData.getInstance().getString(R.string.motor_controller_temperature),
            AppData.getInstance().getString(R.string.motor_controller_voltage),
            AppData.getInstance().getString(R.string.motor_input_voltage),
            AppData.getInstance().getString(R.string.motor_operating_current)};
    private List<Map<String,Object>> listData2=new ArrayList<>();


    private LinearLayout llReturn;
    private ListView listView1;
    private ListView listView2;
    private TextView tvOverTemperature;
    private boolean isOverTemperature=false;
    private TextView tv1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motor_status_display_layout);
        initView();
        initData();
    }

    private int languageState=0;
    @Override
    public void initView() {
        llReturn = findViewById(R.id.ll_return);
        listView1 = findViewById(R.id.listView1);
        listView2 = findViewById(R.id.listView2);
        tvOverTemperature = findViewById(R.id.tv_over_temperature);
        tv1=findViewById(R.id.tv1);

        languageState = MMKVUtils.getInstance().decodeInt("languageState");
        if (languageState!=0){
            tv1.setTextSize(11);
            tvOverTemperature.setTextSize(10);
        }

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
        for (int i=0;i<strName1.length;i++){
            Map<String,Object> map=new HashMap<>();
            map.put("name",strName1[i]);
            map.put("state","-");
            listData1.add(map);
        }

        for (int i=0;i<strName2.length;i++){
            Map<String,Object> map=new HashMap<>();
            map.put("name",strName2[i]);
            map.put("state","-");
            listData2.add(map);
        }
        listView1.setAdapter(adapter1);
        listView2.setAdapter(adapter2);
    }

    private byte[] canData_1DFF32D1;
    private byte[] canData_1DFF33D1;
    @Override
    public void getCanData_1DFF32D1(byte[] canData) {
        super.getCanData_1DFF32D1(canData);
        if (!Arrays.equals(canData, canData_1DFF32D1)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_1DFF32D1 = canData;

                int codeSix = getInteger(canData[6]);
                int running = codeSix & 0x03;
                if (running==0x00){
                    listData1.get(0).put("state",getString(R.string.halt));
                }else if (running==0x01){
                    listData1.get(0).put("state",getString(R.string.run));
                }

                int mode = (codeSix >> 2) & 0x03;
                if (mode==0x01){
                    listData1.get(1).put("state",getString(R.string.drive));
                }else if (mode==0x10){
                    listData1.get(1).put("state",getString(R.string.braking_power_generation));
                }

                int direction = (codeSix >> 4) & 0x03;
                if (direction==0x01){
                    listData1.get(2).put("state",getString(R.string.corotation));
                }else if (direction==0x10){
                    listData1.get(2).put("state",getString(R.string.reversal));
                }

                int fault_conditions = (codeSix >> 6) & 0x03;
                if (fault_conditions==0x01){
                    listData1.get(3).put("state",getString(R.string.normal));
                }else if (fault_conditions==0x10){
                    listData1.get(3).put("state",getString(R.string.fault));
                }
                adapter1.notifyDataSetChanged();

                setAdapterData();
            });
        }
    }

    @Override
    public void getCanData_1DFF33D1(byte[] canData) {
        super.getCanData_1DFF33D1(canData);
        if (!Arrays.equals(canData, canData_1DFF33D1)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_1DFF33D1 = canData;
                setAdapterData();
            });
        }
    }


    private void setAdapterData() {
        if (canData_1DFF32D1!=null) {
            int codeZero = getInteger(canData_1DFF32D1[0]) + (getInteger(canData_1DFF32D1[1]) << 8);
            listData2.get(0).put("state",(codeZero-3200)+" NM");

            int codeTwo =  getInteger(canData_1DFF32D1[2]) + (getInteger(canData_1DFF32D1[3]) << 8);
            listData2.get(1).put("state", MyUtils.getOneDecimal((codeTwo*0.125)-4000)+" rpm");

            int codeFour =  getInteger(canData_1DFF32D1[4]) + (getInteger(canData_1DFF32D1[5]) << 8);
            listData2.get(2).put("state",MyUtils.getOneDecimal((codeFour*0.05)-1600)+" A");
        }

        if (canData_1DFF33D1!=null) {
            int codeZero = getInteger(canData_1DFF33D1[0]);
            listData2.get(3).put("state",(codeZero-40)+" ℃");

            int codeOne = getInteger(canData_1DFF33D1[1]);
            listData2.get(4).put("state",(codeOne-40)+" ℃");

            int codeTwo = getInteger(canData_1DFF33D1[2]) + (getInteger(canData_1DFF33D1[3]) << 8);
            listData2.get(5).put("state",MyUtils.getOneDecimal(codeTwo*0.015)+" V");

            int codeFour = getInteger(canData_1DFF33D1[4]) + (getInteger(canData_1DFF33D1[5]) << 8);
            listData2.get(6).put("state",MyUtils.getOneDecimal(codeFour*0.015)+" V");

            int codeSix = getInteger(canData_1DFF33D1[6]) + (getInteger(canData_1DFF33D1[7]) << 8);
            listData2.get(7).put("state",MyUtils.getOneDecimal(codeSix*0.05)+" A");

            if (((codeZero-40)>155)||((codeOne-40)>85)){
                isOverTemperature=true;
            }else {
                isOverTemperature=false;
            }
        }
        adapter2.notifyDataSetChanged();

        if (isOverTemperature){
            tvOverTemperature.setVisibility(View.VISIBLE);
        }else {
            tvOverTemperature.setVisibility(View.GONE);
        }
    }

    CommonAdapter adapter1 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.motor_status_display_item, listData1) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            TextView tvValue = viewHolder.getView(R.id.tv_value);

            if (languageState!=0){
                tvName.setTextSize(10);
                tvValue.setTextSize(10);
            }
            String name=item.get("name")+"";
            tvName.setText(name);

            String state=item.get("state")+"";
            tvValue.setText(state);
        }
    };

    CommonAdapter adapter2 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.motor_status_display_item, listData2) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            TextView tvValue = viewHolder.getView(R.id.tv_value);

            if (languageState!=0){
                tvName.setTextSize(10);
                tvValue.setTextSize(10);
            }
            String name=item.get("name")+"";
            tvName.setText(name);

            String state=item.get("state")+"";
            tvValue.setText(state);
        }
    };
}
