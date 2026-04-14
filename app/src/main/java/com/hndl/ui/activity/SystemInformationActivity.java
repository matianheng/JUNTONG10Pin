package com.hndl.ui.activity;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.UsbFile;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.receiver.USBDiskReceiver;
import com.hndl.ui.utils.LogUtil;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.utils.UsbHelper;
import com.hndl.ui.widget.DownloadDialog;

import java.io.File;
import java.util.ArrayList;

public class SystemInformationActivity extends BaseActivity implements USBDiskReceiver.UsbListener, UsbHelper.DownloadProgressListener{
    private LinearLayout llReturn;
    private TextView tvVehicleModel;
    private TextView tvSoftwareVersionNumber;
    private TextView tvManufacturer;
    private TextView tvAfterSalesPhoneNumber;
    private LinearLayout llAdmin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_information_layout);
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

    private int clickCount = 0;
    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvVehicleModel = findViewById(R.id.tv_vehicle_model);
        tvSoftwareVersionNumber = findViewById(R.id.tv_software_version_number);
        tvManufacturer = findViewById(R.id.tv_manufacturer);
        tvAfterSalesPhoneNumber = findViewById(R.id.tv_after_sales_phone_number);
        llAdmin=findViewById(R.id.ll_admin);

        llReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        llAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount++;
                if (clickCount == 5) {
                    // 连续点击5次的逻辑
                    //clickCount = 0; // 重置点击计数
                    AppData.isShowAdmin=true;
                    ToastUtils.showShort(getString(R.string.show_admin));
                }
                // 处理点击事件逻辑
            }
        });
    }

    @Override
    public void initData() {
        tvSoftwareVersionNumber.setText("V"+AppUtils.getAppVersionName());
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
                                usbHelper.saveUSbFileToLocal(usbFile, savePath + usbFile.getName(), SystemInformationActivity.this);
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
                MyUtils.installApk(this, saveFileName);
            }
        });
    }

    @Override
    protected void onDestroy() {
        usbHelper.finishUsbHelper();
        super.onDestroy();
    }
}
