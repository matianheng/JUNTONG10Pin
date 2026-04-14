package com.hndl.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;

/**
 * 重载标定
 */
public class OverloadCalibrationActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private TextView tvWeightOne;
    private TextView tvWeightTwo;
    private TextView tvWeightOneParameters;
    private TextView tvWeightTwoParameters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overload_calibration_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvWeightOne = findViewById(R.id.tv_weight_one);
        tvWeightTwo = findViewById(R.id.tv_weight_two);
        tvWeightOneParameters = findViewById(R.id.tv_weight_one_parameters);
        tvWeightTwoParameters = findViewById(R.id.tv_weight_two_parameters);

        llReturn.setOnClickListener(this);
        tvWeightOne.setOnClickListener(this);
        tvWeightTwo.setOnClickListener(this);
        tvWeightOneParameters.setOnClickListener(this);
        tvWeightTwoParameters.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_return:{
                finish();
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
            case R.id.tv_weight_one_parameters:{
                Intent intent=new Intent();
                intent.putExtra("TYPE","1");
                intent.setClass(this,ParametersActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_weight_two_parameters:{
                Intent intent=new Intent();
                intent.putExtra("TYPE","2");
                intent.setClass(this,ParametersActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            }
            default:
                break;
        }
    }
}
