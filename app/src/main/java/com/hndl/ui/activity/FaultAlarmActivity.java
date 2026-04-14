package com.hndl.ui.activity;

import static com.hndl.ui.utils.MyUtils.getInteger;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.MMKVUtils;

import java.util.Arrays;

public class FaultAlarmActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private ImageView ivOverload;
    private ImageView ivEquipmentFailure;
    private ImageView ivOverrollAlarm;
    private ImageView ivOverDischargeAlarm;
    private ImageView ivFiveLegOverpressureAlarm;
    private ImageView ivAngleUpperLimitAlarm;
    private ImageView ivAngleLowerLimitAlarm;
    private ImageView ivEngineFault;
    private ImageView ivExcessiveCoolantTemperature;
    private ImageView ivLowOilPressuse;
    private ImageView ivLowFuelVolume;
    private TextView tvCompulsoryTermination;
    private TextView tvLength1Fault;
    private TextView tvLength2Fault;
    private TextView tvAngle1Fault;
    private TextView tvAngle2Fault;
    private TextView tvPressure1Fault;
    private TextView tvPressure2Fault;
    private TextView tvAnemometerMalfunction;
    private ImageView ivBusCommunicationFailure;

    private TextView tv1,tv2,tv3,tv4,tv5,tv6,tv7,tv8,tv9,tv10,tv11,tv12,tv13,tv14,tv15,tv16,tv17;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fault_alarm_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    @Override
    public void initView() {


        llReturn = findViewById(R.id.ll_return);
        ivOverload = findViewById(R.id.iv_overload);
        ivEquipmentFailure = findViewById(R.id.iv_equipment_failure);
        ivOverrollAlarm = findViewById(R.id.iv_overroll_alarm);
        ivOverDischargeAlarm = findViewById(R.id.iv_over_discharge_alarm);
        ivFiveLegOverpressureAlarm = findViewById(R.id.iv_five_leg_overpressure_alarm);
        ivAngleUpperLimitAlarm = findViewById(R.id.iv_angle_upper_limit_alarm);
        ivAngleLowerLimitAlarm = findViewById(R.id.iv_angle_lower_limit_alarm);
        ivEngineFault = findViewById(R.id.iv_engine_fault);
        ivExcessiveCoolantTemperature = findViewById(R.id.iv_excessive_coolant_temperature);
        ivLowOilPressuse = findViewById(R.id.iv_low_oil_pressuse);
        ivLowFuelVolume = findViewById(R.id.iv_low_fuel_volume);
        tvCompulsoryTermination = findViewById(R.id.tv_compulsory_termination);
        tvLength1Fault = findViewById(R.id.tv_length_1_fault);
        tvLength2Fault = findViewById(R.id.tv_length_2_fault);
        tvAngle1Fault = findViewById(R.id.tv_angle_1_fault);
        tvAngle2Fault = findViewById(R.id.tv_angle_2_fault);
        tvPressure1Fault = findViewById(R.id.tv_pressure_1_fault);
        tvPressure2Fault = findViewById(R.id.tv_pressure_2_fault);
        tvAnemometerMalfunction = findViewById(R.id.tv_anemometer_malfunction);
        ivBusCommunicationFailure = findViewById(R.id.iv_bus_communication_failure);
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
        tv13=findViewById(R.id.tv13);
        tv14=findViewById(R.id.tv14);
        tv15=findViewById(R.id.tv15);
        tv16=findViewById(R.id.tv16);
        tv17=findViewById(R.id.tv17);
        int languageState = MMKVUtils.getInstance().decodeInt("languageState");
        if (languageState!=0){
            tvCompulsoryTermination.setTextSize(9);
            tvLength1Fault.setTextSize(9);
            tvLength2Fault.setTextSize(9);
            tvAngle1Fault.setTextSize(9);
            tvAngle2Fault.setTextSize(9);
            tvPressure1Fault.setTextSize(9);
            tvPressure2Fault.setTextSize(9);
            tvAnemometerMalfunction.setTextSize(9);

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
            tv13.setTextSize(9);
            tv14.setTextSize(9);
            tv15.setTextSize(9);
            tv16.setTextSize(9);
            tv17.setTextSize(9);
        }

        llReturn.setOnClickListener(this);
    }

    @Override
    public void initData() {
        if (AppData.isBusFailure){
            ivBusCommunicationFailure.setImageResource(R.drawable.red_round_icon);
        } else {
            ivBusCommunicationFailure.setImageResource(R.drawable.grenn_round_icon);
        }
    }

    private byte[] canData_this;
    @Override
    public void getFaultAlarm(byte[] canData) {
        super.getFaultAlarm(canData);
        //LogUtil.e("XJW","canData1:"+ ConvertUtils.bytes2HexString(canData));
        if (!Arrays.equals(canData, canData_this)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_this = canData;
                int codeZero = getInteger(canData[0]);
                int overload = codeZero & 0x01;
                if (overload == 1) {
                    ivOverload.setImageResource(R.drawable.red_round_icon);
                } else {
                    ivOverload.setImageResource(R.drawable.grenn_round_icon);
                }

                int equipmentFailure = (codeZero >> 1) & 0x01;
                if (equipmentFailure == 1) {
                    ivEquipmentFailure.setImageResource(R.drawable.red_round_icon);
                } else {
                    ivEquipmentFailure.setImageResource(R.drawable.grenn_round_icon);
                }

                int overrollAlarm = (codeZero >> 2) & 0x01;
                if (overrollAlarm == 1) {
                    ivOverrollAlarm.setImageResource(R.drawable.red_round_icon);
                } else {
                    ivOverrollAlarm.setImageResource(R.drawable.grenn_round_icon);
                }

                int overDischargeAlarm = (codeZero >> 3) & 0x01;
                if (overDischargeAlarm == 1) {
                    ivOverDischargeAlarm.setImageResource(R.drawable.red_round_icon);
                } else {
                    ivOverDischargeAlarm.setImageResource(R.drawable.grenn_round_icon);
                }

                int fiveLegOverpressureAlarm = (codeZero >> 4) & 0x01;
                if (fiveLegOverpressureAlarm == 1) {
                    ivFiveLegOverpressureAlarm.setImageResource(R.drawable.red_round_icon);
                } else {
                    ivFiveLegOverpressureAlarm.setImageResource(R.drawable.grenn_round_icon);
                }

                int angleUpperLimitAlarm = (codeZero >> 5) & 0x01;
                if (angleUpperLimitAlarm == 1) {
                    ivAngleUpperLimitAlarm.setImageResource(R.drawable.red_round_icon);
                } else {
                    ivAngleUpperLimitAlarm.setImageResource(R.drawable.grenn_round_icon);
                }

                int angleLowerLimitAlarm = (codeZero >> 6) & 0x01;
                if (angleLowerLimitAlarm == 1) {
                    ivAngleLowerLimitAlarm.setImageResource(R.drawable.red_round_icon);
                } else {
                    ivAngleLowerLimitAlarm.setImageResource(R.drawable.grenn_round_icon);
                }

                int compulsoryTermination = (codeZero >> 7) & 0x01;
                if (compulsoryTermination == 1) {
                    tvCompulsoryTermination.setText(R.string.open);
                    tvCompulsoryTermination.setTextColor(getResources().getColor(R.color.cED1111));
                } else {
                    tvCompulsoryTermination.setText(R.string.close);
                    tvCompulsoryTermination.setTextColor(getResources().getColor(R.color.c4EB20E));
                }

                int codeOne = getInteger(canData[1]);
                int length1Fault = codeOne & 0x01 + (((codeOne >> 1) & 0x01) << 1);
                if (length1Fault == 1) {
                    tvLength1Fault.setText(R.string.ad_below_lower_limit);
                    tvLength1Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else if (length1Fault == 2) {
                    tvLength1Fault.setText(R.string.ad_below_upper_limit);
                    tvLength1Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else {
                    tvLength1Fault.setText(R.string.normal);
                    tvLength1Fault.setTextColor(getResources().getColor(R.color.c4EB20E));
                }

                int length2Fault = ((codeOne >> 2) & 0x01) & 0x01 + (((codeOne >> 3) & 0x01) << 1);
                if (length2Fault == 1) {
                    tvLength2Fault.setText(R.string.ad_below_lower_limit);
                    tvLength2Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else if (length2Fault == 2) {
                    tvLength2Fault.setText(R.string.ad_below_upper_limit);
                    tvLength2Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else {
                    tvLength2Fault.setText(R.string.normal);
                    tvLength2Fault.setTextColor(getResources().getColor(R.color.c4EB20E));
                }

                int angle1Fault = ((codeOne >> 4) & 0x01) & 0x01 + (((codeOne >> 5) & 0x01) << 1);
                if (angle1Fault == 1) {
                    tvAngle1Fault.setText(R.string.ad_below_lower_limit);
                    tvAngle1Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else if (angle1Fault == 2) {
                    tvAngle1Fault.setText(R.string.ad_below_upper_limit);
                    tvAngle1Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else {
                    tvAngle1Fault.setText(R.string.normal);
                    tvAngle1Fault.setTextColor(getResources().getColor(R.color.c4EB20E));
                }

                int angle2Fault = ((codeOne >> 6) & 0x01) & 0x01 + (((codeOne >> 7) & 0x01) << 1);
                if (angle2Fault == 1) {
                    tvAngle2Fault.setText(R.string.ad_below_lower_limit);
                    tvAngle2Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else if (angle2Fault == 2) {
                    tvAngle2Fault.setText(R.string.ad_below_upper_limit);
                    tvAngle2Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else {
                    tvAngle2Fault.setText(R.string.normal);
                    tvAngle2Fault.setTextColor(getResources().getColor(R.color.c4EB20E));
                }

                int codeTwo = getInteger(canData[2]);
                int pressure1Fault = codeTwo & 0x01 + (((codeTwo >> 1) & 0x01) << 1);
                if (pressure1Fault == 1) {
                    tvPressure1Fault.setText(R.string.ad_below_lower_limit);
                    tvPressure1Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else if (pressure1Fault == 2) {
                    tvPressure1Fault.setText(R.string.ad_below_upper_limit);
                    tvPressure1Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else {
                    tvPressure1Fault.setText(R.string.normal);
                    tvPressure1Fault.setTextColor(getResources().getColor(R.color.c4EB20E));
                }

                int pressure2Fault = ((codeTwo >> 2) & 0x01) & 0x01 + (((codeTwo >> 3) & 0x01) << 1);
                if (pressure2Fault == 1) {
                    tvPressure2Fault.setText(R.string.ad_below_lower_limit);
                    tvPressure2Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else if (pressure2Fault == 2) {
                    tvPressure2Fault.setText(R.string.ad_below_upper_limit);
                    tvPressure2Fault.setTextColor(getResources().getColor(R.color.cED1111));
                }else {
                    tvPressure2Fault.setText(R.string.normal);
                    tvPressure2Fault.setTextColor(getResources().getColor(R.color.c4EB20E));
                }

                int anemometerMalfunction = ((codeTwo >> 4) & 0x01) & 0x01 + (((codeTwo >> 5) & 0x01) << 1);
                if (anemometerMalfunction == 1) {
                    tvAnemometerMalfunction.setText(R.string.ad_below_lower_limit);
                    tvAnemometerMalfunction.setTextColor(getResources().getColor(R.color.cED1111));
                }else if (anemometerMalfunction == 2) {
                    tvAnemometerMalfunction.setText(R.string.ad_below_upper_limit);
                    tvAnemometerMalfunction.setTextColor(getResources().getColor(R.color.cED1111));
                }else {
                    tvAnemometerMalfunction.setText(R.string.normal);
                    tvAnemometerMalfunction.setTextColor(getResources().getColor(R.color.c4EB20E));
                }

                int codeThree = getInteger(canData[3]);
                int engineFault = codeThree & 0x01;
                if (engineFault == 1) {
                    ivEngineFault.setImageResource(R.drawable.red_round_icon);
                } else {
                    ivEngineFault.setImageResource(R.drawable.grenn_round_icon);
                }

                int excessiveCoolantTemperature = (codeThree >> 1) & 0x01;
                if (excessiveCoolantTemperature == 1) {
                    ivExcessiveCoolantTemperature.setImageResource(R.drawable.red_round_icon);
                } else {
                    ivExcessiveCoolantTemperature.setImageResource(R.drawable.grenn_round_icon);
                }

                int lowOilPressuse = (codeThree >> 2) & 0x01;
                if (lowOilPressuse == 1) {
                    ivLowOilPressuse.setImageResource(R.drawable.red_round_icon);
                } else {
                    ivLowOilPressuse.setImageResource(R.drawable.grenn_round_icon);
                }

                int lowFuelVolume = (codeThree >> 3) & 0x01;
                if (lowFuelVolume == 1) {
                    ivLowFuelVolume.setImageResource(R.drawable.red_round_icon);
                } else {
                    ivLowFuelVolume.setImageResource(R.drawable.grenn_round_icon);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_return:{
                finish();
                overridePendingTransition(0, 0);
                break;
            }
        }
    }
}
