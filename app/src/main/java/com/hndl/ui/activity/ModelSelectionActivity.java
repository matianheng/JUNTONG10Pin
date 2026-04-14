package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.widget.HintDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelSelectionActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private TextView btnConfirm;
    private ListView listView1;
    private ListView listView2;

    private String[] strName1={"JPC100E5-Ⅰ","JPC100E5-Ⅱ","JPC100E5-Ⅲ","JPC100H5","JTC12H6B-I","JTC12H5B-I"};
    private int[] type1={2,3,4,5,32,36};
    private List<Map<String,Object>> listData1=new ArrayList<>();
    private String[] strName2={"JPC100H5","JPC100H5Ⅱ","JPC120H5","JPC120H5Ⅱ","JPC160H5","JPC160H5Ⅱ",
                        "JPC120H5-1","JPC120H5Ⅱ-1","JPC160H5-1","JPC160H5Ⅱ-1","JTC16H6","JTC12H5","JTC12H6","JPC100H5B-I","JTC16E5-I",
                        "JTC16E5II-I","JTC12H5A","JTC12H6A","JTC16H6A"};
    private int[] type2={5,6,7,8,9,10,11,12,13,14,15,17,18,20,21,22,26,27,29};
    private List<Map<String,Object>> listData2=new ArrayList<>();

    private int model_type=0;

    private int hint = -1;
    private HintDialog hintDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.model_selection_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
        hintDialog=new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm&&hint==1){
                    canState=1;
                    createLoadingDialog("");
                    startRepeatingTask();
                }else if (isConfirm&&hint==2){
                    Intent intent=new Intent();
                    intent.setClass(ModelSelectionActivity.this, SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });
    }

    private Handler mHandler = new Handler();
    private static final int DELAY_TIME_MS = 100; // 延迟时间
    private int canState = 0;
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            // 在这里执行你的任务
            if (canState==1) {
                byte[] data = new byte[]{(byte) 0x2F, 0x22, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                data[4] = (byte) model_type;
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            }else if (canState==2) {
                byte[] data = new byte[]{(byte) 0x23, 0x11, 0x10, 0x01, (byte) 0x6C, (byte) 0x6F, (byte) 0x61, 0x64};
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

    private byte[] canData_584;
    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                if (canData[0] == 0x60 && canData[1] == 0x22 && canData[2] == 0x20&& canData[3] == 0x01) {
                    canState=2;
                }else if (canData[0] == 0x60 && canData[1] == 0x11 && canData[2] == 0x10&& canData[3] == 0x01) {
                    stopRepeatingTask();
                    AppData.modelType=model_type;
                    MMKVUtils.getInstance().encode("modelType", AppData.modelType);
                    dismissLoadingDialog();
                    showDialog(2,2,getString(R.string.model_reset_success));
                }
            });
        }
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        btnConfirm = findViewById(R.id.btn_confirm);
        listView1 = findViewById(R.id.listView1);
        listView2 = findViewById(R.id.listView2);

        btnConfirm.setBackgroundResource(R.drawable.grey_bg_circle);
        llReturn.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        if (AppData.dataSrc==2){
            listView1.setVisibility(View.GONE);
        }else {
            listView2.setVisibility(View.GONE);
        }
    }

    @Override
    public void initData() {
        model_type= AppData.modelType;
        Map<String,Object> map=new HashMap<>();

        for (int i=0;i<strName1.length;i++){
            map=new HashMap<>();
            map.put("name",strName1[i]);
            map.put("type",type1[i]);
            if (model_type==type1[i]) {
                map.put("state", "1");
            }else {
                map.put("state", "0");
            }
            listData1.add(map);
        }
        for (int i=0;i<strName2.length;i++){
            map=new HashMap<>();
            map.put("name",strName2[i]);
            map.put("type",type2[i]);
            if (model_type==type2[i]) {
                map.put("state", "1");
            }else {
                map.put("state", "0");
            }
            listData2.add(map);
        }

        listView1.setAdapter(adapter1);
        listView2.setAdapter(adapter2);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (Map<String,Object> map : listData1){
                    map.put("state","0");
                }
                for (Map<String,Object> map : listData2){
                    map.put("state","0");
                }
                listData1.get(position).put("state","1");
                model_type= (int) listData1.get(position).get("type");
                adapter1.notifyDataSetChanged();
                adapter2.notifyDataSetChanged();
                if (AppData.modelType==model_type){
                    btnConfirm.setBackgroundResource(R.drawable.grey_bg_circle);
                }else {
                    btnConfirm.setBackgroundResource(R.drawable.blue_bg_circle);
                }
            }
        });
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (Map<String,Object> map : listData1){
                    map.put("state","0");
                }
                for (Map<String,Object> map : listData2){
                    map.put("state","0");
                }
                listData2.get(position).put("state","1");
                model_type= (int) listData2.get(position).get("type");
                adapter1.notifyDataSetChanged();
                adapter2.notifyDataSetChanged();
                if (AppData.modelType==model_type){
                    btnConfirm.setBackgroundResource(R.drawable.grey_bg_circle);
                }else {
                    btnConfirm.setBackgroundResource(R.drawable.blue_bg_circle);
                }
            }
        });
    }

    CommonAdapter adapter1 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.model_selection_item, listData1) {
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

    CommonAdapter adapter2 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.model_selection_item, listData2) {
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return: {
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.btn_confirm:{
                if (AppData.modelType==model_type){
                    return;
                }
                showDialog(1,3,getString(R.string.select_this_model));
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
