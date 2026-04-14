package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.sdk.kb.AutoKeyboardEditText;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.widget.HintDialog;

import java.util.ArrayList;
import java.util.List;

public class SensorCalibrateActivity extends BaseActivity implements View.OnClickListener {


    private LinearLayout llReturn;
    private AutoKeyboardEditText etAdZeroL1;
    private AutoKeyboardEditText etZeroL1;
    private AutoKeyboardEditText etAdFullL1;
    private AutoKeyboardEditText etFullL1;
    private View xianL2;
    private LinearLayout llL2;
    private AutoKeyboardEditText etAdZeroL2;
    private AutoKeyboardEditText etZeroL2;
    private AutoKeyboardEditText etAdFullL2;
    private AutoKeyboardEditText etFullL2;
    private AutoKeyboardEditText etAdZeroA;
    private AutoKeyboardEditText etZeroA;
    private AutoKeyboardEditText etAdFullA;
    private AutoKeyboardEditText etFullA;
    private AutoKeyboardEditText etAdZeroP1;
    private AutoKeyboardEditText etZeroP1;
    private AutoKeyboardEditText etAdFullP1;
    private AutoKeyboardEditText etFullP1;
    private AutoKeyboardEditText etAdZeroP2;
    private AutoKeyboardEditText etZeroP2;
    private AutoKeyboardEditText etAdFullP2;
    private AutoKeyboardEditText etFullP2;
    private TextView btnSave;
    private boolean isL2=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_calibrate_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
        hintDialog=new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm&&hint==1){
                    mModifyList.clear();
                    mModifyList = constructModifyList();

                    //LogUtils.e("----------------test------------modifyList-----"+mModifyList.size());
                    if (mModifyList.size() > 0){
                        createLoadingDialog("");

                        sendCmdModifySensorZeroAndFullValue();
                    }else{
                        ToastUtils.showShort("UnModify");
                    }
                }else if (hint==2){
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopSendCmdGetSensorZeroAndFullValue();
        stopSendCmdModifySensorZeroAndFullValue();
    }

    private String curEtAdZeroL1Value = "";
    private String curEtZeroL1Value = "";
    private String curEtAdFullL1Value = "";
    private String curEtFullL1Value = "";

    private String curEtAdZeroL2Value = "";
    private String curEtZeroL2Value = "";
    private String curEtAdFullL2Value = "";
    private String curEtFullL2Value = "";

    private String curEtAdZeroAValue = "";
    private String curEtZeroAValue = "";
    private String curEtAdFullAValue = "";
    private String curEtFullAValue = "";

    private String curEtAdZeroP1Value = "";
    private String curEtZeroP1Value = "";
    private String curEtAdFullP1Value = "";
    private String curEtFullP1Value = "";

    private String curEtAdZeroP2Value = "";
    private String curEtZeroP2Value = "";
    private String curEtAdFullP2Value = "";
    private String curEtFullP2Value = "";

    private List<byte[]> mModifyList = new ArrayList<>();
    private List<byte[]> constructModifyList(){
        List<byte[]> list = new ArrayList<>();

        // 判断 L1 的零点值
        String strAdZeroL1 = etAdZeroL1.getText().toString();
        String strZeroL1 = etZeroL1.getText().toString();
        if ((!strAdZeroL1.equals("") && !curEtAdZeroL1Value.equals(strAdZeroL1)) || (!strZeroL1.equals("") && !curEtZeroL1Value.equals(strZeroL1))){
            byte[] dataL1 = new byte[]{(byte) 0x23, 0x13, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            int s = (int) (Integer.parseInt(strAdZeroL1));
            dataL1[4] = (byte) (s & 0xff);
            dataL1[5] = (byte) ((s >> 8) & 0xff);

            s = (int) (Double.parseDouble(strZeroL1)*100);
            dataL1[6] = (byte) (s & 0xff);
            dataL1[7] = (byte) ((s >> 8) & 0xff);

            list.add(dataL1);
        }

        if (isL2) {
            // 判断 L2 的零点值
            String strAdZeroL2 = etAdZeroL2.getText().toString();
            String strZeroL2 = etZeroL2.getText().toString();
            if ((!strAdZeroL2.equals("") && !curEtAdZeroL2Value.equals(strAdZeroL2)) || (!strZeroL2.equals("") && !curEtZeroL2Value.equals(strZeroL2))) {
                byte[] dataL2 = new byte[]{(byte) 0x23, 0x13, 0x20, 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                int s = (int) (Integer.parseInt(strAdZeroL2));
                dataL2[4] = (byte) (s & 0xff);
                dataL2[5] = (byte) ((s >> 8) & 0xff);

                s = (int) (Double.parseDouble(strZeroL2) * 100);
                dataL2[6] = (byte) (s & 0xff);
                dataL2[7] = (byte) ((s >> 8) & 0xff);

                list.add(dataL2);
            }
        }

        // 判断 A 的零点值
        String strAdZeroA = etAdZeroA.getText().toString();
        String strZeroA = etZeroA.getText().toString();
        if ((!strAdZeroA.equals("") && !curEtAdZeroAValue.equals(strAdZeroA)) || (!strZeroA.equals("") && !curEtZeroAValue.equals(strZeroA))){
            byte[] dataA = new byte[]{(byte) 0x23, 0x13, 0x20, 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            int s = (int) (Integer.parseInt(strAdZeroA));
            dataA[4] = (byte) (s & 0xff);
            dataA[5] = (byte) ((s >> 8) & 0xff);

            s = (int) (Double.parseDouble(strZeroA)*100);
            dataA[6] = (byte) (s & 0xff);
            dataA[7] = (byte) ((s >> 8) & 0xff);

            list.add(dataA);
        }

        // 判断 P1 的零点值
        String strAdZeroP1 = etAdZeroP1.getText().toString();
        String strZeroP1 = etZeroP1.getText().toString();
        if ((!strAdZeroP1.equals("") && !curEtAdZeroP1Value.equals(strAdZeroP1)) || (!strZeroP1.equals("") && !curEtZeroP1Value.equals(strZeroP1))){
            byte[] dataP1 = new byte[]{(byte) 0x23, 0x13, 0x20, 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            int s = (int) (Integer.parseInt(strAdZeroP1));
            dataP1[4] = (byte) (s & 0xff);
            dataP1[5] = (byte) ((s >> 8) & 0xff);

            s = (int) (Double.parseDouble(strZeroP1)*100);
            dataP1[6] = (byte) (s & 0xff);
            dataP1[7] = (byte) ((s >> 8) & 0xff);

            list.add(dataP1);
        }

        // 判断 P2 的零点值
        String strAdZeroP2 = etAdZeroP2.getText().toString();
        String strZeroP2 = etZeroP2.getText().toString();
        if ((!strAdZeroP2.equals("") && !curEtAdZeroP2Value.equals(strAdZeroP2)) || (!strZeroP2.equals("") && !curEtZeroP2Value.equals(strZeroP2))){
            byte[] dataP2 = new byte[]{(byte) 0x23, 0x13, 0x20, 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            int s = (int) (Integer.parseInt(strAdZeroP2));
            dataP2[4] = (byte) (s & 0xff);
            dataP2[5] = (byte) ((s >> 8) & 0xff);

            s = (int) (Double.parseDouble(strZeroP2)*100);
            dataP2[6] = (byte) (s & 0xff);
            dataP2[7] = (byte) ((s >> 8) & 0xff);

            list.add(dataP2);
        }

        // 判断 L1 的满点值
        String strAdFullL1 = etAdFullL1.getText().toString();
        String strFullL1= etFullL1.getText().toString();
        if ((!strAdFullL1.equals("") && !curEtAdFullL1Value.equals(strAdFullL1)) || (!strFullL1.equals("") && !curEtFullL1Value.equals(strFullL1))){
            byte[] dataL1 = new byte[]{(byte) 0x23, 0x14, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            int s = (int) (Integer.parseInt(strAdFullL1));
            dataL1[4] = (byte) (s & 0xff);
            dataL1[5] = (byte) ((s >> 8) & 0xff);

            s = (int) (Double.parseDouble(strFullL1)*100);
            dataL1[6] = (byte) (s & 0xff);
            dataL1[7] = (byte) ((s >> 8) & 0xff);

            list.add(dataL1);
        }

        // 判断 L2 的满点值
        if (isL2) {
            String strAdFullL2 = etAdFullL2.getText().toString();
            String strFullL2 = etFullL2.getText().toString();
            if ((!strAdFullL2.equals("") && !curEtAdFullL2Value.equals(strAdFullL2)) || (!strFullL2.equals("") && !curEtFullL2Value.equals(strFullL2))) {
                byte[] dataL2 = new byte[]{(byte) 0x23, 0x14, 0x20, 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                int s = (int) (Integer.parseInt(strAdFullL2));
                dataL2[4] = (byte) (s & 0xff);
                dataL2[5] = (byte) ((s >> 8) & 0xff);

                s = (int) (Double.parseDouble(strFullL2) * 100);
                dataL2[6] = (byte) (s & 0xff);
                dataL2[7] = (byte) ((s >> 8) & 0xff);

                list.add(dataL2);
            }
        }

        // 判断 A 的满点值
        String strAdFullA = etAdFullA.getText().toString();
        String strFullA= etFullA.getText().toString();
        if ((!strAdFullA.equals("") && !curEtAdFullAValue.equals(strAdFullA)) || (!strFullA.equals("") && !curEtFullAValue.equals(strFullA))){
            byte[] dataA = new byte[]{(byte) 0x23, 0x14, 0x20, 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            int s = (int) (Integer.parseInt(strAdFullA));
            dataA[4] = (byte) (s & 0xff);
            dataA[5] = (byte) ((s >> 8) & 0xff);

            s = (int) (Double.parseDouble(strFullA)*100);
            dataA[6] = (byte) (s & 0xff);
            dataA[7] = (byte) ((s >> 8) & 0xff);

            list.add(dataA);
        }

        // 判断 P1 的满点值
        String strAdFullP1 = etAdFullP1.getText().toString();
        String strFullP1= etFullP1.getText().toString();
        if ((!strAdFullP1.equals("") && !curEtAdFullP1Value.equals(strAdFullP1)) || (!strFullP1.equals("") && !curEtFullP1Value.equals(strFullP1))){
            byte[] dataP1 = new byte[]{(byte) 0x23, 0x14, 0x20, 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            int s = (int) (Integer.parseInt(strAdFullP1));
            dataP1[4] = (byte) (s & 0xff);
            dataP1[5] = (byte) ((s >> 8) & 0xff);

            s = (int) (Double.parseDouble(strFullP1)*100);
            dataP1[6] = (byte) (s & 0xff);
            dataP1[7] = (byte) ((s >> 8) & 0xff);

            list.add(dataP1);
        }

        // 判断 P2 的满点值
        String strAdFullP2 = etAdFullP2.getText().toString();
        String strFullP2= etFullP2.getText().toString();
        if ((!strAdFullP2.equals("") && !curEtAdFullP2Value.equals(strAdFullP2)) || (!strFullP2.equals("") && !curEtFullP2Value.equals(strFullP2))){
            byte[] dataP2 = new byte[]{(byte) 0x23, 0x14, 0x20, 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            int s = (int) (Integer.parseInt(strAdFullP2));
            dataP2[4] = (byte) (s & 0xff);
            dataP2[5] = (byte) ((s >> 8) & 0xff);

            s = (int) (Double.parseDouble(strFullP2)*100);
            dataP2[6] = (byte) (s & 0xff);
            dataP2[7] = (byte) ((s >> 8) & 0xff);

            list.add(dataP2);
        }

        return list;
    }

    private Handler mModifyHandler = new Handler();
    private Runnable mModifyTask = null;
    private void sendCmdModifySensorZeroAndFullValue(){
        /**
         * 更改了修改传感器零点和满点值的方式
         *   1、一个一个值获取
         *   2、若单个值没获取到，则该值会持续发送数据
         *   3、接收到数据后，会停止发送上一个数据
         */
        if (mModifyTask != null){
            mModifyHandler.removeCallbacks(mModifyTask);
        }

        if (mModifyList!=null&&mModifyList.size()==0){
            LogUtils.e("------test ---sendCmdModifySensorZeroAndFullValue--"+mModifyList.size());
            mModifyHandler.removeCallbacks(mModifyTask);
            mModifyTask=null;
            //如果全部发送完，则发送保存命令
            byte[] data = new byte[]{(byte) 0x23, 0x10, 0x10, 0x01, (byte) 0x73, (byte) 0x61, (byte) 0x76, 0x65};
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));

            return;
        }


        mModifyTask = new Runnable() {
            @Override
            public void run() {
                // 每次发送待修改列表的第一条
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, mModifyList.get(0)));

                mModifyHandler.postDelayed(mModifyTask, 100);
            }
        };
        mModifyHandler.post(mModifyTask);
    }
    private void stopSendCmdModifySensorZeroAndFullValue(){
        if (mModifyTask != null){
            mModifyHandler.removeCallbacks(mModifyTask);
        }
    }





    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        etAdZeroL1 = findViewById(R.id.et_ad_zero_l1);
        etZeroL1 = findViewById(R.id.et_zero_l1);
        etAdFullL1 = findViewById(R.id.et_ad_full_l1);
        etFullL1 = findViewById(R.id.et_full_l1);
        xianL2 = findViewById(R.id.xian_l2);
        llL2 = findViewById(R.id.ll_l2);
        etAdZeroL2 = findViewById(R.id.et_ad_zero_l2);
        etZeroL2 = findViewById(R.id.et_zero_l2);
        etAdFullL2 = findViewById(R.id.et_ad_full_l2);
        etFullL2 = findViewById(R.id.et_full_l2);
        etAdZeroA = findViewById(R.id.et_ad_zero_a);
        etZeroA = findViewById(R.id.et_zero_a);
        etAdFullA = findViewById(R.id.et_ad_full_a);
        etFullA = findViewById(R.id.et_full_a);
        etAdZeroP1 = findViewById(R.id.et_ad_zero_p1);
        etZeroP1 = findViewById(R.id.et_zero_p1);
        etAdFullP1 = findViewById(R.id.et_ad_full_p1);
        etFullP1 = findViewById(R.id.et_full_p1);
        etAdZeroP2 = findViewById(R.id.et_ad_zero_p2);
        etZeroP2 = findViewById(R.id.et_zero_p2);
        etAdFullP2 = findViewById(R.id.et_ad_full_p2);
        etFullP2 = findViewById(R.id.et_full_p2);
        btnSave = findViewById(R.id.btn_save);

        llReturn.setOnClickListener(this);
        if (AppData.modelType==6||AppData.modelType==8||AppData.modelType==10){
            isL2=true;
            xianL2.setVisibility(View.VISIBLE);
            llL2.setVisibility(View.VISIBLE);
        }else {
            isL2=false;
            xianL2.setVisibility(View.GONE);
            llL2.setVisibility(View.GONE);
        }
    }


    @Override
    public void initData() {
        // 单个值获取方式， modify by chenyaoli，2023.12.28
        sendCmdGetSensorZeroAndFullValue((byte) 0x13, (byte)0x01);
//        for (int i=0;i<3;i++){
//            byte[] data = new byte[]{(byte) 0x43, 0x13, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
//            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x604, data));
//
//            data = new byte[]{(byte) 0x43, 0x13, 0x20, 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
//            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x604, data));
//
//            data = new byte[]{(byte) 0x43, 0x13, 0x20, 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
//            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x604, data));
//
//            data = new byte[]{(byte) 0x43, 0x13, 0x20, 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
//            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x604, data));
//        }
//        for (int i=0;i<3;i++){
//            byte[] data = new byte[]{(byte) 0x43, 0x14, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
//            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x604, data));
//            data = new byte[]{(byte) 0x43, 0x14, 0x20, 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
//            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x604, data));
//            data = new byte[]{(byte) 0x43, 0x14, 0x20, 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
//            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x604, data));
//            data = new byte[]{(byte) 0x43, 0x14, 0x20, 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
//            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x604, data));
//        }
    }
    private Handler mHandler = new Handler();
    private Runnable mTask = null;
    private void sendCmdGetSensorZeroAndFullValue(byte index, byte sub_index){
        /**
         * 更改了获取传感器零点和满点值的方式
         *   1、一个一个值获取
         *   2、若单个值没获取到，则该值会持续发送数据
         *   3、接收到数据后，会停止发送上一个数据
         */
        if (mTask != null){
            mHandler.removeCallbacks(mTask);
        }

        mTask = new Runnable() {
            @Override
            public void run() {
                byte[] data = new byte[]{(byte) 0x43, 0x13, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                data[1] = index;
                data[3] = sub_index;
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));

                mHandler.postDelayed(mTask, 100);
            }
        };
        mHandler.post(mTask);
    }
    private void stopSendCmdGetSensorZeroAndFullValue(){
        if (mTask != null){
            mHandler.removeCallbacks(mTask);
        }
    }

    private byte[] canData_584;
    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
//        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
//                LogUtils.e("---------------------test------------"+MyUtils.byteArrayToHexStr(canData_584));
                if (hint == 0){
                    if (canData[0]==0x60&&canData[1]==0x13&&canData[2]==0x20){
                        if (canData[3]==0x01){
                            int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                            int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                            curEtAdZeroL1Value = codeFour+"";
                            etAdZeroL1.setText(curEtAdZeroL1Value);
                            curEtZeroL1Value = MyUtils.getOneDecimal(codeSix*0.01)+"";
                            etZeroL1.setText(curEtZeroL1Value);
                            // 获取下一个数据
                            if (isL2){
                                sendCmdGetSensorZeroAndFullValue((byte) 0x13, (byte)0x02);
                            }else {
                                sendCmdGetSensorZeroAndFullValue((byte) 0x13, (byte)0x03);
                            }
                        }else if (canData[3]==0x02){
                            int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                            int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                            curEtAdZeroL2Value = codeFour+"";
                            etAdZeroL2.setText(curEtAdZeroL2Value);
                            curEtZeroL2Value = MyUtils.getOneDecimal(codeSix*0.01)+"";
                            etZeroL2.setText(curEtZeroL2Value);
                            // 获取下一个数据
                            sendCmdGetSensorZeroAndFullValue((byte) 0x13, (byte)0x03);
                        }else if (canData[3]==0x03){
                            int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                            int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                            curEtAdZeroAValue = codeFour+"";
                            etAdZeroA.setText(curEtAdZeroAValue);
                            curEtZeroAValue = MyUtils.getOneDecimal(codeSix*0.01)+"";
                            etZeroA.setText(curEtZeroAValue);
                            // 获取下一个数据
                            sendCmdGetSensorZeroAndFullValue((byte) 0x13, (byte)0x05);

                        }else if (canData[3]==0x05){
                            int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                            int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                            curEtAdZeroP1Value = codeFour+"";
                            etAdZeroP1.setText(curEtAdZeroP1Value);
                            curEtZeroP1Value = MyUtils.getOneDecimal(codeSix*0.01)+"";
                            etZeroP1.setText(curEtZeroP1Value);
                            // 获取下一个数据
                            sendCmdGetSensorZeroAndFullValue((byte) 0x13, (byte)0x06);

                        }else if (canData[3]==0x06){
                            int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                            int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                            curEtAdZeroP2Value = codeFour+"";
                            etAdZeroP2.setText(curEtAdZeroP2Value);
                            curEtZeroP2Value = MyUtils.getOneDecimal(codeSix*0.01)+"";
                            etZeroP2.setText(curEtZeroP2Value);
                            // 获取下一个数据
                            sendCmdGetSensorZeroAndFullValue((byte) 0x14, (byte)0x01);

                        }
                    }else if (canData[0]==0x60&&canData[1]==0x14&&canData[2]==0x20){
                        if (canData[3]==0x01){
                            int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                            int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                            curEtAdFullL1Value = codeFour+"";
                            etAdFullL1.setText(curEtAdFullL1Value);
                            curEtFullL1Value = MyUtils.getOneDecimal(codeSix*0.01)+"";
                            etFullL1.setText(curEtFullL1Value);
                            // 获取下一个数据
                            if (isL2) {
                                sendCmdGetSensorZeroAndFullValue((byte) 0x14, (byte) 0x02);
                            }else {
                                sendCmdGetSensorZeroAndFullValue((byte) 0x14, (byte) 0x03);
                            }
                        }else if (canData[3]==0x02){
                            int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                            int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                            curEtAdFullL2Value = codeFour+"";
                            etAdFullL2.setText(curEtAdFullL2Value);
                            curEtFullL2Value = MyUtils.getOneDecimal(codeSix*0.01)+"";
                            etFullL2.setText(curEtFullL2Value);
                            // 获取下一个数据
                            sendCmdGetSensorZeroAndFullValue((byte) 0x14, (byte)0x03);
                        }else if (canData[3]==0x03){
                            int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                            int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                            curEtAdFullAValue = codeFour+"";
                            etAdFullA.setText(curEtAdFullAValue);
                            curEtFullAValue = MyUtils.getOneDecimal(codeSix*0.01)+"";
                            etFullA.setText(curEtFullAValue);
                            // 获取下一个数据
                            sendCmdGetSensorZeroAndFullValue((byte) 0x14, (byte)0x05);

                        }else if (canData[3]==0x05){
                            int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                            int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                            curEtAdFullP1Value = codeFour+"";
                            etAdFullP1.setText(curEtAdFullP1Value);
                            curEtFullP1Value = MyUtils.getOneDecimal(codeSix*0.01)+"";
                            etFullP1.setText(curEtFullP1Value);
                            // 获取下一个数据
                            sendCmdGetSensorZeroAndFullValue((byte) 0x14, (byte)0x06);
                        }else if (canData[3]==0x06){
                            int codeFour = getInteger(canData[4])+ (getInteger(canData[5]) << 8);
                            int codeSix= getInteger(canData[6])+ (getInteger(canData[7]) << 8);
                            curEtAdFullP2Value = codeFour+"";
                            etAdFullP2.setText(curEtAdFullP2Value);
                            curEtFullP2Value = MyUtils.getOneDecimal(codeSix*0.01)+"";
                            etFullP2.setText(curEtFullP2Value);
                            // 获取最后一个数据后，停止获取
                            stopSendCmdGetSensorZeroAndFullValue();
                            btnSave.setBackgroundResource(R.drawable.blue_bg_circle);
                            btnSave.setOnClickListener(this);
                        }
                    }
                }else if (hint == 1){
                    if (canData[0]==0x60&&(canData[1]==0x13 || canData[1]==0x14)&&canData[2]==0x20){
                        // 只有发送了所有需修改的数据，并收到回复后，才触发保存指令. add by chenyaoli,2023.12.29

                        // 收到成功的指令，删除第一个待修改项，则执行下个修改的命令
                        if (mModifyList.size()!=0) {
                            mModifyList.remove(0);
                            sendCmdModifySensorZeroAndFullValue();
                        }
                    }else if (canData[0]==0x60&&canData[1]==0x10&&canData[2]==0x10&&canData[3]==0x01){
                        dismissLoadingDialog();
                        showDialog(-1,2,getString(R.string.success_saved));
                    }
                }
            });
//        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_return:{
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.btn_save:{
                showDialog(1,3,getString(R.string.save_or_not));
                break;
            }
        }
    }

    private int hint = 0;
    private HintDialog hintDialog;
    private void showDialog(int h, int type, String content) {
        hint = h;
        hintDialog.setButtonVisibility(type);
        hintDialog.setContent(content);
        hintDialog.setCancelable(false);
        hintDialog.show();
    }
}
