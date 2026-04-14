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

/**
 * 系数设置
 */
public class CoefficienSettingsActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout llReturn;
    private TextView tvSystemSettings;
    private TextView tvAmplitudeDebugging;
    private TextView tvStructureParameters;
    private TextView tvEmptyHookParameters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coefficien_settings_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvSystemSettings = findViewById(R.id.tv_system_settings);
        tvAmplitudeDebugging = findViewById(R.id.tv_amplitude_debugging);
        tvStructureParameters = findViewById(R.id.tv_structure_parameters);
        tvEmptyHookParameters = findViewById(R.id.tv_empty_hook_parameters);

        llReturn.setOnClickListener(this);
        tvSystemSettings.setOnClickListener(this);
        tvAmplitudeDebugging.setOnClickListener(this);
        tvStructureParameters.setOnClickListener(this);
        tvEmptyHookParameters.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return: {
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_system_settings:{
                ActivityUtils.startActivity(SystemSettingsActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_amplitude_debugging: {
                ActivityUtils.startActivity(AmplitudeDebuggingActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_structure_parameters: {
                ActivityUtils.startActivity(StructureParametersActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_empty_hook_parameters: {
                Intent intent=new Intent();
                intent.putExtra("TYPE","0");
                intent.setClass(this,ParametersActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            }
        }
    }
}
