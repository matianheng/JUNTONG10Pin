package com.hndl.ui.server;
import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.AppData.serialPort;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.blankj.utilcode.util.ThreadUtils;
import com.hndl.ui.AppData;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class CanSerialManager {
    private static final String TAG = "CanSerialManager";
    private static final int DEFAULT_CAN_BAUD = 250;
    private static final int SERIAL_READY_MAX_RETRY = 20;
    private static final long SERIAL_READY_RETRY_DELAY_MS = 100L;
    private static final int CAN_OPEN_MAX_RETRY = 3;
    private static final long CAN_REOPEN_DELAY_MS = 80L;
    private static final int COMMAND_QUEUE_SUCCESS_MIN = 1;

    private Context mContext;
    private OnHandleSerialProtocol mOnHandleSerialProtocol;
    private int dataLen = 0;
    private final int MAX_BUFFER_SIZE = 2048;
    private byte[] buffers = new byte[MAX_BUFFER_SIZE];
    private final Object canConfigLock = new Object();

    public CanSerialManager(){
    }

    public void setContext(Context context){
        mContext = context;
    }

    public void setOnHandleSerialProtocol(OnHandleSerialProtocol handleSerialProtocol){
        this.mOnHandleSerialProtocol = handleSerialProtocol;
    }

    public int start(){
        openSerialPort();
        openPersistedCanPorts();

        iniSerialPortManager();
//        ThreadUtils.runOnUiThreadDelayed(() -> {
//           canFilter();
//        }, 300);
        return 0;
    }

    public void stop(){

    }

    public boolean sendBytes(byte[] sendBytes) {
        if (serialPort != null && AppData.isCanData){
            return serialPort.sendUartFunJNI(sendBytes) != -1;
        }else{
            return false;
        }
    }

    public boolean updateCanBaud(int canIndex, int baud) {
        return reopenCanPort(canIndex, baud, true);
    }

    private void openSerialPort() {

        serialPort.recvMsg(new Function1<byte[], Unit>() {
            @Override
            public Unit invoke(byte[] bytes) {
                if (mOnHandleSerialProtocol != null){
                    mOnHandleSerialProtocol.handleSerialData(bytes);
                }
                return null;
            }
        });
        //AppData.baudRate = MMKVUtils.getInstance().decodeInt("baudRate");
        AppData.baudRate = 921600;
        serialPort.openSerialPort("/dev/ttyHSL2", AppData.baudRate);
//        if (AppData.isFrist) {
//            AppData.isFrist = false;
//            for (int i = 0; i < 5; i++) {
//                sendBytes(MyUtils.constructSerialFilter((byte) 0x30, new byte[]{0x32}));
////                serialPortManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x30, new byte[]{0x32}));
//            }
//            ThreadUtils.runOnUiThreadDelayed(() -> {
//                if (AppData.isBaudRate) {
//                    if (AppData.baudRate == 115200) {
//                        AppData.baudRate = 921600;
//                    } else {
//                        AppData.baudRate = 921600;
//                    }
//                    serialPortManager.openSerialPort("/dev/ttyHSL2", AppData.baudRate);
//                    for (int i = 0; i < 3; i++) {
//                        sendBytes(MyUtils.constructSerialFilter((byte) 0x30, new byte[]{0x32}));
////                        serialPortManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x30, new byte[]{0x32}));
//                    }
//                }
//            }, 300);
//        }
    }

    private void openPersistedCanPorts() {
        Thread initThread = new Thread(() -> {
            reopenCanPort(0, getSavedCanBaud("can1Baud"), false);
            reopenCanPort(1, getSavedCanBaud("can2Baud"), false);
        }, "can-port-init");
        initThread.start();
    }

    private int getSavedCanBaud(String key) {
        int baud = MMKVUtils.getInstance().decodeInt(key);
        if (baud == 0) {
            return DEFAULT_CAN_BAUD;
        }
        return baud;
    }

    private boolean reopenCanPort(int canIndex, int baud, boolean saveOnSuccess) {
        int targetBaud = normalizeCanBaud(baud);
        synchronized (canConfigLock) {
            waitForSerialReady();

            int closeResult = serialPort.closeCanPort(canIndex);
            if (isCommandQueued(closeResult)) {
                Log.d(TAG, "closeCanPort queued, index=" + canIndex + ", bytes=" + closeResult);
            } else {
                Log.w(TAG, "closeCanPort queue failed, index=" + canIndex + ", result=" + closeResult);
            }
            SystemClock.sleep(CAN_REOPEN_DELAY_MS);

            for (int attempt = 1; attempt <= CAN_OPEN_MAX_RETRY; attempt++) {
                int openResult = serialPort.openCanPort(canIndex, targetBaud);
                if (isCommandQueued(openResult)) {
                    if (saveOnSuccess) {
                        MMKVUtils.getInstance().encode(canIndex == 0 ? "can1Baud" : "can2Baud", targetBaud);
                    }
                    Log.i(TAG, "openCanPort queued, index=" + canIndex + ", baud=" + targetBaud + ", attempt=" + attempt + ", bytes=" + openResult);
                    return true;
                }

                Log.w(TAG, "openCanPort queue failed, index=" + canIndex + ", baud=" + targetBaud + ", attempt=" + attempt + ", result=" + openResult);
                SystemClock.sleep(CAN_REOPEN_DELAY_MS);
            }
        }

        return false;
    }

    private void waitForSerialReady() {
        for (int retry = 0; retry < SERIAL_READY_MAX_RETRY; retry++) {
            if (serialPort.getWorkFlag()) {
                return;
            }
            SystemClock.sleep(SERIAL_READY_RETRY_DELAY_MS);
        }
        Log.w(TAG, "serial port not ready before CAN configuration");
    }

    private int normalizeCanBaud(int baud) {
        switch (baud) {
            case 100:
            case 125:
            case 250:
            case 500:
            case 666:
            case 1000:
                return baud;
            default:
                return DEFAULT_CAN_BAUD;
        }
    }

    private boolean isCommandQueued(int result) {
        return result >= COMMAND_QUEUE_SUCCESS_MIN;
    }

    private void iniSerialPortManager() {

//        if (portDataListener == null) {
//            portDataListener = new OnSerialPortDataListener() {
//                @Override
//                public void onDataReceived(byte[] bytes, SerialPortEnum serialPortEnum) {
//                    try{
////                        LogUtils.e("XJW","bytes:"+ ConvertUtils.bytes2HexString(bytes));
//                        // 若读取到的数据为空，则忽略
////                    int readyDataLens = bytes.length;
//                        int readyDataLens = bytes.length;
//                        if (readyDataLens <= 0){
//                            return;
//                        }
//                        if ((dataLen+readyDataLens) > MAX_BUFFER_SIZE){
//                            Arrays.fill(buffers,(byte) 0);
//                            dataLen = 0;
//                        }
// //                       LogUtils.e("XJW","---11111--dataLen:"+dataLen+"---readyDataLens:"+readyDataLens+"====bytes len:"+bytes.length);
//                        System.arraycopy(bytes, 0, buffers, dataLen, readyDataLens);  //将返回的串口数据复制给buffers数组
//                        dataLen += readyDataLens;
//                        int startIndex = -1;
//                        int endIndex = -1;
//                        int curPos = 0; // 当前遍历位置
//                        for (int i=0; i < dataLen; i++){
//                            // 0x02-协议头  0x03-协议尾
//                            if (buffers[i] == 0x02){
//                                startIndex = i;
//                            }else if (buffers[i] == 0x03){
//                                endIndex = i;
//                            }
//
//                            if ((startIndex < 0) || (endIndex < 0) ){
//                                continue;
//                            }
//
//                            // 找到协议头、协议尾，并符合要求
//                            if ((endIndex < startIndex) || ((endIndex - startIndex) < 4)){
//                                continue;
//                            }
//                            // 找到协议头、协议尾，且剩余长度也够
//                            if ((dataLen - endIndex) < 3){
//                                continue;
//                            }
//
//                            byte[] serialData = ArrayUtils.subArray(buffers, startIndex, endIndex + 3);   //将标准串口数据取出并赋值
////                        parseSerialData(serialData);
//                            if (mOnHandleSerialProtocol != null){
//                                mOnHandleSerialProtocol.handleSerialData(serialData);
//                            }
//
//                            curPos = endIndex + 3;
//                            startIndex = -1;
//                            endIndex = -1;
//                        }
//
//                        if (curPos > 0){
//                            int remainLens = dataLen - curPos;
//                            byte[] remainBytes = null;
////                            LogUtils.e("XJW","---222222--curPos:"+curPos+"---remainLens:"+remainLens);
//                            if (remainLens > 0){
//                                System.arraycopy(buffers,curPos,buffers,0,remainLens);
//
//                                //将 buffers 缓存区清零
//                                Arrays.fill(buffers,remainLens,dataLen,(byte)0x00);
//                            }
//                            dataLen = remainLens;
//                        }
//                    }catch (Exception e){
//                        e.printStackTrace();
//
//                        Arrays.fill(buffers, (byte)0x00);
//                        dataLen = 0;
//                    }
//
//                }
//
//                @Override
//                public void onDataSent(byte[] bytes,SerialPortEnum serialPortEnum) {
//                    //Log.e("XJW","onDataSent:"+ConvertUtils.bytes2HexString(bytes));
//                }
//            };
//            serialPortManager.setOnSerialPortDataListener(portDataListener);

    }


//    public void iniSerialPortManager() {
//        if (portDataListener == null) {
//            portDataListener = new OnSerialPortDataListener() {
//                @Override
//                public void onDataReceived(byte[] bytes) {
//                    LogUtils.e("XJW","bytes:"+ConvertUtils.bytes2HexString(bytes));
//                    if (dataLen == 0) {
//                        buffers = new byte[2048];
//                    }
//                    System.arraycopy(bytes, 0, buffers, dataLen, bytes.length);  //将返回的串口数据复制给buffers数组
//                    dataLen = 0;
//                    byte[] newBuffer = new byte[2048];
//                    int curPos = 0;
//                    while (true) {
//                        int startIndex = ArrayUtils.indexOf(buffers, (byte) 0x02, curPos);  //数据头0x02的下标
//                        int endIndex = ArrayUtils.indexOf(buffers, (byte) 0x03,curPos);    //数据尾0x03的下标
//
//                        if ((startIndex < 0) || (endIndex < 0)) {
//                            if (startIndex == 0) {
//                                dataLen = bytes.length;
//                            }
//                            break;
//                        }
//
//                        if ((endIndex - startIndex) < 4) {               //数据小于4位为不合法数据
//                            int max = Math.max(endIndex, startIndex);
//                            System.arraycopy(buffers, max, newBuffer, 0, buffers.length - max - 1);
//                            buffers = newBuffer;
//                            continue;
//
//                        }
//                        byte[] serialData = ArrayUtils.subArray(buffers, startIndex, endIndex + 3);   //将标准串口数据取出并赋值
//
//                        parseSerialData(serialData);
//                        curPos = endIndex + 3;
//
////                        System.arraycopy(buffers, endIndex + 1, newBuffer, 0, buffers.length - endIndex - 1);
//                        System.arraycopy(buffers, endIndex + 3, newBuffer, 0, buffers.length - endIndex - 3);
//                        buffers = newBuffer;
//                    }
//                }
//
//                @Override
//                public void onDataSent(byte[] bytes) {
//                    //Log.e("XJW","onDataSent:"+ConvertUtils.bytes2HexString(bytes));
//                }
//            };
//        }
//        serialPortManager.setOnSerialPortDataListener(portDataListener);
//    }

    private void canFilter() {
        byte[] dataFilter = new byte[]{(byte) 0x30, 0x30, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, 0x44, 0x32,
                (byte) 0x30, (byte) 0x32, 0x44, 0x32, (byte) 0x30, (byte) 0x34, 0x44, 0x32, (byte) 0x30, (byte) 0x36, 0x44, 0x32};
        sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x30, 0x31, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x38, 0x30, 0x37,
                (byte) 0x30, (byte) 0x41, 0x38, 0x43, (byte) 0x30, (byte) 0x34, 0x44, 0x36, (byte) 0x30, (byte) 0x38, 0x38, 0x38};
        sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x30, 0x32, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x43, 0x38, 0x38,
                (byte) 0x30, (byte) 0x45, 0x38, 0x38, (byte) 0x30, (byte) 0x38, 0x38, 0x36, (byte) 0x30, (byte) 0x45, 0x38, 0x34};
        sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x30, 0x33, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, 0x39, 0x34,
                (byte) 0x30, (byte) 0x38, 0x38, 0x34, (byte) 0x30, (byte) 0x32, 0x30, 0x38, (byte) 0x30, (byte) 0x34, 0x30, 0x38};
        sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x30, 0x34, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x41, 0x30, 0x38,
                (byte) 0x30, (byte) 0x43, 0x30, 0x38, (byte) 0x30, (byte) 0x45, 0x30, 0x38, (byte) 0x30, (byte) 0x38, 0x44, 0x32};
        sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x30, 0x35, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x43, 0x44, 0x32,
                (byte) 0x30, (byte) 0x45, 0x44, 0x32, (byte) 0x30, (byte) 0x41, 0x44, 0x32, (byte) 0x30, (byte) 0x41, 0x30, 0x37};
        sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x30, 0x36, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x30, 0x45, 0x32,
                (byte) 0x30, (byte) 0x32, 0x45, 0x32, (byte) 0x30, (byte) 0x34, 0x45, 0x32, (byte) 0x30, (byte) 0x36, 0x45, 0x32};
        sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x30, 0x37, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x38, 0x45, 0x32,
                (byte) 0x30, (byte) 0x32, 0x41, 0x32, (byte) 0x30, (byte) 0x34, 0x41, 0x32, (byte) 0x30, (byte) 0x36, 0x41, 0x32};
        sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x30, 0x38, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x32, 0x31, 0x32,
                (byte) 0x30, (byte) 0x41, 0x38, 0x43, (byte) 0x30, (byte) 0x32, 0x39, 0x34, (byte) 0x30, (byte) 0x32, 0x30, 0x33};
        sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));


        dataFilter = new byte[]{(byte) 0x30, 0x39, 0x30, 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x41, 0x45, 0x32,
                (byte) 0x30, (byte) 0x43, 0x45, 0x32, (byte) 0x30, (byte) 0x45, 0x45, 0x32, (byte) 0x30, (byte) 0x36, 0x36, 0x30};
        sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));

        dataFilter = new byte[]{(byte) 0x30, 0x41, 0x30, 0x31, (byte) 0x31, (byte) 0x43, (byte) 0x30, 0x30, 0x30,
                (byte) 0x31, (byte) 0x30, 0x30, 0x30, (byte) 0x43, (byte) 0x30, 0x30, 0x38, (byte) 0x30, (byte) 0x30, 0x30, 0x30};
        sendBytes(MyUtils.constructSerialFilter((byte) 0x34, dataFilter));


    }
}
