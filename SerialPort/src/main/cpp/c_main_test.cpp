//
// Created by 30890 on 2025-03-12.
//
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sched.h>
#include <time.h>
#include <signal.h>
#include <unistd.h>
#include "native-lib.h"

#ifdef LINUX_C_PLATFORM

/**
 * heartBeat 0..9
 */
void user_heartbeat_fun(int heartBeat){

}

void user_requestSleep_fun(void){

}

/**
 * canIndex 0..1
 * canBaud  [125,250,500,1000]
 * resultValue 0 is ok, 1 is err.
 */
void user_canOpenResp_fun(int canIndex,int canBaud,int resultValue){

}

/**
 * canIndex 0..1
 * resultValue 0 is ok, 1 is err.
 */
void user_canCloseResp_fun(int canIndex,int resultValue){

}

/**
 * canIndex 0..1
 * filterBank 0..27,can1 use 0..13,can2 use 14..27
 * filterMode 0 is idmask mode , 1 is idlist mode.
 * filterBits 16bits or 32bits.
 * resultValue 0 is ok, 1 is err.
 */
void user_canFilterResp_fun(int canIndex,int filterBank,int filterMode,int filterBits,int resultValue){

}

/**
 * canIndex 0..1
 * dataLen 1..8
 * canID  11bits or 29bits
 * canFrameType 0 is stdframe,1 is extframe
 * resultValue 0 is ok, 1 is err.
 * msg can's data[]
 */
void user_canSendResp_fun(int canIndex,int dataLen,int canID,int canFrameType,int resultValue,uint8_t * msg){

}

/**
 * canIndex 0..1
 * dataLen 1..8
 * canID  11bits or 29bits
 * canFrameType 0 is stdframe,1 is extframe
 * msg can's data[]
 */
void user_canRecvData_fun(int canIndex,int dataLen,int canID,int canFrameType,uint8_t * msg){

}

/**
 * usbMode 0 is otg mode , 1 is host mode.
 */
void user_usbModeResp_fun(int usbMode){

}

/**
 * canIndex 0..1
 * hasRs 0 is no Rs ,1 is Has Rs
 */
void user_canRsResp_fun(int canIndex,int hasRs){

}

/**
 * canIndex 0..1
 * resetState 0 is can has error,but reset ok ,1 is can has error and reset fault.
 */
void user_canResetState_fun(int canIndex,int resetState){

}

/**
 * gpioIndex 0..9
 * gpioValue 0 is low voltage , 1 is high voltage
 */
void user_gpioValueResp_fun(int gpioIndex,int gpioValue){

}

/**
 * timerId: 0..9
 * resultValue 0 is ok, 1 is error
 */
void user_setTimerResp_fun(int timerId,int resultValue){

}

/**
 * timerId: 0..9
 * resultValue 0 is ok, 1 is error
 */
void user_cancelTimerResp_fun(int timerId,int resultValue){

}

/**
 * version ver format is "01.11"
 * len data's len, almost is 5
 */
void user_getMcuAppVersionResp_fun(uint8_t *version,int len){

}




_callback_interface_s g_callback_interface={
        .heart_beat_callback = user_heartbeat_fun,
        .request_sleep_callback = user_requestSleep_fun,
        .can_open_callback = user_canOpenResp_fun,
        .can_close_callback = user_canCloseResp_fun,
        .can_filter_callback = user_canFilterResp_fun,
        .can_send_callback = user_canSendResp_fun,
        .can_recv_data_callback = user_canRecvData_fun,
        .usb_mode_callback = user_usbModeResp_fun,
        .can_rs_callback = user_canRsResp_fun,
        .can_error_callback = user_canResetState_fun,
        .gpio_value_callback = user_gpioValueResp_fun,
        .set_timer_callback = user_setTimerResp_fun,
        .cancel_timer_callback = user_cancelTimerResp_fun,
        .get_mcu_app_version_callback = user_getMcuAppVersionResp_fun
};


// 线程函数
void* thread_uart_task_function(void* args) {
    //正常使用的时候，可以将此处while打开，以实现工作线程异常后，自动恢复工作。
//    while(true){

        startUartTask("/dev/ttyHSL2");
//    }

}

void user_main_test(void){
    pthread_t uart_task = -1;
    // 设置回调函数接口
    set_callback_interface(&g_callback_interface);
    //设置工作模式在解析模式
    setWorkMode(1);
    //创建协处理器线程任务
    pthread_create(&uart_task, NULL, thread_uart_task_function, NULL);
    //等待协处理器线程任务进入正常工作状态
    while(getWorkState() == 0);
    //发送心跳包，测试协处理器工作状态，会回调对应回调函数
    sendHeartBeat(0);
    //打开can 0端口，配置波特率250kbps，如果出错，会回调对应的回调函数，成功的话，不会回调。
    openCanPort(0,250);
    //配置接收的过滤器，如下配置可以接收0x101，0x102，0x103，0x104四个标准帧id。有数据接收时，会进入数据接收回调,本例中为user_canRecvData_fun函数。
    setCanFilterListWith16bit(0,0,0x101,0x102,0x103,0x104);
    //配置接收的过滤器，如下配置可以接收0x18fff808，0x18fff809四个扩展帧id。有数据接收时，会进入数据接收回调,本例中为user_canRecvData_fun函数。
    setCanFilterListWith32bit(0,1,0x18fff808,0x18fff809);
    //发送一帧测试数据，扩展帧，ID为0x18fff809
    const uint8_t test_can_data[8]={0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07};
    sendCanData(0,0x18fff809,1,8,test_can_data);



    //关闭uart task线程任务。一般不会使用，特殊情况下需要可以使用。
    stopUartTask();

}


#endif