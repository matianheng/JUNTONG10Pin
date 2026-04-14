package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeightSelectionActivity extends BaseActivity implements View.OnClickListener {

    private TextView btnConfirm;
    private ListView listView;

    private String[] strName={"1.2T","4.0T"};
    private List<Map<String,Object>> listData=new ArrayList<>();
    private int index=-1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weight_selection_layout);
        initView();
        initData();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (Map<String,Object> map : listData){
                    map.put("state","0");
                }
                listData.get(position).put("state","1");
                index= (int) listData.get(position).get("type");
                adapter.notifyDataSetChanged();
            }
        });
        byte[] data = new byte[]{(byte) 0x01, 0x00, 0x00, 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
        canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x710+AppData.nodeID, data));
        createLoadingDialog("");
        startRepeatingTask();
    }

    private byte[] canData_584;
    private byte[] mModifyWorkingCanData = new byte[]{0x23, 0x10, 0x20, 0x01, 0x00, 0x00, 0x00, 0x00};
    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        //if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                if (canData[0] == 0x60 && canData[1] == 0x10 && canData[2] == 0x20 && canData[3] == 0x01) {
                    stopRepeatingTask();
                    for (int i = 4; i < 8; i++) {
                        mModifyWorkingCanData[i] = canData[i];
                    }
                    int codeSix = getInteger(canData[6]);
                    int indexCode=codeSix & 0x07;
                    if (indexCode==1){
                        index=1;
                        listData.get(0).put("state","0");
                        listData.get(1).put("state","1");
                    }else {
                        index=0;
                        listData.get(0).put("state","1");
                        listData.get(1).put("state","0");
                    }
                    adapter.notifyDataSetChanged();
                    if (canState==1){
                        canState=-1;
                        AppData.CounterWeight=index;
                        ActivityUtils.startActivity(MainActivity.class);
                        finish();
                        overridePendingTransition(0, 0);
                    }else {
                        canState=2;
                        startRepeatingTask();
                    }
                    dismissLoadingDialog();
                }
            });
        //}
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
            }else if (canState==1){
                mModifyWorkingCanData[6] = (byte) (index & 0xff);
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, mModifyWorkingCanData));
            }else if (canState==2){
                byte[] data = new byte[]{(byte) 0x01, 0x00, 0x00, 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x710+AppData.nodeID, data));
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
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
        mHandler = null;
    }

    @Override
    public void initView() {
        btnConfirm = findViewById(R.id.btn_confirm);
        listView = findViewById(R.id.listView);
        btnConfirm.setOnClickListener(this);
    }

    @Override
    public void initData() {
        for (int i=0;i<strName.length;i++){
            Map<String,Object> map=new HashMap<>();
            map.put("name",strName[i]);
            map.put("type",i);
            if (i==0){
                map.put("state", "1");
                index=i;
            }else {
                map.put("state", "0");
            }
            listData.add(map);
        }
        listView.setAdapter(adapter);
    }

    CommonAdapter adapter = new CommonAdapter<Map<String, Object>>(
            this, R.layout.model_selection_item, listData) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            ImageView ivSel = viewHolder.getView(R.id.iv_sel);
            String name=item.get("name")+"";
            tvName.setText(name);

            String state=item.get("state")+"";
            if (state.equals("1")){
                ivSel.setVisibility(View.VISIBLE);
            }else {
                ivSel.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //onBackPressed();
            return true;
        } else {// 如果不是back键正常响应
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_confirm:{
                if (index!=-1){
                    createLoadingDialog("");
                    canState = 1;
                    //startRepeatingTask();
                }
                break;
            }
        }
    }
}
