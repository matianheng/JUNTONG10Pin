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
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.UsbFile;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.model.OverloadRecordModel;
import com.hndl.ui.receiver.USBDiskReceiver;
import com.hndl.ui.server.FileServer;
import com.hndl.ui.utils.CRC16Utils;
import com.hndl.ui.utils.DatabaseManager;
import com.hndl.ui.utils.FileUtils;
import com.hndl.ui.utils.IpGetUtil;
import com.hndl.ui.utils.LogUtil;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.utils.UsbHelper;
import com.hndl.ui.widget.DownloadDialog;
import com.hndl.ui.widget.HintDialog;
import com.hndl.ui.widget.QRCodeDialog;
import com.hndl.ui.widget.SelecteListDialog;
import com.liyu.sqlitetoexcel.SQLiteToExcel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolActivity extends BaseActivity implements View.OnClickListener, USBDiskReceiver.UsbListener, UsbHelper.DownloadProgressListener {

    private LinearLayout llReturn;
    private TextView tvWifi;
    private TextView tvOverloadDataExport;
    private TextView tvFramDataBackup;
    private TextView tvFramDataRestoration;

    private String[] strExport = {AppData.getInstance().getString(R.string.export_from_wiFi), AppData.getInstance().getString(R.string.export_to_USB_drive)};
    private List<Map<String, Object>> listExport = new ArrayList<>();
    private static final int SERVER_PORT = 8080;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tools_layout);
        initView();
        initData();
        initUsbFile();
        downloadDialog = new DownloadDialog(this, "", "", new DownloadDialog.DownloaDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {

            }
        });
        downloadDialog.setCancelable(false);
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvWifi = findViewById(R.id.tv_wifi);
        tvOverloadDataExport = findViewById(R.id.tv_overload_data_export);
        tvFramDataBackup = findViewById(R.id.tv_fram_data_backup);
        tvFramDataRestoration = findViewById(R.id.tv_fram_data_restoration);

        llReturn.setOnClickListener(this);
        tvWifi.setOnClickListener(this);
        tvOverloadDataExport.setOnClickListener(this);
        tvFramDataBackup.setOnClickListener(this);
        tvFramDataRestoration.setOnClickListener(this);
    }

    @Override
    public void initData() {
        for (int i = 0; i < strExport.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("KEY", strExport[i] + "");
            map.put("VLAUE", i);
            listExport.add(map);
        }
    }

    private UsbHelper usbHelper;
    private ArrayList<UsbFile> usbList;
    private String savePath = Environment.getExternalStorageDirectory().getPath() + "/hndl/";

    /**
     * 初始化 USB文件列表
     */
    private void initUsbFile() {
        usbHelper = new UsbHelper(this, this);
        usbList = new ArrayList<>();
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
                    usbHelper.saveSDFileToUsb(new File(savePath + "超载记录.xls"), usbHelper.getCurrentFolder(), ToolActivity.this);
                }
            }).start();
        } else {
            usbList.clear();
        }
    }

    @Override
    protected void onDestroy() {
        usbHelper.finishUsbHelper();
        stopRepeatingTask();
        mHandler = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return: {
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_wifi: {
                ActivityUtils.startActivity(WifiActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_overload_data_export: {
                List<OverloadRecordModel> queryList = DatabaseManager.getInstance().getQueryAll(OverloadRecordModel.class);
                if (!(queryList != null && queryList.size() > 0)) {
                    ToastUtils.showShort(getString(R.string.no_overload_record));
                    return;
                }
                createLoadingDialog("");
                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdir();
                }
                new SQLiteToExcel
                        .Builder(this)
                        .setDataBase(this.getDatabasePath("mydata.db").getPath()) //必须。 小提示：内部数据库可以通过 context.getDatabasePath("internal.db").getPath() 获取。
                        .setTables("overload_model") //可选, 如果不设置，则默认导出全部表。
                        .setOutputPath(savePath) //可选, 如果不设置，默认输出路径为 app ExternalFilesDir。
                        .setOutputFileName("超载记录.xls") //可选, 如果不设置，输出的文件名为 xxx.db.xls。
//                        .setEncryptKey("1234567") //可选，可对导出的文件进行加密。
//                        .setProtectKey("9876543") //可选，可对导出的表格进行只读的保护。
                        .start(new SQLiteToExcel.ExportListener() {
                            @Override
                            public void onStart() {

                            }

                            @Override
                            public void onCompleted(String filePath) {
                                dismissLoadingDialog();
                                SelecteListDialog sld = new SelecteListDialog(ToolActivity.this, listExport, getString(R.string.please_select),
                                        new SelecteListDialog.SelecteListListener() {
                                            @Override
                                            public void onClick(int index) {
                                                int type = (int) listExport.get(index).get("VLAUE");
                                                if (type == 0) {
                                                    startHttpServer();
                                                    String ipStr = IpGetUtil.getIpAddress(ToolActivity.this);
                                                    if (ipStr.equals("")) {
                                                        ToastUtils.showShort(R.string.please_connect_to_the_network);
                                                        return;
                                                    }
                                                    QRCodeDialog qrCodeDialog = new QRCodeDialog(ToolActivity.this);
                                                    qrCodeDialog.setQr("http://" + ipStr + ":8080/files/超载记录.xls");
                                                    qrCodeDialog.setCancelable(false);
                                                    qrCodeDialog.show();
                                                } else if (type == 1) {
                                                    updateUsbFile(0);
                                                }
                                            }
                                        });
                                sld.show();
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        }); // 或者使用 .start() 同步方法。
                break;
            }
            case R.id.tv_fram_data_backup: {
                HintDialog hintDialog=new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
                    @Override
                    public void onClick(boolean isConfirm) {
                        if (isConfirm){
                            createLoadingDialog("");
                            canState = 1;
                            parameterNums = 0;
                            byteList =new ArrayList<>();
                            startRepeatingTask();
                        }
                    }
                });
                hintDialog.setButtonVisibility(3);
                hintDialog.setContent(getString(R.string.whether_to_backup_data));
                hintDialog.setCancelable(false);
                hintDialog.show();
                break;
            }
            case R.id.tv_fram_data_restoration: {
                HintDialog hintDialog=new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
                    @Override
                    public void onClick(boolean isConfirm) {
                        if (isConfirm){
                            parameterStartAddress= MMKVUtils.getInstance().decodeInt("parameterStartAddress");
                            parameterLength= MMKVUtils.getInstance().decodeInt("parameterLength");
                            if (parameterLength==0){
                                ToastUtils.showShort(getString(R.string.no_data_to_restore));
                                return;
                            }
                            File tempFile = new File(savePath + "framData.txt");
                            try {
                                byteList=FileUtils.readFileToList(tempFile);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if (byteList!=null&&byteList.size()>0) {
                                //LogUtil.e("XJW","byteList:"+byteList.size());
                                createLoadingDialog("");
                                canState = 4;
                                parameterNums = 0;
                                startRepeatingTask();
                            }else {
                                ToastUtils.showShort(getString(R.string.no_data_to_restore));
                            }
                        }
                    }
                });
                hintDialog.setButtonVisibility(3);
                hintDialog.setContent(getString(R.string.whether_to_restore_data));
                hintDialog.setCancelable(false);
                hintDialog.show();
                break;
            }
        }
    }

    private Handler mHandler = new Handler();
    private int canState = 0;
    private static final int DELAY_TIME_MS = 100; // 延迟时间
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            // 在这里执行你的任务
            if (canState == 1) {
                byte[] data = new byte[]{(byte) 0x4F, 0x16, 0x10, 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            } else if (canState == 2) {
                byte[] data = new byte[]{(byte) 0x41, 0x16, 0x10, 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                data[4] = (byte) (parameterAddress & 0xff);
                data[5] = (byte) ((parameterAddress >> 8) & 0xff);
                data[6] = (byte) indexLen;
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            }else if (canState == 3) {
                byte[] data = new byte[]{(byte) 0x4B, 0x16, 0x10, 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                data[4] = (byte) (crc16 & 0xff);
                data[5] = (byte) ((crc16 >> 8) & 0xff);
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
                //LogUtil.e("XJW","crc11116:"+crc16);
            }else if (canState == 4) {
                byte[] data = new byte[]{(byte) 0x23, 0x16, 0x10, 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                data[4] = (byte) (parameterStartAddress & 0xff);
                data[5] = (byte) ((parameterStartAddress >> 8) & 0xff);
                data[6] = (byte)  (parameterLength & 0xff);
                data[7] = (byte) ((parameterLength >> 8) & 0xff);
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
            }else if (canState == 5) {
                byte[] data = byteList.get(parameterNums);
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x400+AppData.nodeID, data));
            }else if (canState == 6) {
                byte[] data = new byte[]{(byte) 0x4B, 0x16, 0x10, 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
                data[4] = (byte) (crc16 & 0xff);
                data[5] = (byte) ((crc16 >> 8) & 0xff);
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+AppData.nodeID, data));
                //LogUtil.e("XJW","crc16:"+crc16);
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

    private byte[] canData_584;
    private byte[] canData_4E4;

    private int parameterStartAddress = 0; //参数初始地址
    private int parameterAddress = 0; //参数地址
    private int parameterLength = 0;  //参数长度
    private int parameterNums=0;
    private int indexLen=0;
    private boolean isStop=false;
    private byte[] parameterByte;
    private int index=0;
    private int crc16=0;
    private List<byte[]> byteList =new ArrayList<>();

    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                if (canData[0] == 0x60 && canData[1] == 0x16 && canData[2] == 0x10 && canData[3] == 0x01) {
                    parameterAddress = getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                    parameterStartAddress=parameterAddress;
                    parameterLength = getInteger(canData[6]) + (getInteger(canData[7]) << 8);
                    parameterByte=new byte[parameterLength];
                    parameterNums = parameterNums + 6;
                    if (parameterNums <= parameterLength) {
                        isStop=false;
                        indexLen = 6;
                    } else if (parameterNums > parameterLength) {
                        isStop = true;
                        indexLen = parameterLength % 6;
                    }
                    index=0;
                    canState = 2;
                }else if (canData[0] == 0x60 && canData[1] == 0x16 && canData[2] == 0x10 && canData[3] == 0x05) {
                    if (canState==3){
                        stopRepeatingTask();
                        //LogUtil.e("XJW","canData:"+canData[4]);
                        if (canData[4] == 0x00) {
                            File tempFile = new File(savePath + "framData.txt");
                            //判断文件是否存在，存在就删除
                            if (tempFile.exists()) {
                                tempFile.delete();
                            }
                            try {
                                tempFile.createNewFile();
                                // 写入文件
                                FileUtils.writeListToFile(byteList, tempFile);
                                MMKVUtils.getInstance().encode("parameterStartAddress", parameterStartAddress);
                                MMKVUtils.getInstance().encode("parameterLength", parameterLength);
                                dismissLoadingDialog();
                                ToastUtils.showShort(getString(R.string.success));
                            } catch (IOException e) {
                                dismissLoadingDialog();
                                ToastUtils.showShort(getString(R.string.fail));
                                e.printStackTrace();
                            }
                        }else {
                            dismissLoadingDialog();
                            ToastUtils.showShort(getString(R.string.fail));
                        }
                    }
                }else if (canData[0] == 0x60 && canData[1] == 0x16 && canData[2] == 0x10 && canData[3] == 0x03) {
                    if (canState==4){
                        if (canData[4] == 0x00) {
                            index=0;
                            parameterNums=0;
                            parameterByte=new byte[parameterLength];
                            canState=5;
                        }else {
                            stopRepeatingTask();
                            dismissLoadingDialog();
                            ToastUtils.showShort(getString(R.string.fail));
                        }
                    }
                }else if (canData[0] == 0x60 && canData[1] == 0x16 && canData[2] == 0x10 && canData[3] == 0x04) {
                    if (canState==5){
                        int startAddress=getInteger(canData[4]) + (getInteger(canData[5]) << 8);
                        int address=getInteger(byteList.get(parameterNums)[0])+(getInteger(byteList.get(parameterNums)[1]) << 8);
                        if (startAddress==address){
                            stopRepeatingTask();
                            for (int i=0;i<6;i++){
                                parameterByte[index]=byteList.get(parameterNums)[i+2];
                                index=index+1;
                            }
                            int total=getInteger(canData[6]) + (getInteger(canData[7]) << 8);
                            if (total<parameterLength){
                                parameterNums=parameterNums+1;
                            }else {
                                crc16= CRC16Utils.CRC16_MAXIM(parameterByte);
                                //LogUtil.e("XJW","crc16:"+crc16);
                                canState = 6;
                            }
                            startRepeatingTask();
                        }
                    }
                }else if (canData[0] == 0x60 && canData[1] == 0x16 && canData[2] == 0x10 && canData[3] == 0x06) {
                    if (canState == 6) {
                        stopRepeatingTask();
                        dismissLoadingDialog();
                        //LogUtil.e("XJW","canData:"+canData[4]);
                        if (canData[4] == 0x00) {
                            ToastUtils.showShort(getString(R.string.success));
                        }else {
                            ToastUtils.showShort(getString(R.string.fail));
                        }
                    }
                }
            });
        }
    }

    @Override
    public void getCanData_4E4(byte[] canData) {
        super.getCanData_4E4(canData);
        if (!Arrays.equals(canData, canData_4E4)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_4E4 = canData;
                int startAddress=getInteger(canData[0]) + (getInteger(canData[1]) << 8);
                if (startAddress==parameterAddress){
                    stopRepeatingTask();
                    for (int i=0;i<indexLen;i++){
                        parameterByte[index]=canData[i+2];
                        index=index+1;
                    }
                    byteList.add(canData_4E4);
                    if (isStop){
                        crc16= CRC16Utils.CRC16_MAXIM(parameterByte);
                        LogUtil.e("XJW","1111crc16:"+crc16);
                        canState = 3;
                    }else {
                        parameterAddress = parameterAddress + indexLen;
                        parameterNums = parameterNums + 6;
                        if (parameterNums <= parameterLength) {
                            indexLen = 6;
                        } else if (parameterNums > parameterLength) {
                            isStop = true;
                            indexLen = parameterLength % 6;
                        }
                    }
                    startRepeatingTask();
                }
            });
        }
    }

    private void startHttpServer() {
        Thread thread = new Thread(() -> {
            try {
                FileServer httpServer = new FileServer(SERVER_PORT);
                httpServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.setDaemon(true);
        thread.start();
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

    int progressUi = 0;

    @Override
    public void downloadProgress(int progress) {
        ThreadUtils.runOnUiThread(() -> {
            progressUi = progress;
            downloadDialog.progress_bar.setProgress(progressUi);
            downloadDialog.tv_bfz.setText(progressUi + "%");
            if (progressUi >= 1) {
                downloadDialog.show();
            }
            if (progressUi >= 100) {
                progressUi = 0;
                downloadDialog.dismiss();
                downloadDialog = null;
                ToastUtils.showShort(getString(R.string.success_saved));
            }
        });
    }
}
