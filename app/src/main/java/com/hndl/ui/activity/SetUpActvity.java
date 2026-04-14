package com.hndl.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.widget.EditDialog;

public class SetUpActvity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private TextView tvBasicWorkingConditions;
    private TextView tvZeroToFullAdjustment;
    private TextView tvSystemCommissioning;
    private TextView tvTimeDate;
    private TextView tvSystemInformation;
    private TextView tvAdministrators;
    private TextView tvOverloadRecord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_up_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvBasicWorkingConditions = findViewById(R.id.tv_basic_working_conditions);
        tvZeroToFullAdjustment = findViewById(R.id.tv_zero_to_full_adjustment);
        tvSystemCommissioning = findViewById(R.id.tv_system_commissioning);
        tvTimeDate = findViewById(R.id.tv_time_date);
        tvSystemInformation = findViewById(R.id.tv_system_information);
        tvAdministrators = findViewById(R.id.tv_administrators);
        tvOverloadRecord= findViewById(R.id.tv_overload_record);

        llReturn.setOnClickListener(this);
        tvBasicWorkingConditions.setOnClickListener(this);
        tvZeroToFullAdjustment.setOnClickListener(this);
        tvSystemCommissioning.setOnClickListener(this);
        tvTimeDate.setOnClickListener(this);
        tvSystemInformation.setOnClickListener(this);
        tvAdministrators.setOnClickListener(this);
        tvOverloadRecord.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AppData.isShowAdmin){
            tvAdministrators.setVisibility(View.VISIBLE);
        }else {
            tvAdministrators.setVisibility(View.GONE);
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
            case R.id.tv_basic_working_conditions:{
                ActivityUtils.startActivity(BasicWorkingConditionsActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_zero_to_full_adjustment:{
                EditDialog editDialog=new EditDialog(this, getString(R.string.password), new EditDialog.HintDialogListener() {
                    @Override
                    public void onClick(boolean isConfirm, String string) {
                        if (isConfirm){
                            if (string.equals("5566")){
                                ActivityUtils.startActivity(ZeroToFullAdjustmentActivity.class);
                                overridePendingTransition(0, 0);
                            }else {
                                ToastUtils.showShort(getString(R.string.password_error));
                            }
                        }
                    }
                });
                editDialog.show();
                break;
            }
            case R.id.tv_system_commissioning:{
                EditDialog editDialog=new EditDialog(this, getString(R.string.password), new EditDialog.HintDialogListener() {
                    @Override
                    public void onClick(boolean isConfirm, String string) {
                        if (isConfirm){
                            if (string.equals("5566")){
                                ActivityUtils.startActivity(SystemCommissioningActivtiy.class);
                                overridePendingTransition(0, 0);
                            }else {
                                ToastUtils.showShort(getString(R.string.password_error));
                            }
                        }
                    }
                });
                editDialog.show();
                break;
            }
            case R.id.tv_overload_record:{
                EditDialog editDialog=new EditDialog(this, getString(R.string.password), new EditDialog.HintDialogListener() {
                    @Override
                    public void onClick(boolean isConfirm, String string) {
                        if (isConfirm){
                            if (string.equals("5566")){
                                ActivityUtils.startActivity(OverloadRecordActivity.class);
                                overridePendingTransition(0, 0);
                            }else {
                                ToastUtils.showShort(getString(R.string.password_error));
                            }
                        }
                    }
                });
                editDialog.show();
                break;
            }
            case R.id.tv_time_date:{
                EditDialog editDialog=new EditDialog(this, getString(R.string.password), new EditDialog.HintDialogListener() {
                    @Override
                    public void onClick(boolean isConfirm, String string) {
                        if (isConfirm){
                            if (string.equals("5566")){
                                ActivityUtils.startActivity(TimeDateActivity.class);
                                overridePendingTransition(0, 0);
                            }else {
                                ToastUtils.showShort(getString(R.string.password_error));
                            }
                        }
                    }
                });
                editDialog.show();
                break;
            }
            case R.id.tv_system_information:{
                ActivityUtils.startActivity(SystemInformationActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_administrators:{
                int pass= (int) ((Math.random() * 9 + 2) * 1000);
                EditDialog editDialog=new EditDialog(this, pass+"", new EditDialog.HintDialogListener() {
                    @Override
                    public void onClick(boolean isConfirm, String string) {
                        if (isConfirm){
                            if (string.equals((pass/2)+"")){
                                ActivityUtils.startActivity(AdministratorsActivity.class);
                                overridePendingTransition(0, 0);
                            }else {
                                ToastUtils.showShort(getString(R.string.password_error));
                            }
                        }
                    }
                });
                editDialog.show();
//                ActivityUtils.startActivity(AdministratorsActivity.class);
//                overridePendingTransition(0, 0);
                break;
            }
            default:
                break;
        }
    }
}
