package com.hndl.ui.activity;

import static com.hndl.ui.utils.MyUtils.getInteger;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.widget.MyListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 过流报警
 */
public class OvercurrentAlarmActivity extends BaseActivity {

    private LinearLayout llReturn;
    private MyListView listView1;
    private MyListView listView2;

    private String[] strName1={"DO_00","DO_01","DO_02","DO_03","DO_04","DO_05","DO_06","DO_07",
            "DO_10","DO_11","DO_12","DO_13"};
    private String[] strName2={"DO_14","DO_15","DO_16","DO_17","DO_20","DO_21","DO_22","DO_23",
            "DO_24","DO_25","DO_26","DO_27"};
    private List<Map<String,Object>> listData1=new ArrayList<>();
    private List<Map<String,Object>> listData2=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overcurrent_alarm_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        listView1 = findViewById(R.id.listView1);
        listView2 = findViewById(R.id.listView2);

        llReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public void initData() {
        for (int i=0;i<strName1.length;i++){
            Map<String,Object> map=new HashMap<>();
            map.put("name",strName1[i]+" "+getString(R.string.overcurrent_fault));
            map.put("state","0");
            listData1.add(map);
        }
        for (int i=0;i<strName2.length;i++){
            Map<String,Object> map=new HashMap<>();
            map.put("name",strName2[i]+" "+getString(R.string.overcurrent_fault));
            map.put("state","0");
            listData2.add(map);
        }
        listView1.setAdapter(adapter1);
        listView2.setAdapter(adapter2);
    }

    CommonAdapter adapter1 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.fault_alarm_item, listData1) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            ImageView ivState = viewHolder.getView(R.id.iv_state);

            String name=item.get("name")+"";
            tvName.setText(name);

            if ((item.get("state")+"").equals("0")){
                ivState.setImageResource(R.drawable.grenn_round_icon);
            }else {
                ivState.setImageResource(R.drawable.red_round_icon);
            }
        }
    };

    CommonAdapter adapter2 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.fault_alarm_item, listData2) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            ImageView ivState = viewHolder.getView(R.id.iv_state);

            String name=item.get("name")+"";
            tvName.setText(name);

            if ((item.get("state")+"").equals("0")){
                ivState.setImageResource(R.drawable.grenn_round_icon);
            }else {
                ivState.setImageResource(R.drawable.red_round_icon);
            }
        }
    };

    private byte[] canData_this;
    @Override
    public void getFaultAlarm(byte[] canData) {
        super.getFaultAlarm(canData);
        if (!Arrays.equals(canData, canData_this)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_this = canData;

                int codeFour = getInteger(canData[4]);
                listData1.get(0).put("state",(codeFour & 0x01)+"");
                listData1.get(1).put("state",((codeFour >> 1) & 0x01)+"");
                listData1.get(2).put("state",((codeFour >> 2) & 0x01)+"");
                listData1.get(3).put("state",((codeFour >> 3) & 0x01)+"");
                listData1.get(4).put("state",((codeFour >> 4) & 0x01)+"");
                listData1.get(5).put("state",((codeFour >> 5) & 0x01)+"");
                listData1.get(6).put("state",((codeFour >> 6) & 0x01)+"");
                listData1.get(7).put("state",((codeFour >> 7) & 0x01)+"");

                int codeFive = getInteger(canData[5]);
                listData1.get(8).put("state",(codeFive & 0x01)+"");
                listData1.get(9).put("state",((codeFive >> 1) & 0x01)+"");
                listData1.get(10).put("state",((codeFive >> 2) & 0x01)+"");
                listData1.get(11).put("state",((codeFive >> 3) & 0x01)+"");
                listData2.get(0).put("state",((codeFive >> 4) & 0x01)+"");
                listData2.get(1).put("state",((codeFive >> 5) & 0x01)+"");
                listData2.get(2).put("state",((codeFive >> 6) & 0x01)+"");
                listData2.get(3).put("state",((codeFive >> 7) & 0x01)+"");

                adapter2.notifyDataSetChanged();

                int codeSix = getInteger(canData[6]);
                listData2.get(4).put("state",(codeSix & 0x01)+"");
                listData2.get(5).put("state",((codeSix >> 1) & 0x01)+"");
                listData2.get(6).put("state",((codeSix >> 2) & 0x01)+"");
                listData2.get(7).put("state",((codeSix >> 3) & 0x01)+"");
                listData2.get(8).put("state",((codeSix >> 4) & 0x01)+"");
                listData2.get(9).put("state",((codeSix >> 5) & 0x01)+"");
                listData2.get(10).put("state",((codeSix >> 6) & 0x01)+"");
                listData2.get(11).put("state",((codeSix >> 7) & 0x01)+"");

                adapter2.notifyDataSetChanged();
            });
        }
    }

}
