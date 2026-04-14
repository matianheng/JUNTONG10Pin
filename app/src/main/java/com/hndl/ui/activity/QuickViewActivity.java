package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.UsbFile;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.core.update.UpdateManager;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.data.DifferenceData;
import com.hndl.ui.receiver.USBDiskReceiver;
import com.hndl.ui.utils.DateUtils;
import com.hndl.ui.utils.LogUtil;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.utils.UsbHelper;
import com.hndl.ui.widget.DownloadDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class  QuickViewActivity extends BaseActivity implements USBDiskReceiver.UsbListener, UsbHelper.DownloadProgressListener{

    private LinearLayout llReturn;
    private ListView listView1;
    private ListView listView2;
    private TextView tvProgramVersion;
    private TextView tvTotalWorkingHours;

    private String[] strName1={"L1","L2","A","P1","P2"};
    private List<Map<String,Object>> listData1=new ArrayList<>();

    private String[] strName2={"R","H","MW","AW","PCT"};
    private List<Map<String,Object>> listData2=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_view_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
        initUsbFile();
        downloadDialog=new DownloadDialog(this, "", "", new DownloadDialog.DownloaDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {

            }
        });
        downloadDialog.setCancelable(false);
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        listView1 = findViewById(R.id.listView1);
        listView2 = findViewById(R.id.listView2);
        tvProgramVersion = findViewById(R.id.tv_program_version);
        tvTotalWorkingHours = findViewById(R.id.tv_total_working_hours);

        llReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public void initData() {
        Map<String,Object> map=new HashMap<>();
        map.put("name","");
        map.put("ad",getString(R.string.ad_name));
        map.put("actual",getString(R.string.actual));
        listData1.add(map);

        map=new HashMap<>();
        map.put("name","");
        map.put("actual",getString(R.string.actual));
        listData2.add(map);

        for (int i=0;i<strName1.length;i++){
            map=new HashMap<>();
            map.put("name",strName1[i]);
            map.put("ad","");
            map.put("actual","");
            listData1.add(map);
        }
        for (int i=0;i<strName2.length;i++){
            map=new HashMap<>();
            map.put("name",strName2[i]);
            map.put("actual","");
            listData2.add(map);
        }

        listView1.setAdapter(adapter1);
        listView2.setAdapter(adapter2);

        //LogUtil.e("XJW",AppData.modelType+"");
        tvProgramVersion.setText(DifferenceData.getModelName(AppData.modelType)+" "+AppUtils.getAppVersionName()+"");
        tvTotalWorkingHours.setText(DateUtils.minuteTotime(AppData.workHours));
    }

    CommonAdapter adapter1 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.quick_view_item, listData1) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            TextView tvAd = viewHolder.getView(R.id.tv_ad);
            TextView tvActual = viewHolder.getView(R.id.tv_actual);

            String name=item.get("name")+"";
            tvName.setText(name);

            String ad=item.get("ad")+"";
            tvAd.setText(ad);

            String actual=item.get("actual")+"";
            tvActual.setText(actual);
        }
    };

    CommonAdapter adapter2 = new CommonAdapter<Map<String, Object>>(
            this, R.layout.quick_view_item, listData2) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            TextView tvName = viewHolder.getView(R.id.tv_name);
            TextView tvAd = viewHolder.getView(R.id.tv_ad);
            TextView tvActual = viewHolder.getView(R.id.tv_actual);

            String name=item.get("name")+"";
            tvName.setText(name);

            tvAd.setVisibility(View.GONE);

            String actual=item.get("actual")+"";
            tvActual.setText(actual);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        canState = 0;
        startRepeatingTask();
    }

    private Handler mHandler = new Handler();
    private int canState = 0;
    private static final int DELAY_TIME_MS = 100; // 延迟时间
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            // 在这里执行你的任务
            if (canState == 0) {
                byte[] data = new byte[]{(byte) 0x43, 0x23, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            } else if (canState == 1) {
                byte[] data = new byte[]{(byte) 0x4F, 0x22, 0x20, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
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
        if (mTask != null) {
            mHandler.removeCallbacks(mTask);
        }
    }

    private String strNumber="";
    private byte[] canData_584;
    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                if (canData[0] == 0x60 && canData[1] == 0x22 && canData[2] == 0x20 && canData[3] == 0x01) {
                    stopRepeatingTask();
                    AppData.modelType=getInteger(canData[4]);
                    MMKVUtils.getInstance().encode("modelType", AppData.modelType);
                    tvProgramVersion.setText(DifferenceData.getModelName(AppData.modelType)+strNumber);
                }else if (canData[0] == 0x60 && canData[1] == 0x23 && canData[2] == 0x20 && canData[3] == 0x01) {
                    int codeFour=getInteger(canData[4]);
                    strNumber="";
//                    if (codeFour==1){
//                        strNumber="-1";
//                    }
                    strNumber+=" "+getInteger(canData[5]);
                    strNumber+="."+getInteger(canData[6]);
                    strNumber+="."+AppData.versionNumber;
                    tvProgramVersion.setText(DifferenceData.getModelName(AppData.modelType)+" "+strNumber);
                    canState=1;
                }
            });
        }
    }

    private byte[] canData_194;
    private byte[] canData_1A4;
    private byte[] canData_1B4;
    private byte[] canData_1C4;
    private byte[] canData_2A4;
    private byte[] canData_294;
    @Override
    public void getCanData_194(byte[] canData) {
        super.getCanData_194(canData);
        if (!Arrays.equals(canData, canData_194)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_194 = canData;
                int codeZero = getInteger(canData[0]) + (getInteger(canData[1]) << 8);
                int codeTwo =  MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                listData1.get(1).put("ad",codeZero+"");
                listData1.get(1).put("actual",MyUtils.getOneDecimal(codeTwo*0.01)+" m");

                int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                int codeSix =  MyUtils.get16Data((byte) canData[6], (byte) canData[7]);
                listData1.get(2).put("ad",codeFour+"");
                listData1.get(2).put("actual",MyUtils.getOneDecimal(codeSix*0.01)+" m");
                adapter1.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void getCanData_1A4(byte[] canData) {
        super.getCanData_1A4(canData);
        if (!Arrays.equals(canData, canData_1A4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_1A4 = canData;
                int codeZero = getInteger(canData[0]) + (getInteger(canData[1]) << 8);
                int codeTwo =  MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                listData1.get(3).put("ad",codeZero+"");
                listData1.get(3).put("actual",MyUtils.getOneDecimal(codeTwo*0.01)+" °");
                adapter1.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void getCanData_1B4(byte[] canData) {
        super.getCanData_1B4(canData);
        if (!Arrays.equals(canData, canData_1B4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_1B4 = canData;
                int codeZero = getInteger(canData[0]) + (getInteger(canData[1]) << 8);
                int codeTwo = MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                listData1.get(4).put("ad",codeZero+"");
                listData1.get(4).put("actual",MyUtils.getOneDecimal(codeTwo*0.01)+" Mpa");

                int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                int codeSix =  MyUtils.get16Data((byte) canData[6], (byte) canData[7]);
                listData1.get(5).put("ad",codeFour+"");
                listData1.get(5).put("actual",MyUtils.getOneDecimal(codeSix*0.01)+" Mpa");
                adapter1.notifyDataSetChanged();
            });
        }
    }

//    @Override
//    public void getCanData_1C4(byte[] canData) {
//        super.getCanData_1C4(canData);
//        if (!Arrays.equals(canData, canData_1C4)) {
//            ThreadUtils.runOnUiThread(() -> {
//                canData_1C4 = canData;
//                int codeZero = getInteger(canData[0]) + (getInteger(canData[1]) << 8);
//                int codeTwo = MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
//                listData.get(6).put("ad",codeZero+"");
//                listData.get(6).put("actual",MyUtils.getOneDecimal(codeTwo*0.01)+" m/s");
//                adapter.notifyDataSetChanged();
//            });
//        }
//    }

    @Override
    public void getCanData_2A4(byte[] canData) {
        super.getCanData_2A4(canData);
        if (!Arrays.equals(canData, canData_2A4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_2A4 = canData;
                int codeZero = MyUtils.get16Data((byte) canData[0], (byte) canData[1]);
                listData2.get(1).put("actual",MyUtils.getOneDecimal(codeZero*0.01)+" m");

                int codeTwo = MyUtils.get16Data((byte) canData[2], (byte) canData[3]);
                listData2.get(2).put("actual",MyUtils.getOneDecimal(codeTwo*0.01)+" m");
                adapter2.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void getCanData_294(byte[] canData) {
        super.getCanData_294(canData);
        if (!Arrays.equals(canData, canData_294)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_294 = canData;
                int codeZero = getInteger(canData[0]) + (getInteger(canData[1]) << 8);
                listData2.get(3).put("actual",MyUtils.getOneDecimal(codeZero*0.01)+" t");

                int codeTwo = getInteger(canData[2]) + (getInteger(canData[3]) << 8);
                listData2.get(4).put("actual",MyUtils.getOneDecimal(codeTwo*0.01)+" t");

                int codeFour = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                double moment_percentage=codeFour*0.01;
                listData2.get(5).put("actual",MyUtils.getOneDecimal(moment_percentage)+"%");
                adapter2.notifyDataSetChanged();
            });
        }
    }

    private UsbHelper usbHelper;
    private ArrayList<UsbFile> usbList;
    private String savePath = Environment.getExternalStorageDirectory().getPath() + "/hndl/";
    private String saveFileName = "";

    /**
     * 初始化 USB文件列表
     */
    private void initUsbFile() {
        usbHelper = new UsbHelper(this, this);
        usbList = new ArrayList<>();
        updateUsbFile(0);
    }

    /**
     * 更新 USB 文件列表
     */
    private void updateUsbFile(int position) {
        //LogUtil.e("XJW","position:"+position);
        UsbMassStorageDevice[] usbMassStorageDevices = usbHelper.getDeviceList();
        if (usbMassStorageDevices.length > 0) {
            //存在USB
            usbList.clear();
            usbList.addAll(usbHelper.readDevice(usbMassStorageDevices[position]));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (usbList != null && usbList.size() > 0) {
                        for (int i = 0; i < usbList.size(); i++) {
                            UsbFile usbFile = usbList.get(i);
                            LogUtil.e("XJW", "getName:" + usbFile.getName());
                            if (usbFile.getName().equals("HNJT.APK")) {
                                File file = new File(savePath);
                                if (!file.exists()) {
                                    file.mkdir();
                                }
                                saveFileName = savePath + usbFile.getName();
                                usbHelper.saveUSbFileToLocal(usbFile, savePath + usbFile.getName(), QuickViewActivity.this);
                                break;
                            }
                        }
                    }
                }
            }).start();
        } else {
            usbList.clear();
        }
    }

    @Override
    public void insertUsb(UsbDevice device_add) {
        if (usbList.size() == 0) {
            updateUsbFile(0);
        }
    }

    @Override
    public void removeUsb(UsbDevice device_remove) {
        updateUsbFile(0);
    }

    @Override
    public void getReadUsbPermission(UsbDevice usbDevice) {

    }

    @Override
    public void failedReadUsb(UsbDevice usbDevice) {

    }

    DownloadDialog downloadDialog;

    int progressUi=0;
    @Override
    public void downloadProgress(int progress) {
        ThreadUtils.runOnUiThread(() -> {
            progressUi=progress;
            downloadDialog.progress_bar.setProgress(progressUi);
            downloadDialog.tv_bfz.setText(progressUi+"%");
            if (progress >= 1){
                downloadDialog.show();
            }
            if (progress >= 100) {
                progressUi=0;
                downloadDialog.dismiss();
                downloadDialog=null;
                //ToastUtils.showShort("成功！");
                //MyUtils.installApk(this, saveFileName);
                UpdateManager.install(saveFileName);
            }
        });
    }

    @Override
    protected void onDestroy() {
        usbHelper.finishUsbHelper();
        stopRepeatingTask();
        super.onDestroy();
    }
}
