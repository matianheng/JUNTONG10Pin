package com.hndl.ui.server;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;

import android.content.Intent;
import android.os.Handler;

import com.hndl.ui.AppData;
import com.hndl.ui.base.BaseService;
import java.util.ArrayList;
import java.util.List;

public class VirtualWallService extends BaseService {

    private List<byte[]> mModifyList = new ArrayList<>();

    private Handler handler = new Handler();
    private Runnable pollingTask = new Runnable() {
        @Override
        public void run() {

            if (AppData.isSendVirtual.get()) {
                canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600 + AppData.nodeID, mModifyList.get(AppData.indexVirtual.get())));
            }
            handler.postDelayed(this, 500);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (mModifyList!=null){
            mModifyList.clear();
        }
        for (int i = 0; i < AppData.sendVirtualData.length; i++) {
            byte[] data = new byte[]{(byte) 0x43, 0x27, 0x20, AppData.sendVirtualData[i], (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00};
            mModifyList.add(data);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 启动轮询任务
        handler.post(pollingTask);

        return START_STICKY; // 服务被杀死后会自动重启
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止轮询任务
        handler.removeCallbacks(pollingTask);
    }
}
