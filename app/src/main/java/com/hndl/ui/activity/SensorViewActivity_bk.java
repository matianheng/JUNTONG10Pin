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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorViewActivity_bk extends BaseActivity {

    private LinearLayout llReturn;
    private ListView listView;

    private String[] strName={"L1","A","P1","P2"};
    private List<Map<String,Object>> listData=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_view_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    @Override
    public void initView() {
        llReturn = findViewById(R.id.ll_return);
        listView = findViewById(R.id.listView);

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
        Map<String,Object> map=new HashMap<>();
        map.put("name","");
        map.put("zero",getString(R.string.zero));
        map.put("full",getString(R.string.full_value));
        listData.add(map);

        for (int i=0;i<strName.length;i++){
            map=new HashMap<>();
            map.put("name",strName[i]);
            map.put("zero","");
            map.put("full","");
            listData.add(map);
        }

        listView.setAdapter(adapter);

        for (int i=0;i<3;i++){
            byte[] data = new byte[]{(byte) 0x43, 0x13, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+ AppData.nodeID, data));

            data = new byte[]{(byte) 0x43, 0x13, 0x20, 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));

            data = new byte[]{(byte) 0x43, 0x13, 0x20, 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));

            data = new byte[]{(byte) 0x43, 0x13, 0x20, 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
        }
        for (int i=0;i<3;i++){
            byte[] data = new byte[]{(byte) 0x43, 0x14, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            data = new byte[]{(byte) 0x43, 0x14, 0x20, 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            data = new byte[]{(byte) 0x43, 0x14, 0x20, 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            data = new byte[]{(byte) 0x43, 0x14, 0x20, 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
        }
    }

    private byte[] canData_584;
    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                if (canData[0]==0x60&&canData[1]==0x13&&canData[2]==0x20){
                    if (canData[3]==0x01){
                        int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                        int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                        listData.get(1).put("zero","("+codeFour+","+ MyUtils.getOneDecimal(codeSix*0.01)+")");
                    }else if (canData[3]==0x03){
                        int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                        int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                        listData.get(2).put("zero","("+codeFour+","+MyUtils.getOneDecimal(codeSix*0.01)+")");
                    }else if (canData[3]==0x05){
                        int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                        int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                        listData.get(3).put("zero","("+codeFour+","+MyUtils.getOneDecimal(codeSix*0.01)+")");
                    }else if (canData[3]==0x06){
                        int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                        int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                        listData.get(4).put("zero","("+codeFour+","+MyUtils.getOneDecimal(codeSix*0.01)+")");
                    }
                }else if (canData[0]==0x60&&canData[1]==0x14&&canData[2]==0x20){
                    if (canData[3]==0x01){
                        int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                        int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                        listData.get(1).put("full","("+codeFour+","+MyUtils.getOneDecimal(codeSix*0.01)+")");
                    }else if (canData[3]==0x03){
                        int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                        int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                        listData.get(2).put("full","("+codeFour+","+MyUtils.getOneDecimal(codeSix*0.01)+")");
                    }else if (canData[3]==0x05){
                        int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                        int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                        listData.get(3).put("full","("+codeFour+","+MyUtils.getOneDecimal(codeSix*0.01)+")");
                    }else if (canData[3]==0x06){
                        int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                        int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                        listData.get(4).put("full","("+codeFour+","+MyUtils.getOneDecimal(codeSix*0.01)+")");
                    }
                }
                adapter.notifyDataSetChanged();
            });
        }
    }

    CommonAdapter adapter = new CommonAdapter<Map<String, Object>>(
            this, R.layout.quick_view_item, listData) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            TextView tvAd = viewHolder.getView(R.id.tv_ad);
            TextView tvActual = viewHolder.getView(R.id.tv_actual);

            String name=item.get("name")+"";
            tvName.setText(name);

            String ad=item.get("zero")+"";
            tvAd.setText(ad);

            String actual=item.get("full")+"";
            tvActual.setText(actual);
        }
    };
}
