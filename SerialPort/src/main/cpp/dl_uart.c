//
// Created by DL on 2024/10/22.
//
#include "dl_uart.h"
pthread_mutex_t write_lock;

#ifdef ANDROID_JAVA_PLATFORM
#include "android/log.h"
static const char *TAG="serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
#endif

static speed_t getBaudrate(int baudrate)
{
    switch(baudrate) {
        case 0: return B0;
        case 50: return B50;
        case 75: return B75;
        case 110: return B110;
        case 134: return B134;
        case 150: return B150;
        case 200: return B200;
        case 300: return B300;
        case 600: return B600;
        case 1200: return B1200;
        case 1800: return B1800;
        case 2400: return B2400;
        case 4800: return B4800;
        case 9600: return B9600;
        case 19200: return B19200;
        case 38400: return B38400;
        case 57600: return B57600;
        case 115200: return B115200;
        case 230400: return B230400;
        case 460800: return B460800;
        case 500000: return B500000;
        case 576000: return B576000;
        case 921600: return B921600;
        case 1000000: return B1000000;
        case 1152000: return B1152000;
        case 1500000: return B1500000;
        case 2000000: return B2000000;
        case 2500000: return B2500000;
        case 3000000: return B3000000;
        case 3500000: return B3500000;
        case 4000000: return B4000000;
        default: return -1;
    }
}

int open_serial_port(const char *port) {
    int serial_port = open(port, O_RDWR | O_NOCTTY | O_NDELAY);
    if (serial_port < 0) {
        perror("Failed to open the serial port");
        return -1;
    }
    pthread_mutex_init(&write_lock, NULL);
    return serial_port;
}

int configure_serial_port(int serial_port, int baud_rate) {
    struct termios options;
    if (tcgetattr(serial_port, &options) != 0) {
        perror("Failed to get serial port attributes");
//        LOGD("%s","Failed to get serial port attributes");
        return -1;
    }
    speed_t baud_real = getBaudrate(baud_rate);
    // 设置波特率


    cfmakeraw(&options);
    // 8数据位, 无校验, 1停止位
    options.c_cflag &= ~PARENB; // 无校验
    options.c_cflag &= ~CSTOPB; // 1停止位
    options.c_cflag &= ~CSIZE;   // 清除数据位掩码
    options.c_cflag |= CS8;       // 8数据位

    // 设置本地连接和启用接收
//    options.c_cflag |= (CLOCAL | CREAD);
    options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG); // 非规范模式，关闭回显
    options.c_oflag &= ~OPOST; // 关闭输出处理

    // 设置超时
    options.c_cc[VMIN] = 1;  // 不等待
    options.c_cc[VTIME] = 1; // 设置超时为1秒
    cfsetispeed(&options, baud_real);
    cfsetospeed(&options, baud_real);
    // 应用设置
    if (tcsetattr(serial_port, TCSANOW, &options) != 0) {
        perror("Failed to set serial port attributes");
        return -1;
    }
    return 0;
}

int read_from_serial_port(int serial_port, char *buffer, size_t size) {
    int num_bytes = read(serial_port, buffer, size);
    if (num_bytes < 0) {
        perror("Error reading from the serial port");
//        LOGD("%s","Error reading from the serial port");
        return -1;
    }
    return num_bytes;
}

int write_to_serial_port(int serial_port, const char *data,size_t size) {
    pthread_mutex_lock(&write_lock);
    int bytes_written = write(serial_port, data, size);
    if (bytes_written < 0) {
        perror("Error writing to the serial port");
        pthread_mutex_unlock(&write_lock);
        return -1;
    }
    pthread_mutex_unlock(&write_lock);
    return bytes_written;
}

void close_serial_port(int serial_port) {
    pthread_mutex_destroy(&write_lock);
    close(serial_port);
}



