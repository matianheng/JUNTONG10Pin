//
// Created by DL on 2024/10/22.
//

#ifndef CANDEMO_DL_UART_H
#define CANDEMO_DL_UART_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <errno.h>
#include <pthread.h>


extern int open_serial_port(const char *port);
extern int configure_serial_port(int serial_port, int baud_rate);
extern int read_from_serial_port(int serial_port, char *buffer, size_t size);
extern int write_to_serial_port(int serial_port, const char *data,size_t size);
extern void close_serial_port(int serial_port);
#endif //CANDEMO_DL_UART_H
