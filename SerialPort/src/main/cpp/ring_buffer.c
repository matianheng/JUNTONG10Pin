//
// Created by DL on 2024/10/22.
//
#include "ring_buffer.h"



#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <stdint.h>
#include <string.h>
#include <pthread.h>


// 初始化环形缓冲区
RingBuffer* create_ring_buffer(size_t size) {
    RingBuffer *rb = (RingBuffer *)malloc(sizeof(RingBuffer));
    rb->buffer = (uint8_t *)malloc(size * sizeof(uint8_t));
    rb->max = size;
    rb->head = 0;
    rb->tail = 0;
    pthread_mutex_init(&rb->lock, NULL); // 初始化互斥锁
    return rb;
}

// 获取当前可用空间
size_t available_space(RingBuffer *rb) {
    if(rb == NULL){
        return 0; // 缓冲区满
    }
    return (rb->tail - rb->head - 1 + rb->max) % rb->max;
}

// 获取当前有效数据
size_t available_data(RingBuffer *rb) {
    if(rb == NULL){
        return 0; // 缓冲区满
    }
    return (rb->head - rb->tail + rb->max) % rb->max;
}

// 入队多个数据
size_t enqueue(RingBuffer *rb, const uint8_t *data, size_t length) {
    if(rb == NULL){
        return 0; // 缓冲区满
    }
    pthread_mutex_lock(&rb->lock); // 加锁

    size_t space = available_space(rb);
    if (space == 0) {
        pthread_mutex_unlock(&rb->lock); // 解锁
        return 0; // 缓冲区满
    }

    size_t to_enqueue = (length < space) ? length : space;
    size_t head_index = rb->head % rb->max;

    // 处理分段写入
    if (to_enqueue + head_index > rb->max) {
        size_t first_part = rb->max - head_index;
        memcpy(&rb->buffer[head_index], data, first_part);
        memcpy(rb->buffer, &data[first_part], to_enqueue - first_part);
    } else {
        memcpy(&rb->buffer[head_index], data, to_enqueue);
    }

    rb->head = (rb->head + to_enqueue) % rb->max; // 更新头指针
    pthread_mutex_unlock(&rb->lock); // 解锁
    return to_enqueue; // 返回实际入队的数量
}



// 入队多个数据,必须一次入队，否则返回错误。
size_t enqueue_once(RingBuffer *rb, const uint8_t *data, size_t length) {
    if(rb == NULL){
        return 0; // 缓冲区满
    }
    pthread_mutex_lock(&rb->lock); // 加锁

    size_t space = available_space(rb);
    if (space < length) {
        pthread_mutex_unlock(&rb->lock); // 解锁
        return 0; // 缓冲区满
    }

    size_t to_enqueue = (length < space) ? length : space;
    size_t head_index = rb->head % rb->max;

    // 处理分段写入
    if (to_enqueue + head_index > rb->max) {
        size_t first_part = rb->max - head_index;
        memcpy(&rb->buffer[head_index], data, first_part);
        memcpy(rb->buffer, &data[first_part], to_enqueue - first_part);
    } else {
        memcpy(&rb->buffer[head_index], data, to_enqueue);
    }

    rb->head = (rb->head + to_enqueue) % rb->max; // 更新头指针
    pthread_mutex_unlock(&rb->lock); // 解锁
    return to_enqueue; // 返回实际入队的数量
}




// 高效单字节入队
bool enqueue_byte(RingBuffer *rb, uint8_t byte) {
    if(rb == NULL){
        return 0; // 缓冲区满
    }
    pthread_mutex_lock(&rb->lock); // 加锁

    if (available_space(rb) == 0) {
        pthread_mutex_unlock(&rb->lock); // 解锁
        return false; // 缓冲区满
    }

    rb->buffer[rb->head] = byte; // 直接写入
    rb->head = (rb->head + 1) % rb->max; // 更新头指针

    pthread_mutex_unlock(&rb->lock); // 解锁
    return true; // 入队成功
}

// 出队
size_t dequeue(RingBuffer *rb, uint8_t *data, size_t length) {
    if(rb == NULL){
        return 0; // 缓冲区满
    }
    pthread_mutex_lock(&rb->lock); // 加锁

    size_t data_count = available_data(rb);
    if (data_count == 0) {
        pthread_mutex_unlock(&rb->lock); // 解锁
        return 0; // 缓冲区空
    }

    size_t to_dequeue = (length < data_count) ? length : data_count;
    size_t tail_index = rb->tail % rb->max;

    // 处理分段读取
    if (to_dequeue + tail_index > rb->max) {
        size_t first_part = rb->max - tail_index;
        memcpy(data, &rb->buffer[tail_index], first_part);
        memcpy(&data[first_part], rb->buffer, to_dequeue - first_part);
    } else {
        memcpy(data, &rb->buffer[tail_index], to_dequeue);
    }

    rb->tail = (rb->tail + to_dequeue) % rb->max; // 更新尾指针
    pthread_mutex_unlock(&rb->lock); // 解锁
    return to_dequeue; // 返回实际出队的数量
}

// 高效单字节出队
bool dequeue_byte(RingBuffer *rb, uint8_t *byte) {
    if(rb == NULL){
        return false; // 缓冲区满
    }
    pthread_mutex_lock(&rb->lock); // 加锁

    if (available_data(rb) == 0) {
        pthread_mutex_unlock(&rb->lock); // 解锁
        return false; // 缓冲区空
    }

    *byte = rb->buffer[rb->tail]; // 直接读取
    rb->tail = (rb->tail + 1) % rb->max; // 更新尾指针

    pthread_mutex_unlock(&rb->lock); // 解锁
    return true; // 出队成功
}

// 高效单字节出队 ,调用此接口，必须在外部使用自行加锁解锁，切记切记
bool dequeue_byte_with_no_mutex(RingBuffer *rb, uint8_t *byte) {
    if(rb == NULL){
        return false; // 缓冲区满
    }
//    pthread_mutex_lock(&rb->lock); // 加锁

    if (available_data(rb) == 0) {
//        pthread_mutex_unlock(&rb->lock); // 解锁
        return false; // 缓冲区空
    }

    *byte = rb->buffer[rb->tail]; // 直接读取
    rb->tail = (rb->tail + 1) % rb->max; // 更新尾指针

//    pthread_mutex_unlock(&rb->lock); // 解锁
    return true; // 出队成功
}



// 销毁环形缓冲区
void destroy_ring_buffer(RingBuffer *rb) {
    if(rb == NULL){
        return ; // 缓冲区满
    }
    pthread_mutex_destroy(&rb->lock); // 销毁互斥锁
    free(rb->buffer);
    free(rb);
}
//
//// 示例使用
//int main() {
//    size_t size = 10; // 缓冲区大小
//    RingBuffer *rb = create_ring_buffer(size);
//
//    // 入队示例
//    uint8_t data_to_enqueue[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
//    size_t enqueued = enqueue(rb, data_to_enqueue, sizeof(data_to_enqueue));
//    printf("Enqueued %zu items\n", enqueued);
//
//    // 使用高效单字节入队
//    enqueue_byte(rb, 12);
//    printf("Enqueued 1 item: 12\n");
//
//    // 出队示例
//    uint8_t data_dequeue[5];
//    size_t dequeued = dequeue(rb, data_dequeue, sizeof(data_dequeue));
//    printf("Dequeued %zu items: ", dequeued);
//    for (size_t i = 0; i < dequeued; i++) {
//        printf("%d ", data_dequeue[i]);
//    }
//    printf("\n");
//
//    // 使用高效单字节出队
//    uint8_t single_byte;
//    if (dequeue_byte(rb, &single_byte)) {
//        printf("Dequeued 1 item: %d\n", single_byte);
//    } else {
//        printf("Failed to dequeue a single item, buffer might be empty.\n");
//    }
//
//    destroy_ring_buffer(rb);
//    return EXIT_SUCCESS;
//}