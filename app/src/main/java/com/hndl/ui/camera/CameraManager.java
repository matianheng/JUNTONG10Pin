package com.hndl.ui.camera;

import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.quectel.qcarapi.helper.QCarCamInDetectHelper;
import com.quectel.qcarapi.stream.QCarCamera;
import com.quectel.qcarapi.util.QCarError;
import com.quectel.qcarapi.util.QCarLog;

public class CameraManager {
    private CameraManager() {
        QCarLog.setModuleLogLevel(QCarLog.LOG_MODULE_APP);
        QCarLog.setTagLogLevel(Log.ERROR);
    }

    private static CameraManager instance;

    public static CameraManager getInstance() {
        if (instance == null) {
            synchronized (CameraManager.class) {
                if (instance == null) {
                    instance = new CameraManager();
                }
            }
        }
        return instance;
    }

    public void openCamera(int csi,int inputNum,int inputType){
        QCarCamera carCamera = GUtilMain.getQCamera(csi);
        if (carCamera != null){
            int count = 0;
            while (count < 10) {
                int ret = carCamera.cameraOpen(inputNum, inputType);
                if (ret == 0) {
                    break;
                } else {
                    count++;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            carCamera.registerOnErrorCB(new QCarError.OnErrorCB() {
                @Override
                public void onError(int i, int i1, byte[] bytes, int i2, int i3) {
                    LogUtils.e("errorType:"+i+"  errorCode:"+i1+"  csiNum:"+i2+"   channelName:"+i3+bytes.toString());
                }
            });
        }
    }

    public void closeCamera(int csi){
        synchronized (this) {
            try {
                wait(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Object>() {
            @Override
            public Object doInBackground() throws Throwable {
                QCarCamera carCamera = GUtilMain.getQCamera(csi);
                carCamera.cameraClose();  // 关闭ais_server，必须保证最后关闭
                carCamera.release();  // 关闭ais_server，必须保证最后关闭
                GUtilMain.removeQCamera(csi);

                return null;
            }

            @Override
            public void onSuccess(Object result) {

            }
        });
    }

    public QCarCamera getCarCamera(int csi) {
        return GUtilMain.getQCamera(csi);
    }
}
