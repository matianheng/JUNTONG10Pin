// Created by DL on 2024/10/22.
//

#ifndef CANDEMO_UART_TASK_H
#define CANDEMO_UART_TASK_H


#define MAX_SEND_UART_DATA_LEN_PERS 512
#define UART_MAX_MSG_DATA_LEN 256
#define MAX_READ_UART_DATA_LEN_PERS 512
#include "ring_buffer.h"

#include "native-lib.h"
#ifdef ANDROID_JAVA_PLATFORM
#include "jni.h"
#endif


#define TRANSPARENT_MODE 0
#define ANALYTICAL_MODE 1

// 定义传递给线程的参数结构体
typedef struct {
    int serial_port;
    RingBuffer * precv_buf;
    RingBuffer * psend_buf;
#ifdef ANDROID_JAVA_PLATFORM
    JavaVM *javaVm;
    jobject obj;
#endif
} ThreadArgs;

#ifdef __cplusplus
extern "C" {
#endif

#ifdef ANDROID_JAVA_PLATFORM
extern int uart_task_start(JavaVM * javaVM, jobject obj,const char *port, int baud_rate);
extern void deal_msg_task(JNIEnv *env, jobject obj  ,uint8_t * msg,int msg_len);
extern void analytical_msg_task(JNIEnv *env, jobject obj  ,uint8_t * msg,int msg_len);
#endif

#ifdef LINUX_C_PLATFORM
extern int uart_task_start(const char * port);
extern void deal_msg_task(uint8_t * msg,int msg_len);
extern void analytical_msg_task(uint8_t * msg,int msg_len);
#endif

extern void write_baud_to_record_file(void);
extern void create_timer( long ns);
#ifdef __cplusplus
}
#endif

#endif //CANDEMO_UART_TASK_H