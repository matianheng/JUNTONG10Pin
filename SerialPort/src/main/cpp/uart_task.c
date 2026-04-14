//
// Created by DL on 2024/10/22.
//

#include "ring_buffer.h"
#include "pthread.h"

#include "dl_uart.h"

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

#include "uart_task.h"
#include <sched.h>
#include "msg_protocol.h"

#include <time.h>
#include <signal.h>
#include <unistd.h>

#include "native-lib.h"

#ifdef ANDROID_JAVA_PLATFORM
#include <jni.h>
#include "android/log.h"
static const char *TAG="serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
#endif

#ifdef LINUX_C_PLATFORM
#define LOGI(fmt, args...)
#define LOGD(fmt, args...)
#define LOGE(fmt, args...)
#endif


int baudArray[]={115200,921600};
int baudTraverseFlag = 0;
int baudTraverseIndex = -1;
int baud_rate_try = 0;
int thread_flag =0;

pthread_t thread_recv = -1;
pthread_t thread_send = -1;

bool run_state = true;
timer_t timer_id;
int timer_created = 0;
int timer_deal_flag = 0;
extern uint8_t isWorkOk ;


uint8_t protocol_mode = TRANSPARENT_MODE;


//int get_num_from_asc(uint8_t input){
//    if(input>=0x30 && input <=0x39){
//        return (input - 0x30);
//    } else if(input >= 0x41 && input <= 0x46){
//        return (input- 0x41 + 10);
//    } else if(input >= 0x61 && input <= 0x66){
//        return ( input - 0x61 + 10);
//    } else {
//        return -1;
//    }
//}


#ifdef ANDROID_JAVA_PLATFORM
ThreadArgs thread_arg = {
        .serial_port = -1,
        .precv_buf = NULL,
        .psend_buf = NULL,
        .javaVm = NULL,
        .obj = NULL
};
#endif

#ifdef  LINUX_C_PLATFORM
ThreadArgs thread_arg = {
        .serial_port = -1,
        .precv_buf = NULL,
        .psend_buf = NULL
};
#endif



// 线程函数
void* thread_recv_function(void* args) {
    ThreadArgs *thread_args = (ThreadArgs *)args;
    // 这里可以添加其他处理逻辑
    uint8_t uart_msg_buf[UART_MAX_MSG_DATA_LEN];
    int uart_msg_count = 0;
    int real_size = 0;
    int msg_len = 0;
    uint8_t temp_buf[MAX_READ_UART_DATA_LEN_PERS];
    uint8_t temp_byte;

#ifdef ANDROID_JAVA_PLATFORM
    JNIEnv *env;
    JavaVM *javaVM = thread_args->javaVm;
    //附加当前线程到 JVM
    if((*javaVM)->AttachCurrentThread(javaVM, (void **)&env, NULL) != 0){
        thread_flag =1;
        run_state = false;
        LOGD("A\n");
        return NULL;
    }
#endif

    uint8_t temp_msg_buf[128];
    int len = protocol_heart_beat(temp_msg_buf,'0',128);
    if(len != -1){
//        thread_flag =1;
//        run_state =false;

//        enqueue_once(thread_arg.psend_buf,(const uint8_t *)temp_msg_buf,len);
    } else {
        thread_flag =1;
        run_state =false;
        LOGD("B\n");
    }
    int ret = write_to_serial_port(thread_args->serial_port,temp_msg_buf,len);
    if(ret < 0){ // uart write fail, 可以做一些错误处理。比如关闭串口，重新打开。
        thread_flag =1;
        run_state= false;
        LOGD("C\n");
    }
    timer_created = 0;
    create_timer(100000000); // 打开定时器200ms
    if(timer_created == 0){
//        return NULL;  // 定时器创建失败
        thread_flag =1;
        run_state= false;
        LOGD("D\n");
        pthread_exit(NULL); // 结束线程
    }
    timer_deal_flag = 1;

    thread_flag =1;
    while(1){
        real_size = read_from_serial_port(thread_args->serial_port,temp_buf,MAX_READ_UART_DATA_LEN_PERS);
        if(real_size>0){
//            LOGD("recvsize=%d\n",real_size);
            int real_enqueue_size = enqueue(thread_args->precv_buf,temp_buf,real_size);
            if(real_enqueue_size != real_size){
                perror("enqueue recv data fail!");
            }
            pthread_mutex_lock(&thread_args->precv_buf->lock);
            while(dequeue_byte_with_no_mutex(thread_args->precv_buf,&temp_byte)){
                if(temp_byte == 0x02){
                    uart_msg_count = 0;
                }
                if(uart_msg_count>= UART_MAX_MSG_DATA_LEN){
                    uart_msg_count =0;
                }
                uart_msg_buf[uart_msg_count++] = temp_byte;
                if(uart_msg_buf[0] == 0x02){

                    if(uart_msg_count <4){
                        msg_len = 0;
                    } else
                    if(uart_msg_count == 4){
                        int ret = get_num_from_asc(uart_msg_buf[2]);
                        if(ret == -1){
                            uart_msg_count =0;
                            continue;
                        } else {
                            msg_len+= ret;
                        }
                        ret = get_num_from_asc(uart_msg_buf[3]);
                        if(ret == -1){
                            uart_msg_count =0;
                            continue;
                        } else {
                            msg_len += (ret <<4);
                        }
                        if(msg_len >UART_MAX_MSG_DATA_LEN -7){
                            perror("msg parse len error!");
                            uart_msg_count =0;
                            continue;
                        }
                    } else if(uart_msg_count >= 7 + msg_len){
                        if(uart_msg_buf[4+msg_len] == 0x03){
                            // now we can deal this msg
                            isWorkOk = 1;
                            /********** deal msg ***********/
                            if(timer_created == 1){
                                timer_deal_flag = 0;
//                                if(timer_id != NULL) {
//                                    timer_delete(timer_id);
//                                    timer_id = NULL;
//                                }
                                timer_created = 0;
                                if(baudTraverseFlag){
                                    baudTraverseFlag = 0;
                                    baudTraverseIndex = -1;
                                    // 将波特率写入到记录文件。
                                    write_baud_to_record_file();
                                }
                            }
                            if(protocol_mode == TRANSPARENT_MODE){
#ifdef ANDROID_JAVA_PLATFORM
                                deal_msg_task(env,thread_args->obj,uart_msg_buf,uart_msg_count);
#endif
#ifdef LINUX_C_PLATFOR
                                deal_msg_task(uart_msg_buf,uart_msg_count);
#endif
                            } else {
#ifdef ANDROID_JAVA_PLATFORM
                                analytical_msg_task(env,thread_args->obj,uart_msg_buf,uart_msg_count);
#endif
#ifdef LINUX_C_PLATFOR
                                analytical_msg_task(uart_msg_buf,uart_msg_count);
#endif
                            }

                            /********** deal msg ***********/
                            uart_msg_count = 0;
                        } else {
                            uart_msg_count = 0;
                        }
                    }
                } else{
                    uart_msg_count =0;
                }
            }
            pthread_mutex_unlock(&thread_args->precv_buf->lock);
        }
        if(run_state == false){

            break;
        }
    }
#ifdef ANDROID_JAVA_PLATFORM
    (*javaVM)->DetachCurrentThread(javaVM);
#endif

//    pthread_exit(NULL); // 结束线程
}



// 线程函数
void* thread_send_function(void* args) {
    ThreadArgs *thread_args = (ThreadArgs *)args;
    size_t avail_data_size;
    uint8_t temp_buf[MAX_SEND_UART_DATA_LEN_PERS];
    size_t real_size;

//    JNIEnv *env;
//    JavaVM *javaVM = thread_args->javaVm;
//    //附加当前线程到 JVM
//    if((*javaVM)->AttachCurrentThread(javaVM, (void **)&env, NULL) != 0){
//        return NULL;
//    }
    // 这里可以添加其他处理逻辑
    while(!thread_flag);  // 等待线程同步
    while(1){

        real_size = dequeue(thread_args->psend_buf,temp_buf,MAX_SEND_UART_DATA_LEN_PERS);
        if(real_size){
            int ret = write_to_serial_port(thread_args->serial_port,temp_buf,real_size);
            if(ret < 0){ // uart write fail, 可以做一些错误处理。比如关闭串口，重新打开。

            }
        }

        usleep(10);
        if(run_state == false){
            break;
        }
    }
//    pthread_exit(NULL); // 结束线程
}


//
//int main() {
//    pthread_t thread;
//    ThreadArgs args;
//
//    // 设置参数
//    args.param1 = 42;
//    args.param2 = 3.14;
//    args.param3 = "Hello, World!";
//
//    // 创建线程
//    if (pthread_create(&thread, NULL, thread_function, (void *)&args) != 0) {
//        perror("Failed to create thread");
//        return EXIT_FAILURE;
//    }
//
//    // 等待线程结束
//    pthread_join(thread, NULL);
//
//    return EXIT_SUCCESS;
//}


void timer_handler(int sig) {
    if(timer_deal_flag) {

        //    printf("定时器到期，执行任务。\n");
        //定时器触发了，说明当前的这个波特率的配置下，没有接收到数据
        int max_baud_array = sizeof(baudArray) / sizeof(int);
        timer_created = 0;
        run_state = false;  // 退出两个线程。
        baudTraverseFlag = 1;
        if (baudTraverseIndex == -1) {
            baudTraverseIndex = 0;
            baud_rate_try = baudArray[baudTraverseIndex];
        } else {
            baudTraverseIndex += 1;
            if (baudTraverseIndex >= max_baud_array) {
                baudTraverseFlag = 0;
            } else {
                baud_rate_try = baudArray[baudTraverseIndex];
            }
        }
//        timer_delete(timer_id);
//        timer_id = NULL;
//        pthread_cancel(thread_recv);
//        pthread_cancel(thread_send);
        LOGD("timer handler do !");
    }

}


void create_timer( long ns) {
    struct sigaction sa;
    struct sigevent sev;
    struct itimerspec timer;

    // 设置定时器处理函数
    sa.sa_handler = timer_handler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
    if (sigaction(SIGRTMIN, &sa, NULL) == -1) {
        perror("sigaction");
        return;
    }

    // 设置sigevent结构
    sev.sigev_notify = SIGEV_SIGNAL;
    sev.sigev_signo = SIGRTMIN;

    // 创建定时器
    if (timer_create(CLOCK_REALTIME, &sev, &timer_id) == -1) {
        perror("timer_create");
        return;
    }
    timer_created = 1;

    // 设置定时器的初始值
    timer.it_value.tv_sec = ns / 1000000000;
    timer.it_value.tv_nsec = ns % 1000000000;
    timer.it_interval.tv_sec = 0; // Set to non-zero for repeated intervals
    timer.it_interval.tv_nsec = 0; // Set to non-zero for repeated intervals

    // 启动定时器
    if (timer_settime(timer_id, 0, &timer, NULL) == -1) {
        perror("timer_settime");
        return;
    }
}

void write_baud_to_record_file(void){
    // 以写入模式打开文件，这将清空文件内容
    FILE *file = fopen("/sdcard/baudRecord.txt", "w");
    if (file == NULL) {
        perror("无法打开文件");
//        exit(EXIT_FAILURE);
        return ;
    }

    // 写入数据到文件
    fprintf(file, "%d\n", baud_rate_try);

    // 关闭文件
    fclose(file);
}

#ifdef ANDROID_JAVA_PLATFORM
int uart_task_start(JavaVM * javaVM, jobject obj,const char *port, int baud_rate){
#endif

#ifdef LINUX_C_PLATFORM
int uart_task_start(const char * port){
#endif


    struct sched_param param;
    int policy;
    baud_rate_try = 0;
    baudTraverseIndex  = -1;
    baudTraverseFlag = 0;
    isWorkOk = 0;

    FILE *recordFile;
    recordFile = fopen("/sdcard/baudRecord.txt", "r"); // 打开文件用于读取
    if (recordFile == NULL) {
        perror("无法打开文件baudRecord.txt");
        // 文件不存在，走遍历尝试流程。
        baudTraverseFlag = 1;
        baudTraverseIndex = 0;
        baud_rate_try = baudArray[baudTraverseIndex];
    } else {
        int ret_temp = fscanf(recordFile,"%d",&baud_rate_try);
        if(ret_temp == -1){
            perror("读取baud record error!");
            // 文件不存在，走遍历尝试流程。
            baudTraverseFlag = 1;
            baudTraverseIndex = 0;
            baud_rate_try = baudArray[baudTraverseIndex];
        } else {
            int baudArrayNum = sizeof(baudArray)/sizeof(int);
            int index_temp = 0;
            for(index_temp = 0 ; index_temp != baudArrayNum; index_temp++){
                if(baud_rate_try == baudArray[index_temp]){
                    break;
                }
            }
            if( index_temp == baudArrayNum){ // 读取的数值不在我的列表中。
                perror("读取baud record value error!");
                // 文件不存在，走遍历尝试流程。
                baudTraverseFlag = 1;
                baudTraverseIndex = 0;
                baud_rate_try = baudArray[baudTraverseIndex];
            }
        }
        fclose(recordFile);
    }




#ifdef ANDROID_JAVA_PLATFORM
    thread_arg.javaVm = javaVM;
    thread_arg.obj = obj;
#endif


    do {
        if(thread_arg.serial_port >= 0){
            close_serial_port(thread_arg.serial_port);
            thread_arg.serial_port = -1;
        }
        thread_arg.serial_port = open_serial_port(port);
        if (thread_arg.serial_port < 0) {
            return EXIT_FAILURE;
        }
        int ret = configure_serial_port(thread_arg.serial_port,baud_rate_try);
        if(ret < 0){
            close_serial_port(thread_arg.serial_port);
            return EXIT_FAILURE;
        }
        if(thread_arg.precv_buf){
            destroy_ring_buffer(thread_arg.precv_buf);
            thread_arg.precv_buf = NULL;
        }
        if(thread_arg.psend_buf){
            destroy_ring_buffer(thread_arg.psend_buf);
            thread_arg.psend_buf = NULL;
        }
        thread_arg.precv_buf = create_ring_buffer(1024*10);
        thread_arg.psend_buf = create_ring_buffer(1024*10);

//        uint8_t temp_msg_buf[128];
//        int len = protocol_heart_beat(temp_msg_buf,'0',128);
//        if(len != -1){
//            enqueue_once(thread_arg.psend_buf,(const uint8_t *)temp_msg_buf,len);
//        }

//        timer_created = 0;
//        create_timer(900000000); // 打开定时器200ms
//        if(timer_created == 0){
//            return EXIT_FAILURE;  // 定时器创建失败
//        }
//        timer_deal_flag = 1;
        LOGD("the thread is create!++++++++++++++++++++++++++++++++++++");
        thread_flag = 0;
        pthread_create(&thread_recv, NULL, thread_recv_function, &thread_arg);
        pthread_create(&thread_send, NULL, thread_send_function, &thread_arg);

        run_state = true;

        // 设置调度策略为 SCHED_FIFO，并设置优先级
        policy = SCHED_FIFO; // 或者 SCHED_OTHER, SCHED_RR
        param.sched_priority = 80; // 优先级范围 1-99，具体取决于策略


        // 设置线程的调度策略和优先级
        if (pthread_setschedparam(thread_recv, policy, &param) != 0) {
            perror("Failed to set thread scheduling parameters");
        }
        if (pthread_setschedparam(thread_send, policy, &param) != 0) {
            perror("Failed to set thread scheduling parameters");
        }

        if(pthread_setname_np(thread_recv,"serial_recv")!=0){
            perror("failed to set thread_recv name!");
        }


        if(pthread_setname_np(thread_send,"serial_send")!=0){
            perror("failed to set thread_send name!");
        }

        pthread_join(thread_recv,NULL);
        pthread_join(thread_send,NULL);
        LOGD("the thread is over!++++++++++++++++++++++++++++++++++++");

    }while(baudTraverseFlag);
//    if(baudTraverseFlag){
//        destroy_ring_buffer(thread_arg.precv_buf);
//        thread_arg.precv_buf = NULL;
//        destroy_ring_buffer(thread_arg.psend_buf);
//        thread_arg.psend_buf = NULL;
//        goto CONFIG_SERIAL;
//    }

    LOGD("the thread is game over!++++++++++++++++++++++++++++++++++++");
    close_serial_port(thread_arg.serial_port);
    thread_arg.serial_port = -1;
    destroy_ring_buffer(thread_arg.precv_buf);
    thread_arg.precv_buf = NULL;
    destroy_ring_buffer(thread_arg.psend_buf);
    thread_arg.psend_buf = NULL;

    isWorkOk = 0;
    return EXIT_SUCCESS;
}


