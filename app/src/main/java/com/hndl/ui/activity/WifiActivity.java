package com.hndl.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.AdbUtils;
import com.hndl.ui.utils.IpGetUtil;
import com.hndl.ui.widget.HintDialog;
import com.hndl.ui.widget.WifiEditDialog;
import com.iwdael.wifimanager.IWifi;
import com.iwdael.wifimanager.IWifiManager;
import com.iwdael.wifimanager.OnWifiChangeListener;
import com.iwdael.wifimanager.OnWifiConnectListener;
import com.iwdael.wifimanager.OnWifiStateChangeListener;
import com.iwdael.wifimanager.State;
import com.iwdael.wifimanager.WifiManager;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WifiActivity extends BaseActivity implements View.OnClickListener, OnWifiChangeListener, OnWifiConnectListener, OnWifiStateChangeListener {

    private LinearLayout llReturn;
    private LinearLayout llWlan;
    private Switch switchWifi;
    private LinearLayout llWlanShow;
    private LinearLayout llWifi;
    private SmartRefreshLayout refreshLayout;
    private ListView listWifi;
    private AVLoadingIndicatorView avi;
    private TextView tvIp;

    private boolean isWlanStatus = false;

    private IWifiManager iWifiManager;
    private List<IWifi> wifiList = new ArrayList<>();
    private List<IWifi> iWifiList = new ArrayList<>();

    private IWifi connectingWifi;

    private int number=0;
    private Timer timerDate = new Timer();
    private TimerTask taskDate;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                number=number++;
                if (connectingWifi != null) {
                    if (connectingWifi.isSaved()||connectingWifi.isConnected()) {
                        if (connectingWifi.isConnected()) {
                            ToastUtils.showShort(getString(R.string.successfully_connected));
                        }else {
                            iWifiManager.removeWifi(connectingWifi);
                            iWifiManager.disConnectWifi();
                            ToastUtils.showShort(getString(R.string.password_error));
                        }
                        connectingWifi = null;
                        taskDate.cancel();
                        timerDate.cancel();
                    } else {
                        if (number==10) {
                            ToastUtils.showShort(getString(R.string.connection_failed));
                            connectingWifi = null;
                            taskDate.cancel();
                            timerDate.cancel();
                        }
                    }
                }
                avi.setVisibility(View.VISIBLE);
                iWifiManager.scanWifi();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_layout);
        initView();
        initData();
    }

    @Override
    public void initView() {
        iWifiManager = WifiManager.create(this);
        iWifiManager.setOnWifiChangeListener(this);
        iWifiManager.setOnWifiConnectListener(this);
        iWifiManager.setOnWifiStateChangeListener(this);

        llReturn = findViewById(R.id.ll_return);
        llWlan = findViewById(R.id.ll_wlan);
        switchWifi = findViewById(R.id.switch_wifi);
        llWlanShow = findViewById(R.id.ll_wlan_show);
        llWifi = findViewById(R.id.ll_wifi);
        refreshLayout = findViewById(R.id.refreshLayout);
        listWifi = findViewById(R.id.list_wifi);
        avi = findViewById(R.id.avi);
        tvIp=findViewById(R.id.tv_ip);

        llWlan.setOnClickListener(this);
        switchWifi.setOnClickListener(this);
        tvIp.setOnClickListener(this);
        llReturn.setOnClickListener(this);
        adapter = new CommonAdapter<IWifi>(
                this, R.layout.wifi_set_item, wifiList) {
            @Override
            protected void convert(ViewHolder viewHolder,
                                   final IWifi item, final int position) {
                TextView tvWifiName = viewHolder.getView(R.id.tv_wifi_name);
                TextView tvWifiState = viewHolder.getView(R.id.tv_wifi_state);
                ImageView ivWifi = viewHolder.getView(R.id.iv_wifi);
                String title = item.name();
                if (title == null || title.equals("")) {
                    title = getString(R.string.unknown_network_hidden_network);
                }
                tvWifiName.setText(title);
                ivWifi.setImageResource(getLevelIcon(calculateSignalLevel(item.level(), 100)));
                if (item.isEncrypt()) {
                    tvWifiState.setText(getString(R.string.encryption));
                } else {
                    tvWifiState.setText(getString(R.string.openness));
                }
            }
        };
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                try {
                    avi.setVisibility(View.VISIBLE);
                    iWifiManager.scanWifi();
                } catch (Exception e) {
                    //ToastUtils.showShort("wifi搜索失败，请稍后重试！");
                }
                refreshlayout.finishRefresh(1200);
            }
        });
        listWifi.setAdapter(adapter);
        listWifi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                IWifi iWifi = wifiList.get(i);
                connectingWifi = iWifi;
                if (iWifi.isEncrypt()) {
                    WifiEditDialog wifiEditDialog = new WifiEditDialog(WifiActivity.this, iWifi.name(), new WifiEditDialog.WifiEditListener() {
                        @Override
                        public void onClick(boolean isConfirm, String string) {
                            if (isConfirm) {
                                iWifiManager.connectEncryptWifi(iWifi, string);
                                connecDate();
                            }
                        }
                    });
                    wifiEditDialog.show();
                } else {
                    iWifiManager.connectOpenWifi(iWifi);
                    connecDate();
                }
            }
        });
    }

    @Override
    public void initData() {
        isWlanStatus = iWifiManager.isOpened();
        if (isWlanStatus) {
            switchWifi.setChecked(true);
            llWlanShow.setVisibility(View.VISIBLE);
            avi.setVisibility(View.VISIBLE);
            iWifiManager.scanWifi();
        } else {
            switchWifi.setChecked(false);
            llWlanShow.setVisibility(View.GONE);
        }
    }

    private void connecDate(){
        number=0;
        timerDate = new Timer();
        taskDate = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 100;
                handler.sendMessage(message);
            }
        };
        timerDate.schedule(taskDate, 1000, 1000);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return:{
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.switch_wifi: {
            }
            case R.id.ll_wlan: {
                if (!isWlanStatus) {
                    isWlanStatus = true;
                    switchWifi.setChecked(true);
                    llWlanShow.setVisibility(View.VISIBLE);
                    iWifiManager.openWifi();
                    avi.setVisibility(View.VISIBLE);
                    iWifiManager.scanWifi();
                } else {
                    isWlanStatus = false;
                    iWifiManager.closeWifi();
                    switchWifi.setChecked(false);
                    llWlanShow.setVisibility(View.GONE);
                    iWifiList.clear();
                    wifiList.clear();
                    llWifi.removeAllViews();
                    iWifiManager.disConnectWifi();
                }
                break;
            }
            case R.id.tv_ip:{
                String ipStr = IpGetUtil.getIpAddress(this);
                AdbUtils.adbDebugging();
                HintDialog hintDialog=new HintDialog(WifiActivity.this,getString(R.string.tips), "", new HintDialog.HintDialogListener() {
                    @Override
                    public void onClick(boolean isConfirm) {
                    }
                });
                hintDialog.setContent("IP:"+ipStr);
                hintDialog.setButtonIsShow(2);
                hintDialog.show();
                break;
            }
        }
    }

    CommonAdapter adapter;

    public int calculateSignalLevel(int l, int nl) {
        return android.net.wifi.WifiManager.calculateSignalLevel(l, nl);
    }

    private int getLevelIcon(int l) {
        int[] icon = {R.drawable.wifi4, R.drawable.wifi3, R.drawable.wifi2, R.drawable.wifi1};
        int max = 4;
        float lenF = (float) l / 100 * max;
        int len = (int) Math.floor(lenF);
        if (len >= icon.length) {
            len = icon.length - 1;
        }
        return icon[len];
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (taskDate!=null) {
            taskDate.cancel();
            taskDate = null;
        }
        if (timerDate!=null){
            timerDate.cancel();
            timerDate = null;
        }
        iWifiManager.destroy();
    }

    @Override
    public void onWifiChanged(List<IWifi> wifis) {
        if (wifis != null && wifis.size() > 0) {
            iWifiList.clear();
            wifiList.clear();
            for (int i = 0; i < wifis.size(); i++) {
                boolean isSaved = wifis.get(i).isSaved();
                boolean isConnected = wifis.get(i).isConnected();
                if (isConnected) {
                    iWifiList.add(wifis.get(i));
                } else {
                    wifiList.add(wifis.get(i));
                }
            }
            adapter.notifyDataSetChanged();
        }
        if (iWifiList != null && iWifiList.size() > 0) {
            llWifi.removeAllViews();
            for (int i = 0; i < iWifiList.size(); i++) {
                IWifi item = iWifiList.get(i);
                View view = View.inflate(this, R.layout.wifi_set_item, null);
                TextView tvWifiName = view.findViewById(R.id.tv_wifi_name);
                TextView tvWifiState = view.findViewById(R.id.tv_wifi_state);
                ImageView ivWifi = view.findViewById(R.id.iv_wifi);
                String title = item.name();
                if (title == null || title.equals("")) {
                    title = getString(R.string.unknown_network_hidden_network);
                }
                tvWifiName.setText(title);
                ivWifi.setImageResource(getLevelIcon(calculateSignalLevel(item.level(), 100)));
                if (item.isConnected()) {
                    tvWifiState.setText(getString(R.string.connected));
                } else {
                    tvWifiState.setText(getString(R.string.saved));
                }
                view.setTag(i);
                view.setOnClickListener(new OnClick());
                view.setOnLongClickListener(new OnLongClick());
                llWifi.addView(view);
            }
        }
        avi.setVisibility(View.INVISIBLE);
    }
    class OnLongClick implements View.OnLongClickListener {


        @Override
        public boolean onLongClick(View view) {
            int tag = (int) view.getTag();
            IWifi iWifi = iWifiList.get(tag);
            HintDialog hintDialog = new HintDialog(WifiActivity.this, iWifi.name(), "", new HintDialog.HintDialogListener() {
                @Override
                public void onClick(boolean isConfirm) {
                    if (isConfirm) {
                        iWifiManager.removeWifi(iWifi);
                        iWifiManager.disConnectWifi();
                        llWifi.removeView(llWifi.getChildAt(tag));
                        iWifiManager.scanWifi();
                    }
                }
            });
            hintDialog.setContent(getString(R.string.cancel_saving_this_network));
            hintDialog.show();
            return false;
        }
    }

    class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int tag = (int) view.getTag();
            IWifi iWifi = iWifiList.get(tag);
            HintDialog hintDialog = new HintDialog(WifiActivity.this, iWifi.name(), "", new HintDialog.HintDialogListener() {
                @Override
                public void onClick(boolean isConfirm) {
                    if (isConfirm) {
                        iWifiManager.connectSavedWifi(iWifi);
                        iWifiManager.scanWifi();
                    }
                }
            });
            hintDialog.setContent(getString(R.string.connect_to_this_network));
            hintDialog.show();
        }
    }

    @Override
    public void onConnectChanged(boolean status) {
        if (status) {
            ToastUtils.showShort("wifi" + getString(R.string.connected) + "!");
        }
    }

    @Override
    public void onStateChanged(State state) {

    }
}
