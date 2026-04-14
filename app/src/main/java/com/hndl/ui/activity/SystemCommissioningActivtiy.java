package com.hndl.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;

public class SystemCommissioningActivtiy extends BaseActivity implements View.OnClickListener {
    private LinearLayout llReturn;
    private TextView tvAmplitudeDebugging;
    private TextView tvEmptyHookDebugging;
    private TextView tvWeightOne;
    private TextView tvWeightTwo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_commissioning_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvAmplitudeDebugging = findViewById(R.id.tv_amplitude_debugging);
        tvEmptyHookDebugging = findViewById(R.id.tv_empty_hook_debugging);
        tvWeightOne = findViewById(R.id.tv_weight_one);
        tvWeightTwo = findViewById(R.id.tv_weight_two);

        llReturn.setOnClickListener(this);
        tvAmplitudeDebugging.setOnClickListener(this);
        tvEmptyHookDebugging.setOnClickListener(this);
        tvWeightOne.setOnClickListener(this);
        tvWeightTwo.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_return:{
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_amplitude_debugging:{
                ActivityUtils.startActivity(AmplitudeDebuggingActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_empty_hook_debugging:{
                ActivityUtils.startActivity(EmptyHookDebuggingActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_weight_one:{
                Intent intent=new Intent();
                intent.putExtra("TYPE","1");
                intent.setClass(this,WeightActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_weight_two:{
                Intent intent=new Intent();
                intent.putExtra("TYPE","2");
                intent.setClass(this,WeightActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            }
            default:
                break;
        }
    }
}
