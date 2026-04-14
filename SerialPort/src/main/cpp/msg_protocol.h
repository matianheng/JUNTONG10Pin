//
// Created by 30890 on 2024-11-12.
//

#ifndef FORKLIFT_BYD_8_MSG_PROTOCOL_H
#define FORKLIFT_BYD_8_MSG_PROTOCOL_H

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <stdint.h>
#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

#define CAN_ID_EXT_USER 1
#define CAN_ID_STD_USER 0

#define USB_TYPE_OTG 0
#define USB_TYPE_HOST 1


typedef struct {
    uint32_t year;
    uint32_t month;
    uint32_t day;
    uint32_t hour;
    uint32_t minute;
    uint32_t second;
} timer_s;

int protocol_heart_beat(uint8_t * buf, char heart_beat, uint32_t max_len);
int get_num_from_asc(uint8_t input);
unsigned int get_num_from_asc_array(uint8_t * p,uint32_t num);

int protocol_heart_beat(uint8_t * buf, char heart_beat, uint32_t max_len);
int response_low_power(uint8_t * buf, uint32_t max_len);
int open_can_port(uint8_t * buf, int32_t can_index,int32_t baud,uint32_t max_len);
int can_close_port(uint8_t * buf,int32_t can_index,uint32_t max_len);
int can_filter_list_16bit(uint8_t *buf,int32_t can_index,int32_t bank,uint16_t id0,uint16_t id1,uint16_t id2,uint16_t id3,uint32_t max_len);
int can_filter_list_32bit(uint8_t *buf,int32_t can_index,int32_t bank,uint32_t id0,uint32_t id1,uint32_t max_len);
int can_filter_mask_16bit(uint8_t *buf,int32_t can_index,int32_t bank,uint16_t id0,uint16_t id0_mask,uint16_t id1,uint16_t id1_mask,uint32_t max_len);
int can_filter_mask_32bit(uint8_t *buf,int32_t can_index,int32_t bank,uint32_t id0,uint32_t id0_mask,uint32_t max_len);
int can_send_data(uint8_t *buf,int32_t can_index,int32_t can_id,uint32_t id_type,const uint8_t *data,uint8_t len,uint32_t max_len);
int change_usb_type(uint8_t * buf, uint32_t usb_type, uint32_t max_len);
int can_config_rs(uint8_t * buf, uint32_t can_index,uint32_t has_rs, uint32_t max_len);
int get_usb_type(uint8_t * buf, uint32_t max_len);
int get_can_rs(uint8_t * buf,uint32_t can_index, uint32_t max_len);
int get_gpio_value(uint8_t * buf,uint32_t gpio_index, uint32_t max_len);
int set_timer_task(uint8_t * buf,uint32_t timer_index, uint32_t task_type,timer_s * pnow,timer_s * paim ,uint32_t max_len);
int cancel_timer_task(uint8_t * buf,uint32_t timer_index, uint32_t max_len);
int get_coprocessor_version(uint8_t * buf, uint32_t max_len);


#ifdef __cplusplus
}
#endif
#endif //FORKLIFT_BYD_8_MSG_PROTOCOL_H
