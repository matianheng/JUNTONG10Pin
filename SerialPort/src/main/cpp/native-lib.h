//
// Created by 30890 on 2025-03-11.
//

#ifndef FORKLIFT_BYD_8_NATIVE_LIB_H
#define FORKLIFT_BYD_8_NATIVE_LIB_H

#define ANDROID_JAVA_PLATFORM
//#define LINUX_C_PLATFORM



#ifdef LINUX_C_PLATFORM

/**
 * heartBeat 0..9
 */
typedef void (*heartbeat_fun)(int heartBeat);

typedef void (*requestSleep_fun)(void);

/**
 * canIndex 0..1
 * canBaud  [125,250,500,1000]
 * resultValue 0 is ok, 1 is err.
 */
typedef void (*canOpenResp_fun)(int canIndex,int canBaud,int resultValue);

/**
 * canIndex 0..1
 * resultValue 0 is ok, 1 is err.
 */
typedef void (*canCloseResp_fun)(int canIndex,int resultValue);

/**
 * canIndex 0..1
 * filterBank 0..27,can1 use 0..13,can2 use 14..27
 * filterMode 0 is idmask mode , 1 is idlist mode.
 * filterBits 16bits or 32bits.
 * resultValue 0 is ok, 1 is err.
 */
typedef void (*canFilterResp_fun)(int canIndex,int filterBank,int filterMode,int filterBits,int resultValue);

/**
 * canIndex 0..1
 * dataLen 1..8
 * canID  11bits or 29bits
 * canFrameType 0 is stdframe,1 is extframe
 * resultValue 0 is ok, 1 is err.
 * msg can's data[]
 */
typedef void (*canSendResp_fun)(int canIndex,int dataLen,int canID,int canFrameType,int resultValue,uint8_t * msg);

/**
 * canIndex 0..1
 * dataLen 1..8
 * canID  11bits or 29bits
 * canFrameType 0 is stdframe,1 is extframe
 * msg can's data[]
 */
typedef void (*canRecvData_fun)(int canIndex,int dataLen,int canID,int canFrameType,uint8_t * msg);

/**
 * usbMode 0 is otg mode , 1 is host mode.
 */
typedef void (*usbModeResp_fun)(int usbMode);

/**
 * canIndex 0..1
 * hasRs 0 is no Rs ,1 is Has Rs
 */
typedef void (*canRsResp_fun)(int canIndex,int hasRs);

/**
 * canIndex 0..1
 * resetState 0 is can has error,but reset ok ,1 is can has error and reset fault.
 */
typedef void (*canResetState_fun)(int canIndex,int resetState);

/**
 * gpioIndex 0..9
 * gpioValue 0 is low voltage , 1 is high voltage
 */
typedef void (*gpioValueResp_fun)(int gpioIndex,int gpioValue);

/**
 * timerId: 0..9
 * resultValue 0 is ok, 1 is error
 */
typedef void (*setTimerResp_fun)(int timerId,int resultValue);

/**
 * timerId: 0..9
 * resultValue 0 is ok, 1 is error
 */
typedef void (*cancelTimerResp_fun)(int timerId,int resultValue);

/**
 * version ver format is "01.11"
 * len data's len, almost is 5
 */
typedef void (*getMcuAppVersionResp_fun)(uint8_t *version,int len);

typedef struct{
    heartbeat_fun heart_beat_callback;
    requestSleep_fun request_sleep_callback;
    canOpenResp_fun can_open_callback;
    canCloseResp_fun can_close_callback;
    canFilterResp_fun can_filter_callback;
    canSendResp_fun can_send_callback;
    canRecvData_fun can_recv_data_callback;
    usbModeResp_fun usb_mode_callback;
    canRsResp_fun can_rs_callback;
    canResetState_fun can_error_callback;
    gpioValueResp_fun gpio_value_callback;
    setTimerResp_fun set_timer_callback;
    cancelTimerResp_fun cancel_timer_callback;
    getMcuAppVersionResp_fun get_mcu_app_version_callback;
}_callback_interface_s;

void set_callback_interface(_callback_interface_s * p);

/**
 *
 * @param port Serial Device Files
 * @return
 */
int startUartTask(const char * port);

/**
 *
 * @param p data
 * @param len data's len
 * @return
 */
int sendUartMsg(uint8_t * p,int len);

/**
 * mode 0 is TRANSPARENT_MODE , 1 is ANALYTICAL_MODE
 */
void setWorkMode(int mode);


void stopUartTask(void);
int getWorkState(void);

/**
 * heartBeat 0..15
 */
int sendHeartBeat(int heartBeat);


int enterLowPower(void);

/**
 * canIndex 0..1
 * baud 125,250,500,1000   kbps
 */
int openCanPort(int canIndex,int baud);

/**
 * canIndex 0..1
 */
int closeCanPort(int canIndex);

/**
 * canIndex 0..1
 * bank 0..27  , 0..14 is for can0 , 15..27 is for can1
 * id0/id1/id2/id3 16bits canId for list mode
 */
int setCanFilterListWith16bit(int canIndex,int bank,int id0,int id1,int id2,int id3);

/**
 * canIndex 0..1
 * bank 0..27  , 0..14 is for can0 , 15..27 is for can1
 * id0/id1 32bits canId for list mode
 */
int setCanFilterListWith32bit(int canIndex,int bank,int id0,int id1);

/**
 * canIndex 0..1
 * bank 0..27  , 0..14 is for can0 , 15..27 is for can1
 * id0/id1 16bits canId for mask mode
 * id0Mask/id1Mask 16bits mask for mask mode
 */
int setCanFilterMaskWith16bit(int canIndex,int bank,int id0,int id0Mask,int id1,int id1Mask);

/**
 * canIndex 0..1
 * bank 0..27  , 0..14 is for can0 , 15..27 is for can1
 * id0 32bits canId for mask mode
 * id0Mask 32bits mask for mask mode,please take careful,32bit default is ext frame,if need std frame mask ,please use setCanFilterMaskWith16bit function
 */
int setCanFilterMaskWith32bit(int canIndex,int bank,int id0,int id0Mask);

/**
 * canIndex 0..1
 * canId 11bit std frame ID or 29bit std frame id
 * frameType 0 is std ,1 is ext
 * dataLen 1..8
 * pdata can's data
 */
int sendCanData(int canIndex,int canId,int frameType,int dataLen,const uint8_t * pdata);

/**
 * usbMode 0 is otg mode , 1 is host mode, default for power on is host mode.
 */
int changeUsbMode(int usbMode);

/**
 * canIndex 0..1
 * hasRs 0 is no Rs(120ohm) ,1 is has Rs(120ohm)
  */
int configCanRs(int canIndex,int hasRs);


int sendGetUsbModeMsg(void);

/**
 * canIndex 0..1
 */
int sendGetCanRsMsg(int canIndex);

/**
 * gpioValue 0..4
 */
int sendGetGpioValueMsg(int gpioIndex);

/**
 * timerId 0..9
 * taskType now almost is 0
 * currentYear The year of the current time,but need note, for example ,24 is 2024
 * currentMon  the month of the current time
 * currentDay the day of the current time
 * currentHour the hour of the current time
 * currentMin The minute of the current time
 * currentSec the second of the current time
 * aimYear The year of the aim time,but need note, for example ,24 is 2024
 * aimMon  the month of the aim time
 * aimDay the day of the aim time
 * aimHour the hour of the aim time
 * aimMin The minute of the aim time
 * aimSec the second of the aim time
 */
int openTimerTask(
        int timerId,
        int taskType,
        int currentYear,
        int currentMon,
        int currentDay,
        int currentHour,
        int currentMin,
        int currentSec,
        int aimYear,
        int aimMon,
        int aimDay,
        int aimHour,
        int aimMin,
        int aimSec);

/**
 * timerId 0..9
 */
int cancelTimerTask(int timerId);

int getCoprocessorVersion(void);
#endif




#endif //FORKLIFT_BYD_8_NATIVE_LIB_H
