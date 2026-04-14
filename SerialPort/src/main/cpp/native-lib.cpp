
#include <string>
#include "uart_task.h"
#include "ring_buffer.h"
#include "msg_protocol.h"
#include "native-lib.h"

uint8_t isWorkOk = 0;
extern ThreadArgs thread_arg;
extern uint8_t protocol_mode;
extern bool run_state;

#ifdef LINUX_C_PLATFORM
#define LOGI(fmt, args...)
#define LOGD(fmt, args...)
#define LOGE(fmt, args...)
#endif




#ifdef ANDROID_JAVA_PLATFORM
#include "android/log.h"
#include <jni.h>
static const char *TAG="serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)



#ifdef __cplusplus
extern "C" {
#endif




// 缓存全局引用（可选优化）
static jclass g_cls = nullptr;
static jmethodID g_mid_recvMsg = nullptr;
static jmethodID g_mid_heartBeat = nullptr;
static jmethodID g_mid_requestSleep = nullptr;
static jmethodID g_mid_canOpenResp = nullptr;
static jmethodID g_mid_canCloseResp = nullptr;
static jmethodID g_mid_canFilterResp = nullptr;
static jmethodID g_mid_canSendResp = nullptr;
static jmethodID g_mid_canRecvData = nullptr;
static jmethodID g_mid_usbModeResp = nullptr;
static jmethodID g_mid_canRsResp = nullptr;
static jmethodID g_mid_canResetState = nullptr;
static jmethodID g_mid_gpioValueResp = nullptr;
static jmethodID g_mid_setTimerResp = nullptr;
static jmethodID g_mid_cancelTimerResp = nullptr;
static jmethodID g_mid_getMcuAppVersionResp = nullptr;


// 可选初始化（缓存全局引用）
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    // 查找并缓存全局类引用
    jclass tmp = env->FindClass("com/hndl/serialport/SerialPortUtils");
    if (tmp == nullptr) {
        return JNI_ERR;
    }
    g_cls = static_cast<jclass>(env->NewGlobalRef(tmp));
    env->DeleteLocalRef(tmp);

    // 缓存方法 ID
    g_mid_recvMsg = env->GetMethodID(g_cls, "recvMsgCallback", "([B)V");
    if (g_mid_recvMsg == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_heartBeat = env->GetMethodID(g_cls, "heartBeatCallback", "(I)V");
    if (g_mid_heartBeat == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_requestSleep = env->GetMethodID(g_cls, "requestSleepCallback", "()V");
    if (g_mid_requestSleep == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_canOpenResp = env->GetMethodID(g_cls, "canOpenRespCallback", "(III)V");
    if (g_mid_canOpenResp == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_canCloseResp = env->GetMethodID(g_cls, "canCloseRespCallback", "(II)V");
    if (g_mid_canCloseResp == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_canFilterResp = env->GetMethodID(g_cls, "canFilterRespCallback", "(IIIII)V");
    if (g_mid_canFilterResp == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_canSendResp = env->GetMethodID(g_cls, "canSendRespCallback", "(IIIII[B)V");
    if (g_mid_canSendResp == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_canRecvData = env->GetMethodID(g_cls, "canRecvDataCallback", "(IIII[B)V");
    if (g_mid_canRecvData == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_usbModeResp = env->GetMethodID(g_cls, "usbModeRespCallback", "(I)V");
    if (g_mid_usbModeResp == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_canRsResp = env->GetMethodID(g_cls, "canRsRespCallback", "(II)V");
    if (g_mid_canRsResp == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_canResetState = env->GetMethodID(g_cls, "canResetStateCallback", "(II)V");
    if (g_mid_canResetState == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_gpioValueResp = env->GetMethodID(g_cls, "gpioValueRespCallback", "(II)V");
    if (g_mid_gpioValueResp == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_setTimerResp = env->GetMethodID(g_cls, "setTimerRespCallback", "(II)V");
    if (g_mid_setTimerResp == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_cancelTimerResp = env->GetMethodID(g_cls, "cancelTimerRespCallback", "(II)V");
    if (g_mid_cancelTimerResp == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    g_mid_getMcuAppVersionResp = env->GetMethodID(g_cls, "getMcuAppVersionRespCallback", "([B)V");
    if (g_mid_getMcuAppVersionResp == nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}



void analytical_msg_task(JNIEnv *env, jobject obj ,uint8_t * msg,int msg_len){
    // 获取 MyNativeClass 的类引用
//    for(int i = 0 ; i!= msg_len ;i++){
//        LOGD("%02x ",msg[i]);
//    }

    // 1. 输入参数校验
    if (msg_len < 0) {
        LOGD("Invalid msg_len: %d", msg_len);
        return;
    }
    if (msg_len > 0 && msg == nullptr) {
        LOGD("msg is null but msg_len > 0");
        return;
    }

//    // 2. 获取类引用
//    jclass cls = env->GetObjectClass(obj);
//    if (cls == nullptr) {
//        LOGD("Failed to get MyNativeClass");
//        env->ExceptionClear();  // 清除可能的异常
//        return;
//    }

    switch(msg[1]) {
        case 0x30: {
            jint heartBeat = get_num_from_asc(msg[4]);
            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_heartBeat, heartBeat);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
        }
            break;
        case 0x31: {
            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_requestSleep);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
        }
            break;
        case 0x33: {
            jint canIndex = 0;
            jint canOpt = msg[5];
            jint canBaud = 0;
            jint resultValue = 0;
            if (msg[4] == 0x30) {
                canIndex = 0;
            } else if (msg[4] == 0x31) {
                canIndex = 1;
            }
            if (canOpt == 0x30) {
                canBaud = (jint) get_num_from_asc_array(&msg[6], 4);
                resultValue = get_num_from_asc(msg[10]);
                // 调用 Java 的 invokeCallback 方法
                (env)->CallVoidMethod(obj, g_mid_canOpenResp, canIndex, canBaud, resultValue);

                // 7. 检查 Java 回调是否抛出异常
                if (env->ExceptionCheck()) {
                    LOGD("Exception in recvMsgCallback");
                    env->ExceptionDescribe();  // 打印异常信息（调试用）
                    env->ExceptionClear();     // 必须清除异常
                }
            } else {
                resultValue = get_num_from_asc(msg[6]);
                // 调用 Java 的 invokeCallback 方法
                (env)->CallVoidMethod(obj, g_mid_canCloseResp, canIndex, resultValue);

                // 7. 检查 Java 回调是否抛出异常
                if (env->ExceptionCheck()) {
                    LOGD("Exception in recvMsgCallback");
                    env->ExceptionDescribe();  // 打印异常信息（调试用）
                    env->ExceptionClear();     // 必须清除异常
                }
            }


        }
            break;
        case 0x34: {
            jint canIndex = get_num_from_asc(msg[4]);
            jint filterBank = (jint) get_num_from_asc_array(&msg[5], 2);
            jint filterMode = (jint) get_num_from_asc(msg[7]);
            jint filterBits = msg[8] == 0x30 ? 16 : 32;
            jint resultValue = get_num_from_asc(msg[25]);

            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_canFilterResp, canIndex, filterBank, filterMode,
                                  filterBits, resultValue);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
        }
            break;
        case 0x35: {
            jint canIndex = get_num_from_asc(msg[4]);
            jint canLen = (jint) get_num_from_asc(msg[5]);
            uint32_t canIDTemp = get_num_from_asc_array(&msg[6], 8);
            jint canID = (jint) (canIDTemp & 0x7FFFFFFF);
            jint frameType = 0;
            uint8_t canMsgData[8];
            if (canIDTemp & 0x80000000) {
                frameType = 1;
            } else {
                frameType = 0;
            }
            // 4. 创建 Byte 数组
            jbyteArray byteArray = env->NewByteArray(8);
            if (byteArray == nullptr) {
                LOGD("Failed to create byte array");
                return;
            }
            for (int i = 0; i != 8; i++) {
                canMsgData[i] = get_num_from_asc(msg[2 * i + 14]) +
                                (get_num_from_asc(msg[2 * i + 1 + 14]) << 4);
            }
            env->SetByteArrayRegion(byteArray, 0, 8, reinterpret_cast<const jbyte *>(canMsgData));
            jint resultValue = get_num_from_asc(msg[30]);

            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_canSendResp, canIndex, canLen, canID, frameType,
                                  resultValue, byteArray);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
            env->DeleteLocalRef(byteArray);

        }
            break;
        case 0x36: {
            jint canIndex = get_num_from_asc(msg[4]);
            jint canLen = (jint) get_num_from_asc(msg[5]);
            uint32_t canIDTemp = get_num_from_asc_array(&msg[6], 8);
            jint canID = (jint) (canIDTemp & 0x7FFFFFFF);
            jint frameType = 0;
            uint8_t canMsgData[8];
            if (canIDTemp & 0x80000000) {
                frameType = 1;
            } else {
                frameType = 0;
            }
            // 4. 创建 Byte 数组
            jbyteArray byteArray = env->NewByteArray(8);
            if (byteArray == nullptr) {
                LOGD("Failed to create byte array");
                return;
            }
            for (int i = 0; i != 8; i++) {
                canMsgData[i] = get_num_from_asc(msg[2 * i + 14]) +
                                (get_num_from_asc(msg[2 * i + 1 + 14]) << 4);
            }
            env->SetByteArrayRegion(byteArray, 0, 8, reinterpret_cast<const jbyte *>(canMsgData));


            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_canRecvData, canIndex, canLen, canID, frameType,
                                  byteArray);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
            env->DeleteLocalRef(byteArray);

        }
            break;
        case 0x39: {
            jint usbType = get_num_from_asc(msg[4]);
            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_usbModeResp, usbType);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
        }
            break;
        case 0x3A: {
            jint canIndex = get_num_from_asc(msg[4]);
            jint hasRs = get_num_from_asc(msg[5]);

            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_canRsResp, canIndex, hasRs);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
        }
            break;
        case 0x3B:
        {
            jint canIndex = get_num_from_asc(msg[4]);
            jint canResetState = get_num_from_asc(msg[5]);


            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_canResetState, canIndex, canResetState);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
        }
        break;
        case 0x3D:
        {
            jint gpioIndex = get_num_from_asc(msg[4]);
            jint gpioValue = get_num_from_asc(msg[5]);

            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_gpioValueResp, gpioIndex, gpioValue);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
        }
        break;
        case 0x3E:
        {
            jint timerIndex = get_num_from_asc(msg[4]);
            jint timerResult = get_num_from_asc(msg[5]);



            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_setTimerResp, timerIndex, timerResult);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
        }
            break;
        case 0x40:
        {
            jint timerIndex = get_num_from_asc(msg[4]);
            jint timerResult = get_num_from_asc(msg[5]);



            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_cancelTimerResp, timerIndex, timerResult);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
        }
        break;
        case 0x41:
        {

            uint8_t version[5];
            // 4. 创建 Byte 数组
            jbyteArray byteArray = env->NewByteArray(5);
            if (byteArray == nullptr) {
                LOGD("Failed to create byte array");
                return;
            }
            for (int i = 0; i != 5; i++) {
                version[i] = msg[4+i];
            }
            env->SetByteArrayRegion(byteArray, 0, 5, reinterpret_cast<const jbyte *>(version));


            // 调用 Java 的 invokeCallback 方法
            (env)->CallVoidMethod(obj, g_mid_getMcuAppVersionResp,
                                  byteArray);

            // 7. 检查 Java 回调是否抛出异常
            if (env->ExceptionCheck()) {
                LOGD("Exception in recvMsgCallback");
                env->ExceptionDescribe();  // 打印异常信息（调试用）
                env->ExceptionClear();     // 必须清除异常
            }
            env->DeleteLocalRef(byteArray);
        }
        break;
        default:
            break;
    }






//    (env)->DeleteLocalRef(cls);
}



// 可选清理
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return;
    }

    if (g_cls != nullptr) {
        env->DeleteGlobalRef(g_cls);
        g_cls = nullptr;
    }
}





void deal_msg_task(JNIEnv *env, jobject obj ,uint8_t * msg,int msg_len){
    // 1. 输入参数校验
    if (msg_len < 0) {
        LOGD("Invalid msg_len: %d", msg_len);
        return;
    }
    if (msg_len > 0 && msg == nullptr) {
        LOGD("msg is null but msg_len > 0");
        return;
    }

    // 2. 获取类引用
    jclass cls = env->GetObjectClass(obj);
    if (cls == nullptr) {
        LOGD("Failed to get MyNativeClass");
        env->ExceptionClear();  // 清除可能的异常
        return;
    }

    // 3. 获取方法 ID
    jmethodID mid = env->GetMethodID(cls, "recvMsgCallback", "([B)V");
    if (mid == nullptr) {
        LOGD("Method recvMsgCallback not found");
        env->DeleteLocalRef(cls);  // 释放类引用
        env->ExceptionClear();     // 清除 NoSuchMethodError
        return;
    }

    // 4. 创建 Byte 数组
    jbyteArray byteArray = env->NewByteArray(msg_len);
    if (byteArray == nullptr) {
        LOGD("Failed to create byte array");
        env->DeleteLocalRef(cls);
        return;
    }

    // 5. 填充数据
    if (msg_len > 0) {
        env->SetByteArrayRegion(byteArray, 0, msg_len, reinterpret_cast<const jbyte*>(msg));
    }

    // 6. 调用 Java 方法
    env->CallVoidMethod(obj, mid, byteArray);

    // 7. 检查 Java 回调是否抛出异常
    if (env->ExceptionCheck()) {
        LOGD("Exception in recvMsgCallback");
        env->ExceptionDescribe();  // 打印异常信息（调试用）
        env->ExceptionClear();     // 必须清除异常
    }

    // 8. 释放局部引用
    env->DeleteLocalRef(byteArray);
    env->DeleteLocalRef(cls);
}

#ifdef __cplusplus
}
#endif


extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_startUartTaskJNI(
        JNIEnv* env,
        jobject jobj /* this */,
        jstring port ,
        jint baud) {
    JavaVM * javaVM;
    if ((*env).GetJavaVM(&javaVM) != 0){
        return -1;
    }
    // 将 jstring 转换为 C 字符串
    jboolean  isCopy = true;
    const char *cStr = (env)->GetStringUTFChars(port,&isCopy);
    if (cStr == NULL) {
        return -1; // 转换失败
    }

    // 使用字符串数据
    printf("String from Java: %s\n", cStr);
    LOGD("String from Java: %s\n", cStr);

    jobject globalObj = (*env).NewGlobalRef(jobj);
    jint ret= uart_task_start(javaVM,globalObj,cStr,baud);
    // 释放 C 字符串
    (env)->ReleaseStringUTFChars(port , cStr);

    return ret;

}

extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_sendUartFunJNI(
    JNIEnv* env,
    jobject jobj /* this */,
    jbyteArray msg) {


    if (msg == nullptr) {
//        __android_log_print(ANDROID_LOG_ERROR, "UART", "msg is null");
        LOGE("msg is null!");
        return -2; // 定义明确错误码
    }

    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    jboolean  isCopy = true;
// 获取数组长度
    jsize length = (env)->GetArrayLength(msg);
    // 获取数据
    jbyte *byteArray = (env)->GetByteArrayElements(msg,&isCopy);
    if (byteArray == NULL) {
        perror("get bytearray data error!");
        return -1; // 获取元素失败
    }
    jint ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)byteArray,length);
    env->ReleaseByteArrayElements(msg, byteArray, 0);
    return ret;
}



extern "C" JNIEXPORT void JNICALL
Java_com_hndl_serialport_SerialPortUtils_setWorkModeFunJNI(
        JNIEnv* env,
        jobject jobj /* this */,
        jint mode) {

    protocol_mode = mode;
}




extern "C" JNIEXPORT void JNICALL
        Java_com_hndl_serialport_SerialPortUtils_stopUartTaskJNI(
                JNIEnv* env,
                jobject jobj /* this */,
                jboolean run){
        if(run){

        } else {
            run_state = false;
            while(isWorkOk);
        }

}



extern "C" JNIEXPORT jboolean JNICALL
        Java_com_hndl_serialport_SerialPortUtils_getWorkFlag(
        JNIEnv* env,
        jobject jobj /* this */){
        jboolean work = isWorkOk;
        return work;
}


/*************************************************************************************************************/

extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_sendHeartBeat(
        JNIEnv* env,
        jobject jobj /* this */,
        jint heartBeat){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = protocol_heart_beat(msgSendBuf,heartBeat,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_enterLowPower(
        JNIEnv* env,
        jobject jobj /* this */){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = response_low_power(msgSendBuf,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}


//int open_can_port(uint8_t * buf, int32_t can_index,int32_t baud,uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_openCanPort(
        JNIEnv* env,
        jobject jobj /* this */,
        jint canIndex,
        jint baud){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = open_can_port(msgSendBuf,canIndex,baud,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}


//int can_close_port(uint8_t * buf,int32_t can_index,uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_closeCanPort(
        JNIEnv* env,
        jobject jobj /* this */,
        jint canIndex){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_close_port(msgSendBuf,canIndex,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}

//int can_filter_list_16bit(uint8_t *buf,int32_t can_index,int32_t bank,uint16_t id0,uint16_t id1,uint16_t id2,uint16_t id3,uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_setCanFilterListWith16bit(
        JNIEnv* env,
        jobject jobj /* this */,
        jint canIndex,
        jint bank,
        jint id0,
        jint id1,
        jint id2,
        jint id3){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_filter_list_16bit(msgSendBuf,canIndex,bank,id0,id1,id2,id3,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}

//int can_filter_list_32bit(uint8_t *buf,int32_t can_index,int32_t bank,uint32_t id0,uint32_t id1,uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_setCanFilterListWith32bit(
        JNIEnv* env,
        jobject jobj /* this */,
        jint canIndex,
        jint bank,
        jint id0,
        jint id1){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_filter_list_32bit(msgSendBuf,canIndex,bank,id0,id1,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}

//int can_filter_mask_16bit(uint8_t *buf,int32_t can_index,int32_t bank,uint16_t id0,uint16_t id0_mask,uint16_t id1,uint16_t id1_mask,uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_setCanFilterMaskWith16bit(
        JNIEnv* env,
        jobject jobj /* this */,
        jint canIndex,
        jint bank,
        jint id0,
        jint id0Mask,
        jint id1,
        jint id1Mask){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_filter_mask_16bit(msgSendBuf,canIndex,bank,id0,id0Mask,id1,id1Mask,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}


//int can_filter_mask_32bit(uint8_t *buf,int32_t can_index,int32_t bank,uint32_t id0,uint32_t id0_mask,uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_setCanFilterMaskWith32bit(
        JNIEnv* env,
        jobject jobj /* this */,
        jint canIndex,
        jint bank,
        jint id0,
        jint id0Mask){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_filter_mask_32bit(msgSendBuf,canIndex,bank,id0,id0Mask,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}


//int can_send_data(uint8_t *buf,int32_t can_index,int32_t can_id,uint32_t id_type,const uint8_t *data,uint8_t len,uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_sendCanData(
        JNIEnv* env,
        jobject jobj /* this */,
        jint canIndex,
        jint canId,
        jint frameType,
        jint dataLen,
        jbyteArray data){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    if (data == nullptr) {
//        __android_log_print(ANDROID_LOG_ERROR, "UART", "msg is null");
        LOGE("data is null!");
        return -2; // 定义明确错误码
    }

    jboolean  isCopy = true;
// 获取数组长度
    jsize length_data = (env)->GetArrayLength(data);
    // 获取数据
    jbyte *byteArray = (env)->GetByteArrayElements(data,&isCopy);
    if (byteArray == NULL) {
        perror("get bytearray data error!");
        return -1; // 获取元素失败
    }



    int length = can_send_data(msgSendBuf,canIndex,canId,frameType,(const uint8_t *)byteArray,dataLen,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    env->ReleaseByteArrayElements(data, byteArray, 0);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}


//int change_usb_type(uint8_t * buf, uint32_t usb_type, uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_changeUsbMode(
        JNIEnv* env,
        jobject jobj /* this */,
        jint usbMode){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = change_usb_type(msgSendBuf,usbMode,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}


//int can_config_rs(uint8_t * buf, uint32_t can_index,uint32_t has_rs, uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_configCanRs(
        JNIEnv* env,
        jobject jobj /* this */,
        jint canIndex,
        jint hasRs){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_config_rs(msgSendBuf,canIndex,hasRs,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}

//int get_usb_type(uint8_t * buf, uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_sendGetUsbModeMsg(
        JNIEnv* env,
        jobject jobj /* this */){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = get_usb_type(msgSendBuf,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}

//int get_can_rs(uint8_t * buf,uint32_t can_index, uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_sendGetCanRsMsg(
        JNIEnv* env,
        jobject jobj /* this */,
        jint canIndex){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = get_can_rs(msgSendBuf,canIndex,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}


//int get_gpio_value(uint8_t * buf,uint32_t gpio_index, uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_sendGetGpioValueMsg(
        JNIEnv* env,
        jobject jobj /* this */,
        jint gpioIndex){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = get_gpio_value(msgSendBuf,gpioIndex,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}

//int set_timer_task(uint8_t * buf,uint32_t timer_index, uint32_t task_type,timer_s * pnow,timer_s * paim ,uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_openTimerTask(
        JNIEnv* env,
        jobject jobj /* this */,
        jint timerId,
        jint taskType,
        jint currentYear,
        jint currentMon,
        jint currentDay,
        jint currentHour,
        jint currentMin,
        jint currentSec,
        jint aimYear,
        jint aimMon,
        jint aimDay,
        jint aimHour,
        jint aimMin,
        jint aimSec){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    timer_s current,aim;
    current.year = currentYear;
    current.month = currentMon;
    current.day = currentDay;
    current.hour = currentHour;
    current.minute = currentMin;
    current.second = currentSec;

    aim.year = aimYear;
    aim.month = aimMon;
    aim.day = aimDay;
    aim.hour = aimHour;
    aim.minute = aimMin;
    aim.second = aimSec;

    int length = set_timer_task(msgSendBuf,timerId,taskType,&current,&aim,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}



//int cancel_timer_task(uint8_t * buf,uint32_t timer_index, uint32_t max_len)
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_cancelTimerTask(
        JNIEnv* env,
        jobject jobj /* this */,
        jint timerId){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = cancel_timer_task(msgSendBuf,timerId,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}


//int get_coprocessor_version(uint8_t * buf, uint32_t max_len);
extern "C" JNIEXPORT jint JNICALL
Java_com_hndl_serialport_SerialPortUtils_getCoprocessorVersion(
        JNIEnv* env,
        jobject jobj /* this */){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = get_coprocessor_version(msgSendBuf,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (jint)ret;
    }
}


#endif  // #define ANDROID_JAVA_PLATFORM





#ifdef LINUX_C_PLATFORM


_callback_interface_s * gp_callback_interface = NULL;


void set_callback_interface(_callback_interface_s * p){
    gp_callback_interface = p;
}



void analytical_msg_task(uint8_t * msg,int msg_len){


    // 1. 输入参数校验
    if (msg_len < 0) {
        LOGD("Invalid msg_len: %d", msg_len);
        return;
    }
    if (msg_len > 0 && msg == nullptr) {
        LOGD("msg is null but msg_len > 0");
        return;
    }

    switch(msg[1]) {
        case 0x30: {
            int heartBeat = get_num_from_asc(msg[4]);
            if(gp_callback_interface!= NULL && gp_callback_interface->heart_beat_callback != NULL){
                gp_callback_interface->heart_beat_callback(heartBeat);
            }
        }
            break;
        case 0x31: {
            // 调用 Java 的 invokeCallback 方法
            if(gp_callback_interface!= NULL && gp_callback_interface->request_sleep_callback != NULL){
                gp_callback_interface->request_sleep_callback();
            }
        }
            break;
        case 0x33: {
            int canIndex = 0;
            int canOpt = msg[5];
            int canBaud = 0;
            int resultValue = 0;
            if (msg[4] == 0x30) {
                canIndex = 0;
            } else if (msg[4] == 0x31) {
                canIndex = 1;
            }
            if (canOpt == 0x30) {
                canBaud = (int) get_num_from_asc_array(&msg[6], 4);
                resultValue = get_num_from_asc(msg[10]);
                if(gp_callback_interface!= NULL && gp_callback_interface->can_open_callback != NULL){
                    gp_callback_interface->can_open_callback(canIndex, canBaud, resultValue);
                }

            } else {
                resultValue = get_num_from_asc(msg[6]);
                if(gp_callback_interface!= NULL && gp_callback_interface->can_close_callback != NULL){
                    gp_callback_interface->can_close_callback(canIndex, resultValue);
                }
            }
        }
            break;
        case 0x34: {
            int canIndex = get_num_from_asc(msg[4]);
            int filterBank = (int) get_num_from_asc_array(&msg[5], 2);
            int filterMode = (int) get_num_from_asc(msg[7]);
            int filterBits = msg[8] == 0x30 ? 16 : 32;
            int resultValue = get_num_from_asc(msg[25]);
            if(gp_callback_interface!= NULL && gp_callback_interface->can_filter_callback != NULL){
                gp_callback_interface->can_filter_callback(canIndex, filterBank, filterMode,filterBits, resultValue);
            }
        }
            break;
        case 0x35: {
            int canIndex = get_num_from_asc(msg[4]);
            int canLen = (int) get_num_from_asc(msg[5]);
            uint32_t canIDTemp = get_num_from_asc_array(&msg[6], 8);
            int canID = (int) (canIDTemp & 0x7FFFFFFF);
            int frameType = 0;
            uint8_t canMsgData[8];
            if (canIDTemp & 0x80000000) {
                frameType = 1;
            } else {
                frameType = 0;
            }

            for (int i = 0; i != 8; i++) {
                canMsgData[i] = get_num_from_asc(msg[2 * i + 14]) +
                                (get_num_from_asc(msg[2 * i + 1 + 14]) << 4);
            }

            int resultValue = get_num_from_asc(msg[30]);
            if(gp_callback_interface!= NULL && gp_callback_interface->can_send_callback != NULL){
                gp_callback_interface->can_send_callback(canIndex, canLen, canID, frameType,resultValue,canMsgData);
            }

        }
            break;
        case 0x36: {
            int canIndex = get_num_from_asc(msg[4]);
            int canLen = (int) get_num_from_asc(msg[5]);
            uint32_t canIDTemp = get_num_from_asc_array(&msg[6], 8);
            int canID = (int) (canIDTemp & 0x7FFFFFFF);
            int frameType = 0;
            uint8_t canMsgData[8];
            if (canIDTemp & 0x80000000) {
                frameType = 1;
            } else {
                frameType = 0;
            }

            for (int i = 0; i != 8; i++) {
                canMsgData[i] = get_num_from_asc(msg[2 * i + 14]) +
                                (get_num_from_asc(msg[2 * i + 1 + 14]) << 4);
            }

            if(gp_callback_interface!= NULL && gp_callback_interface->can_recv_data_callback != NULL){
                gp_callback_interface->can_recv_data_callback(canIndex, canLen, canID, frameType,canMsgData);
            }

        }
            break;
        case 0x39: {
            int usbType = get_num_from_asc(msg[4]);
            if(gp_callback_interface!= NULL && gp_callback_interface->usb_mode_callback != NULL){
                gp_callback_interface->usb_mode_callback(usbType);
            }

        }
            break;
        case 0x3A: {
            int canIndex = get_num_from_asc(msg[4]);
            int hasRs = get_num_from_asc(msg[5]);
            if(gp_callback_interface!= NULL && gp_callback_interface->can_rs_callback != NULL){
                gp_callback_interface->can_rs_callback(canIndex,hasRs);
            }
        }
            break;
        case 0x3B:
        {
            int canIndex = get_num_from_asc(msg[4]);
            int canResetState = get_num_from_asc(msg[5]);
            if(gp_callback_interface!= NULL && gp_callback_interface->can_error_callback != NULL){
                gp_callback_interface->can_error_callback(canIndex,canResetState);
            }
        }
        break;
        case 0x3D:
        {
            int gpioIndex = get_num_from_asc(msg[4]);
            int gpioValue = get_num_from_asc(msg[5]);
            if(gp_callback_interface!= NULL && gp_callback_interface->gpio_value_callback != NULL){
                gp_callback_interface->gpio_value_callback(gpioIndex, gpioValue);
            }

        }
        break;
        case 0x3E:
        {
            int timerIndex = get_num_from_asc(msg[4]);
            int timerResult = get_num_from_asc(msg[5]);
            if(gp_callback_interface!= NULL && gp_callback_interface->set_timer_callback != NULL){
                gp_callback_interface->set_timer_callback(timerIndex,timerResult);
            }
        }
            break;
        case 0x40:
        {
            int timerIndex = get_num_from_asc(msg[4]);
            int timerResult = get_num_from_asc(msg[5]);
            if(gp_callback_interface!= NULL && gp_callback_interface->cancel_timer_callback != NULL){
                gp_callback_interface->cancel_timer_callback(timerIndex,timerResult);
            }
        }
        break;
        case 0x41:
        {

            uint8_t version[5];

            for (int i = 0; i != 5; i++) {
                version[i] = msg[4+i];
            }
            if(gp_callback_interface!= NULL && gp_callback_interface->get_mcu_app_version_callback != NULL){
                gp_callback_interface->get_mcu_app_version_callback(version,5);
            }

        }
        break;
        default:
            break;
    }

}


int startUartTask(const char * port){
    int ret = uart_task_start(port);
    return ret;
}

int sendUartMsg(uint8_t * p,int len){
    if(thread_arg.psend_buf == NULL){
        return -1;
    }
    int ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)p,len);
    return ret;
}


void setWorkMode(int mode){
    protocol_mode = mode;
}

void stopUartTask(void){
    run_state = false;
    while(isWorkOk);
}

int getWorkState(void){
    return isWorkOk;
}

/***********************************************************************************/

int sendHeartBeat(int heartBeat){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = protocol_heart_beat(msgSendBuf,heartBeat,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}

int enterLowPower(void){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = response_low_power(msgSendBuf,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}


int openCanPort(int canIndex,int baud){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = open_can_port(msgSendBuf,canIndex,baud,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}


int closeCanPort(int canIndex){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_close_port(msgSendBuf,canIndex,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}

int setCanFilterListWith16bit(
        int canIndex,
        int bank,
        int id0,
        int id1,
        int id2,
        int id3){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_filter_list_16bit(msgSendBuf,canIndex,bank,id0,id1,id2,id3,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}

int setCanFilterListWith32bit(
        int canIndex,
        int bank,
        int id0,
        int id1){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_filter_list_32bit(msgSendBuf,canIndex,bank,id0,id1,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}

int setCanFilterMaskWith16bit(
        int canIndex,
        int bank,
        int id0,
        int id0Mask,
        int id1,
        int id1Mask){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_filter_mask_16bit(msgSendBuf,canIndex,bank,id0,id0Mask,id1,id1Mask,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}


int setCanFilterMaskWith32bit(
        int canIndex,
        int bank,
        int id0,
        int id0Mask){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_filter_mask_32bit(msgSendBuf,canIndex,bank,id0,id0Mask,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}


int sendCanData(
        int canIndex,
        int canId,
        int frameType,
        int dataLen,
        const uint8_t * pdata){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    if (pdata == NULL) {
        LOGE("data is null!");
        return -2; // 定义明确错误码
    }

    int length = can_send_data(msgSendBuf,canIndex,canId,frameType,pdata,dataLen,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}


int changeUsbMode(int usbMode){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = change_usb_type(msgSendBuf,usbMode,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}


int configCanRs(int canIndex,int hasRs){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = can_config_rs(msgSendBuf,canIndex,hasRs,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}

int sendGetUsbModeMsg(void){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = get_usb_type(msgSendBuf,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}

int sendGetCanRsMsg(int canIndex){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = get_can_rs(msgSendBuf,canIndex,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}


int sendGetGpioValueMsg(int gpioIndex){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = get_gpio_value(msgSendBuf,gpioIndex,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}

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
        int aimSec){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    timer_s current,aim;
    current.year = currentYear;
    current.month = currentMon;
    current.day = currentDay;
    current.hour = currentHour;
    current.minute = currentMin;
    current.second = currentSec;

    aim.year = aimYear;
    aim.month = aimMon;
    aim.day = aimDay;
    aim.hour = aimHour;
    aim.minute = aimMin;
    aim.second = aimSec;

    int length = set_timer_task(msgSendBuf,timerId,taskType,&current,&aim,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}



int cancelTimerTask(int timerId){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = cancel_timer_task(msgSendBuf,timerId,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}


int getCoprocessorVersion(void){
    uint8_t msgSendBuf[256];
    if(thread_arg.psend_buf == NULL){
        perror("uart not start!");
        return -1;
    }
    int length = get_coprocessor_version(msgSendBuf,256);
    if(length<0){
        return -1;
    }
    size_t ret = enqueue_once(thread_arg.psend_buf,(const uint8_t *)msgSendBuf,(size_t)length);
    if(ret == 0){
        return -1;
    } else {
        return (int)ret;
    }
}



#endif


