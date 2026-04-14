package com.hndl.ui.server;

/**
 * 串口协议处理
 */
public interface OnHandleSerialProtocol {
    boolean handleSerialData(byte[] serialData);
}
