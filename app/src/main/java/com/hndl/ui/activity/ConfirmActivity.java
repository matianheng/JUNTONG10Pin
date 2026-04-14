package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.constructSerialFilter;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.model.OverloadRecordModel;
import com.hndl.ui.utils.DatabaseManager;
import com.hndl.ui.utils.LogUtil;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;

import java.util.Arrays;

public class ConfirmActivity extends BaseActivity {

    private TextView btnConfirm;
    private ImageView ivBg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
//        byte[] dataFilter = new byte[]{(byte) 0x30, 0x30,(byte) 0x41, 0x46,(byte) 0x30, 0x30};
//        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x33, dataFilter));
//        dataFilter = new byte[]{(byte) 0x31, 0x30,(byte) 0x41, 0x46,(byte) 0x30, 0x30};
//        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x33, dataFilter));

    }

    @Override
    public void initView() {
        //DatabaseManager.getInstance().delete(OverloadRecordModel.class);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] data = new byte[]{(byte) 0x01, 0x00, 0x00, 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x01, data));
                isConfirm=true;
                if (AppData.modelType==15||AppData.modelType==29){
                    ActivityUtils.startActivity(WeightSelectionActivity.class);
                }else {
                    ActivityUtils.startActivity(MainActivity.class);
                }
                finish();
                overridePendingTransition(0, 0);
            }
        });

        ivBg=findViewById(R.id.iv_bg);
        int languageState= MMKVUtils.getInstance().decodeInt("languageState");
        if (languageState==2){
            ivBg.setImageResource(R.drawable.splash_ru_icon);
        }else {
            ivBg.setImageResource(R.drawable.splash_icon);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ThreadUtils.runOnUiThreadDelayed(() -> {
            AppData.modelType=MMKVUtils.getInstance().decodeInt("modelType");
            initData();
        }, 200);
    }

    @Override
    public void initData() {
        byte[] dataFilter = new byte[]{(byte) 0x30, 0x30, 0x30, 0x31, (byte) 0x31, (byte) 0x34, (byte) 0x30, 0x30,0x32,
                (byte) 0x30, (byte) 0x38, 0x37,0x36,(byte) 0x34, (byte) 0x30, 0x38,0x37,(byte) 0x37, (byte) 0x46, 0x37,0x43};
        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x30, 0x31, 0x30, 0x31, (byte) 0x31, (byte) 0x34, (byte) 0x30, 0x30,0x37,
                (byte) 0x37, (byte) 0x46, 0x37,0x43,(byte) 0x43, (byte) 0x38, 0x36,0x39,(byte) 0x39, (byte) 0x46, 0x46,0x45};
        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x30, 0x32, 0x30, 0x31, (byte) 0x31, (byte) 0x43, (byte) 0x38, 0x45,0x39,
                (byte) 0x39, (byte) 0x46, 0x46,0x45,(byte) 0x00, (byte) 0x00, 0x00,0x00,(byte) 0x00, (byte) 0x00, 0x00,0x00};
        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x31, 0x45, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, 0x31,0x42,
                (byte) 0x30, (byte) 0x30, 0x39,0x33,(byte) 0x30, (byte) 0x30, 0x44,0x33,(byte) 0x30, (byte) 0x30, 0x33,0x35};
        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x31, 0x46, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, 0x35,0x35,
                (byte) 0x30, (byte) 0x30, 0x37,0x35,(byte) 0x30, (byte) 0x30, 0x39,0x35,(byte) 0x30, (byte) 0x30, 0x33,0x33};
        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x31, 0x31, 0x31, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, 0x35,0x33,
                (byte) 0x30, (byte) 0x30, 0x37,0x33,(byte) 0x30, (byte) 0x30, 0x42,0x35,(byte) 0x30, (byte) 0x30, 0x39,0x37};
        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x31, 0x32, 0x31, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, 0x42,0x37,
                (byte) 0x30, (byte) 0x30, 0x44,0x37,(byte) 0x30, (byte) 0x30, 0x46,0x37,(byte) 0x30, (byte) 0x30, 0x31,0x45};
        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x31, 0x33, 0x31, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, 0x46,0x33,
                (byte) 0x30, (byte) 0x30, 0x44,0x39,(byte) 0x30, (byte) 0x30, 0x33,0x45,(byte) 0x30, (byte) 0x30, 0x35,0x34};
        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x31, 0x34, 0x31, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, 0x44,0x39,
                (byte) 0x30, (byte) 0x30, 0x31,0x38,(byte) 0x30, (byte) 0x30, 0x44,0x35,(byte) 0x00, (byte) 0x00, 0x00,0x00};
        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));
    }

//    private byte[] canData_584;
//    @Override
//    public void getCanData_584(byte[] canData) {
//        super.getCanData_584(canData);
//        if (!Arrays.equals(canData, canData_584)) {
//            ThreadUtils.runOnUiThread(() -> {
//                canData_584 = canData;
//                if (canData[0] == 0x60 && canData[1] == 0x22 && canData[2] == 0x20 && canData[3] == 0x01) {
//                    AppData.modelType=getInteger(canData[4]);
//                    MMKVUtils.getInstance().encode("modelType", AppData.modelType);
//                }
//            });
//        }
//    }

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
}
