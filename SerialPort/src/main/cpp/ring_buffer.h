// Created by DL on 2024/10/22.
//

#ifndef CANDEMO_RING_BUFFER_H
#define CANDEMO_RING_BUFFER_H
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <stdint.h>
#include <string.h>
#include <pthread.h>

typedef struct {
    uint8_t *buffer;       // 存储数据的数组
    size_t head;           // 写入位置
    size_t tail;           // 读取位置
    size_t max;            // 缓冲区大小
    pthread_mutex_t lock;  // 互斥锁
} RingBuffer;

#ifdef __cplusplus
extern "C" {
#endif

extern RingBuffer* create_ring_buffer(size_t size);
extern size_t available_space(RingBuffer *rb);
extern size_t available_data(RingBuffer *rb);
extern size_t enqueue(RingBuffer *rb, const uint8_t *data, size_t length);
extern size_t dequeue(RingBuffer *rb, uint8_t *data, size_t length);
extern void destroy_ring_buffer(RingBuffer *rb);
extern bool enqueue_byte(RingBuffer *rb, uint8_t byte);
extern size_t enqueue_once(RingBuffer *rb, const uint8_t *data, size_t length);
extern bool dequeue_byte(RingBuffer *rb, uint8_t *byte);
bool dequeue_byte_with_no_mutex(RingBuffer *rb, uint8_t *byte);

#ifdef __cplusplus
}
#endif

#endif //CANDEMO_RING_BUFFER_H