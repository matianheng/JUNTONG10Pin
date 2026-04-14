package com.hndl.ui.activity;

import static com.hndl.ui.utils.MyUtils.getInteger;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 输入输出界面
 */
public class InputOutputStatusActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private TextView tvInputState;
    private TextView tvOutputState;
    private GridView gridView1;
    private GridView gridView2;

    private String[] strNameIoInput={AppData.getInstance().getString(R.string.master_switch),
            AppData.getInstance().getString(R.string.force_switch),AppData.getInstance().getString(R.string.overload_release),
            AppData.getInstance().getString(R.string.cylinder_switching_switch),AppData.getInstance().getString(R.string.variable_amplitude_telescopic_switching_switch),
            AppData.getInstance().getString(R.string.judgment),AppData.getInstance().getString(R.string.overroll_signal),AppData.getInstance().getString(R.string.overdischarge_signal_valve),
            AppData.getInstance().getString(R.string.power_take_off_signal)};
    private String[] strNameIoInput1={AppData.getInstance().getString(R.string.counterweight_forward_switch),
            AppData.getInstance().getString(R.string.counterweight_rearward_movement_switch)};
    private String[] strName40Input={AppData.getInstance().getString(R.string.auxiliary_hook_over_roll_signal),
            AppData.getInstance().getString(R.string.front_wiper_reset),AppData.getInstance().getString(R.string.top_wiper_reset),
            AppData.getInstance().getString(R.string.oil_cooler_switch),AppData.getInstance().getString(R.string.air_conditioning_switch),
            AppData.getInstance().getString(R.string.front_wiper_low_speed_switch),AppData.getInstance().getString(R.string.front_wiper_high_speed_switch),
            AppData.getInstance().getString(R.string.top_wiper_low_speed_switch),AppData.getInstance().getString(R.string.front_wiper_wash_switch),
            AppData.getInstance().getString(R.string.top_wiper_washing_switch),AppData.getInstance().getString(R.string.turntable_work_light_switch),
            AppData.getInstance().getString(R.string.boom_work_light_switch),AppData.getInstance().getString(R.string.position_light_switch),
            AppData.getInstance().getString(R.string.diverter_valve),AppData.getInstance().getString(R.string.main_roll_overplay),
            AppData.getInstance().getString(R.string.auxiliary_roll_over_release),AppData.getInstance().getString(R.string.proximity_switch),
            AppData.getInstance().getString(R.string.push_rod_in_place),AppData.getInstance().getString(R.string.push_rod_retracted_into_place),
            AppData.getInstance().getString(R.string.under_voltage_signal)};
    private String[] strNameIoOutput={AppData.getInstance().getString(R.string.main_control_valve),
            AppData.getInstance().getString(R.string.cylinder_switching_valve),AppData.getInstance().getString(R.string.lifting_shrinking_arm_valve),
            AppData.getInstance().getString(R.string.lowering_extending_arm_valve),AppData.getInstance().getString(R.string.overroll_protection_valve),
            AppData.getInstance().getString(R.string.overdischarge_protection_valve),AppData.getInstance().getString(R.string.buzzer),
            AppData.getInstance().getString(R.string.oil_cooler_air_conditioning_control)};
    private String[] strNameIoOutput1={AppData.getInstance().getString(R.string.counterweight_air_conditioning_switching_valve),
            AppData.getInstance().getString(R.string.counterweight_forward_valve),AppData.getInstance().getString(R.string.rearward_weight_shift_valve),
            AppData.getInstance().getString(R.string.counterweight_main_control_valve),AppData.getInstance().getString(R.string.micro_motion_valve)};
    private String[] strName40Output={AppData.getInstance().getString(R.string.front_wiper_high_speed),
            AppData.getInstance().getString(R.string.front_wiper_low_speed),AppData.getInstance().getString(R.string.top_wiper_low_speed),
            AppData.getInstance().getString(R.string.front_washing),AppData.getInstance().getString(R.string.top_washing),
            AppData.getInstance().getString(R.string.work_lights_turntable),AppData.getInstance().getString(R.string.work_lights_boom),
            AppData.getInstance().getString(R.string.position_light),AppData.getInstance().getString(R.string.three_color_green_light),
            AppData.getInstance().getString(R.string.three_color_yellow_light),AppData.getInstance().getString(R.string.three_color_red_light),
            AppData.getInstance().getString(R.string.air_conditioning_control_end),AppData.getInstance().getString(R.string.engine_shutdown_control),
            AppData.getInstance().getString(R.string.push_rod_extension),AppData.getInstance().getString(R.string.push_rod_contraction)};
    private List<Map<String,Object>> listDataInput=new ArrayList<>();
    private List<Map<String,Object>> listDataOutput=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_output_status_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    private int languageState=0;
    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvInputState = findViewById(R.id.tv_input_state);
        tvOutputState = findViewById(R.id.tv_output_state);
        gridView1 = findViewById(R.id.gridView1);
        gridView2 = findViewById(R.id.gridView2);

        llReturn.setOnClickListener(this);
        tvInputState.setOnClickListener(this);
        tvOutputState.setOnClickListener(this);

        languageState = MMKVUtils.getInstance().decodeInt("languageState");
    }

    private byte[] canData_1F4;
    @Override
    public void getCanData_1F4(byte[] canData) {
        super.getCanData_1F4(canData);
        if (!Arrays.equals(canData, canData_1F4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_1F4 = canData;
                int codeZero = getInteger(canData[0]);
                int codeFour= getInteger(canData[4]);
                listDataOutput.get(0).put("state",codeZero & 0x01);
                listDataOutput.get(1).put("state",(codeZero >> 1) & 0x01);
                listDataOutput.get(2).put("state",(codeZero >> 2) & 0x01);
                listDataOutput.get(3).put("state",(codeZero >> 3) & 0x01);
                listDataOutput.get(4).put("state",(codeZero >> 4) & 0x01);
                listDataOutput.get(5).put("state",(codeZero >> 5) & 0x01);
                listDataOutput.get(6).put("state",(codeZero >> 6) & 0x01);
                listDataOutput.get(7).put("state",(codeZero >> 7) & 0x01);

                if (AppData.dataSrc==2) {
                    int codeOne = getInteger(canData[1]);
                    listDataOutput.get(8).put("state", codeOne & 0x01);
                    listDataOutput.get(9).put("state", (codeOne >> 1) & 0x01);
                    listDataOutput.get(10).put("state", (codeOne >> 2) & 0x01);
                    listDataOutput.get(11).put("state", (codeOne >> 3) & 0x01);
                    listDataOutput.get(12).put("state", (codeOne >> 4) & 0x01);
                    listDataOutput.get(13).put("state", (codeOne >> 5) & 0x01);
                    listDataOutput.get(14).put("state", (codeOne >> 6) & 0x01);
                    listDataOutput.get(15).put("state", (codeOne >> 7) & 0x01);
                    int codeTwo = getInteger(canData[2]);
                    listDataOutput.get(16).put("state", codeTwo & 0x01);
                    listDataOutput.get(17).put("state", (codeTwo >> 1) & 0x01);
                    listDataOutput.get(18).put("state", (codeTwo >> 2) & 0x01);
                    listDataOutput.get(19).put("state", (codeTwo >> 3) & 0x01);
                    listDataOutput.get(20).put("state", (codeTwo >> 4) & 0x01);
                    listDataOutput.get(21).put("state", (codeTwo >> 5) & 0x01);
                    listDataOutput.get(22).put("state", (codeTwo >> 6) & 0x01);
                    if (AppData.modelType==26||AppData.modelType==27||AppData.modelType==29){
                        int codeThree = getInteger(canData[3]);
                        listDataOutput.get(23).put("state", (codeThree >> 3) & 0x01);
                    }
                }else if (AppData.modelType==32||AppData.modelType==36){
                    int codeTwo = getInteger(canData[2]);
                    int codeThree = getInteger(canData[3]);
                    listDataOutput.get(8).put("state", (codeTwo >> 7) & 0x01);
                    listDataOutput.get(9).put("state", codeThree & 0x01);
                    listDataOutput.get(10).put("state", (codeThree >> 1) & 0x01);
                    listDataOutput.get(11).put("state", (codeThree >> 2) & 0x01);
                    listDataOutput.get(12).put("state", (codeThree >> 3) & 0x01);
                }
                adapter2.notifyDataSetChanged();

                int codeSix = getInteger(canData[6]);
                listDataInput.get(0).put("state",codeFour & 0x01);
                listDataInput.get(1).put("state",(codeFour >> 1) & 0x01);
                listDataInput.get(2).put("state",(codeFour >> 2) & 0x01);
                listDataInput.get(3).put("state",(codeFour >> 3) & 0x01);
                listDataInput.get(4).put("state",(codeFour >> 4) & 0x01);
                listDataInput.get(5).put("state",(codeFour >> 5) & 0x01);
                listDataInput.get(6).put("state",(codeFour >> 6) & 0x01);
                listDataInput.get(7).put("state",(codeSix >> 6) & 0x01);
                listDataInput.get(8).put("state",(codeFour >> 7) & 0x01);

                if (AppData.dataSrc==2) {
                    int codeFive = getInteger(canData[5]);
                    listDataInput.get(9).put("state", codeFive & 0x01);
                    listDataInput.get(10).put("state", (codeFive >> 1) & 0x01);
                    listDataInput.get(11).put("state", (codeFive >> 2) & 0x01);
                    listDataInput.get(12).put("state", (codeFive >> 3) & 0x01);
                    listDataInput.get(13).put("state", (codeFive >> 4) & 0x01);
                    listDataInput.get(14).put("state", (codeFive >> 5) & 0x01);
                    listDataInput.get(15).put("state", (codeFive >> 6) & 0x01);
                    listDataInput.get(16).put("state", (codeFive >> 7) & 0x01);

                    listDataInput.get(17).put("state", codeSix & 0x01);
                    listDataInput.get(18).put("state", (codeSix >> 1) & 0x01);
                    listDataInput.get(19).put("state", (codeSix >> 2) & 0x01);
                    listDataInput.get(20).put("state", (codeSix >> 3) & 0x01);
                    listDataInput.get(21).put("state", (codeSix >> 4) & 0x01);
                    listDataInput.get(22).put("state", (codeSix >> 5) & 0x01);
                    listDataInput.get(23).put("state", (codeSix >> 6) & 0x01);
                    listDataInput.get(24).put("state", (codeSix >> 7) & 0x01);

                    int codeSeven= getInteger(canData[7]);
                    listDataInput.get(25).put("state", codeSeven & 0x01);
                    listDataInput.get(26).put("state", (codeSeven >> 1) & 0x01);
                    listDataInput.get(27).put("state", (codeSeven >> 2) & 0x01);
                    listDataInput.get(28).put("state", (codeSeven >> 3) & 0x01);
                }else if (AppData.modelType==32||AppData.modelType==36){
                    int codeSeven= getInteger(canData[7]);
                    listDataInput.get(9).put("state", (codeSeven >> 4) & 0x01);
                    listDataInput.get(10).put("state", (codeSeven >> 5) & 0x01);
                }
                adapter1.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void initData() {
        for (int i=0;i<strNameIoInput.length;i++){
            Map<String,Object> map=new HashMap<>();
            map.put("name",strNameIoInput[i]);
            map.put("state", "0");
            listDataInput.add(map);
        }
        for (int i=0;i<strNameIoOutput.length;i++){
            Map<String,Object> map=new HashMap<>();
            map.put("name",strNameIoOutput[i]);
            map.put("state", "0");
            listDataOutput.add(map);
        }
        if (AppData.dataSrc==2) {
            for (int i=0;i<strName40Input.length;i++){
                Map<String,Object> map=new HashMap<>();
                map.put("name",strName40Input[i]);
                map.put("state", "0");
                listDataInput.add(map);
            }
            for (int i = 0; i < strName40Output.length; i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", strName40Output[i]);
                map.put("state", "0");
                listDataOutput.add(map);
            }
            if (AppData.modelType==26||AppData.modelType==27||AppData.modelType==29){
                Map<String, Object> map = new HashMap<>();
                map.put("name", getString(R.string.micro_motion_valve));
                map.put("state", "0");
                listDataOutput.add(map);
            }
        }else if (AppData.modelType==32||AppData.modelType==36){
            for (int i=0;i<strNameIoInput1.length;i++){
                Map<String,Object> map=new HashMap<>();
                map.put("name",strNameIoInput1[i]);
                map.put("state", "0");
                listDataInput.add(map);
            }
            for (int i=0;i<strNameIoOutput1.length;i++){
                Map<String,Object> map=new HashMap<>();
                map.put("name",strNameIoOutput1[i]);
                map.put("state", "0");
                listDataOutput.add(map);
            }
        }
        gridView1.setAdapter(adapter1);
        gridView2.setAdapter(adapter2);
    }

    CommonAdapter adapter1 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.input_output_status_item, listDataInput) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            ImageView ivState = viewHolder.getView(R.id.iv_state);
            String name=item.get("name")+"";
            tvName.setText(name);

            if (languageState!=0){
                tvName.setTextSize(8);
            }
            String state=item.get("state")+"";
            if (state.equals("1")){
                ivState.setImageResource(R.drawable.grenn_round_bg);
            }else {
                ivState.setImageResource(R.drawable.white_round_bg);
            }
        }
    };

    CommonAdapter adapter2 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.input_output_status_item, listDataOutput) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            ImageView ivState = viewHolder.getView(R.id.iv_state);
            String name=item.get("name")+"";
            tvName.setText(name);

            if (languageState!=0){
                tvName.setTextSize(8);
            }
            String state=item.get("state")+"";
            if (state.equals("1")){
                ivState.setImageResource(R.drawable.grenn_round_bg);
            }else {
                ivState.setImageResource(R.drawable.white_round_bg);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_return:{
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_input_state:{
                tvInputState.setBackground(getDrawable(R.color.c3363FC));
                tvInputState.setTextColor(getResources().getColor(R.color.white));
                tvOutputState.setBackground(getDrawable(R.color.c606060));
                tvOutputState.setTextColor(getResources().getColor(R.color.c444444));
                gridView1.setVisibility(View.VISIBLE);
                gridView2.setVisibility(View.GONE);
                break;
            }
            case R.id.tv_output_state:{
                tvOutputState.setBackground(getDrawable(R.color.c3363FC));
                tvOutputState.setTextColor(getResources().getColor(R.color.white));
                tvInputState.setBackground(getDrawable(R.color.c606060));
                tvInputState.setTextColor(getResources().getColor(R.color.c444444));
                gridView1.setVisibility(View.GONE);
                gridView2.setVisibility(View.VISIBLE);
                break;
            }
        }
    }
}
