//
// Created by 30890 on 2024-11-12.
//

#include "msg_protocol.h"


#define CAN_ID_STD                  (0x00000000U)  /*!< Standard Id */
#define CAN_ID_EXT                  (0x00000004U)  /*!< Extended Id */

/** @defgroup CAN_remote_transmission_request CAN Remote Transmission Request
  * @{
  */
#define CAN_RTR_DATA                (0x00000000U)  /*!< Data frame   */
#define CAN_RTR_REMOTE              (0x00000002U)  /*!< Remote frame */


uint8_t itoa_map[16]={
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        'A',
        'B',
        'C',
        'D',
        'E',
        'F'
};


int get_num_from_asc(uint8_t input){
    if(input>=0x30 && input <= 0x39){
        return (input-0x30);
    } else if( input>=0x41 && input <= 0x46){
        return (input-0x41+10);
    } else if( input>= 0x61 && input <= 0x66){
        return (input-0x61+10);
    } else {
        return -1;
    }
}


// num max =8
unsigned int get_num_from_asc_array(uint8_t * p,uint32_t num){
    uint32_t i = 0;
    uint32_t ret = 0;
    for(i = 0 ; i!= num ;i++){
//        ret = (ret << 4);
        ret |= (get_num_from_asc(p[i])<<(i*4));
    }
    return ret;
}


static int translate_ascii_array_to_bytes(uint8_t * pinput,uint32_t len,uint8_t *pout){
    int count = (len+1)/2;
    int flag = len%2;
    for(int i = 0 ; i!= count ; i++){
        if(flag == 0){
            pout[i] = get_num_from_asc_array(&pinput[2*i],2);
        } else {
            if(i!=count-1){
                pout[i] = get_num_from_asc_array(&pinput[2*i],2);
            } else {
                pout[i] = pinput[2*i];
            }
        }

    }
    return count;
}

static void calc_msg_crc(uint8_t * pbuf,uint8_t len){
    uint8_t crc = 0;
    uint8_t i = 0;
    for(i = 1;i != len-1; i++){
        crc += pbuf[i];
    }
    pbuf[len] = itoa_map[crc&0x0f];
    pbuf[len+1] = itoa_map[(crc>>4)&0x0f];
}




/***********************************************CONSTRUCT MSG***************************************************************/

int protocol_heart_beat(uint8_t * buf, char heart_beat, uint32_t max_len){
    int i = 0;
    if(max_len < 8){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = '0';
    buf[i++] = 0x31;
    buf[i++] = 0x30;
    buf[i++] = itoa_map[heart_beat&0x0f];
    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}

int response_low_power(uint8_t * buf, uint32_t max_len){
    int i = 0;
    if(max_len< 7){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = 0x32;
    buf[i++] = 0x30;
    buf[i++] = 0x30;
    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}

// baud  125=125kbps 250=250kbps ...依次类推
int open_can_port(uint8_t * buf, int32_t can_index,int32_t baud,uint32_t max_len){
    int i = 0;
    // 7 protocal info+2 protocal data
    if(max_len<13){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = '3';
    buf[i++] = itoa_map[(6&0x0f)];
    buf[i++] = itoa_map[((6>>4)&0x0f)];
    // can id
    if(can_index >=0 && can_index <2 ){
        buf[i++] = '0'+can_index;
    } else {
        return -1;
    }
    buf[i++] = '0'; // open
    if(baud>0 && baud<=1000){
        buf[i++] = itoa_map[(baud&0x0f)];
        buf[i++] = itoa_map[((baud>>4)&0x0f)];
        buf[i++] = itoa_map[((baud>>8)&0x0f)];
        buf[i++] = itoa_map[((baud>>12)&0x0f)];
    } else {
        return -1;
    }

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;

}

int can_close_port(uint8_t * buf,int32_t can_index,uint32_t max_len){
    int i = 0;
    int data_len = 2;
    // 7 protocal info+2 protocal data
    if(max_len<7+data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = '3';
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];
    // can id
    if(can_index >=0 && can_index <2 ){
        buf[i++] = '0'+can_index;
    } else {
        return -1;
    }
    buf[i++] = '1'; // close
    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}

int can_filter_list_16bit(uint8_t *buf,int32_t can_index,int32_t bank,uint16_t id0,uint16_t id1,uint16_t id2,uint16_t id3,uint32_t max_len){
    int i = 0;
    int data_len = 21;
    // 7 protocal info+ protocal data
    if(max_len<7+data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = '4';
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];
    // can id
    if(can_index >=0 && can_index <2 ){
        buf[i++] = '0'+can_index;
    } else {
        return -1;
    }
    if(bank>=0 && bank<=27){
        buf[i++] = itoa_map[(bank&0x0f)];
        buf[i++] = itoa_map[((bank>>4)&0x0f)];
    } else {
        return -1;
    }
    buf[i++] = '1'; //list
    buf[i++] = '0'; //16bit
    //id0
    uint16_t id0_real = ((id0<<5)|CAN_ID_STD|CAN_RTR_DATA);
    buf[i++] = itoa_map[(id0_real&0x0f)];
    buf[i++] = itoa_map[((id0_real>>4)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>8)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>12)&0x0f)];
    //id1
    uint16_t id1_real = ((id1<<5)|CAN_ID_STD|CAN_RTR_DATA);
    buf[i++] = itoa_map[(id1_real&0x0f)];
    buf[i++] = itoa_map[((id1_real>>4)&0x0f)];
    buf[i++] = itoa_map[((id1_real>>8)&0x0f)];
    buf[i++] = itoa_map[((id1_real>>12)&0x0f)];
    //id2
    uint16_t id2_real = ((id2<<5)|CAN_ID_STD|CAN_RTR_DATA);
    buf[i++] = itoa_map[(id2_real&0x0f)];
    buf[i++] = itoa_map[((id2_real>>4)&0x0f)];
    buf[i++] = itoa_map[((id2_real>>8)&0x0f)];
    buf[i++] = itoa_map[((id2_real>>12)&0x0f)];
    //id3
    uint16_t id3_real = ((id3<<5)|CAN_ID_STD|CAN_RTR_DATA);
    buf[i++] = itoa_map[(id3_real&0x0f)];
    buf[i++] = itoa_map[((id3_real>>4)&0x0f)];
    buf[i++] = itoa_map[((id3_real>>8)&0x0f)];
    buf[i++] = itoa_map[((id3_real>>12)&0x0f)];

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}


int can_filter_list_32bit(uint8_t *buf,int32_t can_index,int32_t bank,uint32_t id0,uint32_t id1,uint32_t max_len){
    int i = 0;
    int data_len = 21;
    // 7 protocal info+ protocal data
    if(max_len<7+data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = '4';
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];
    // can id
    if(can_index >=0 && can_index <2 ){
        buf[i++] = '0'+can_index;
    } else {
        return -1;
    }
    if(bank>=0 && bank<=27){
        buf[i++] = itoa_map[(bank&0x0f)];
        buf[i++] = itoa_map[((bank>>4)&0x0f)];
    } else {
        return -1;
    }
    buf[i++] = '1'; //list
    buf[i++] = '1'; //32bit
    //id0
    uint32_t id0_real = ((id0<<3)|CAN_ID_EXT|CAN_RTR_DATA);
    buf[i++] = itoa_map[(id0_real&0x0f)];
    buf[i++] = itoa_map[((id0_real>>4)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>8)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>12)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>16)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>20)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>24)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>28)&0x0f)];

    //id1
    uint32_t id1_real = ((id1<<3)|CAN_ID_EXT|CAN_RTR_DATA);
    buf[i++] = itoa_map[(id1_real&0x0f)];
    buf[i++] = itoa_map[((id1_real>>4)&0x0f)];
    buf[i++] = itoa_map[((id1_real>>8)&0x0f)];
    buf[i++] = itoa_map[((id1_real>>12)&0x0f)];
    buf[i++] = itoa_map[((id1_real>>16)&0x0f)];
    buf[i++] = itoa_map[((id1_real>>20)&0x0f)];
    buf[i++] = itoa_map[((id1_real>>24)&0x0f)];
    buf[i++] = itoa_map[((id1_real>>28)&0x0f)];

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}


int can_filter_mask_16bit(uint8_t *buf,int32_t can_index,int32_t bank,uint16_t id0,uint16_t id0_mask,uint16_t id1,uint16_t id1_mask,uint32_t max_len){
    int i = 0;
    int data_len = 21;
    // 7 protocal info+ protocal data
    if(max_len<7+data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = '4';
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];
    // can id
    if(can_index >=0 && can_index <2 ){
        buf[i++] = '0'+can_index;
    } else {
        return -1;
    }
    if(bank>=0 && bank<=27){
        buf[i++] = itoa_map[(bank&0x0f)];
        buf[i++] = itoa_map[((bank>>4)&0x0f)];
    } else {
        return -1;
    }
    buf[i++] = '0'; //mask
    buf[i++] = '0'; //16bit
    //id0
    uint16_t id0_real = ((id0<<5)|CAN_ID_STD|CAN_RTR_DATA);
    buf[i++] = itoa_map[(id0_real&0x0f)];
    buf[i++] = itoa_map[((id0_real>>4)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>8)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>12)&0x0f)];
    //id0_mask
    uint16_t id0_real_mask = ((id0_mask<<5)|(1<<3)); //IDE need for 0
    buf[i++] = itoa_map[(id0_real_mask&0x0f)];
    buf[i++] = itoa_map[((id0_real_mask>>4)&0x0f)];
    buf[i++] = itoa_map[((id0_real_mask>>8)&0x0f)];
    buf[i++] = itoa_map[((id0_real_mask>>12)&0x0f)];
    //id1
    uint16_t id1_real = ((id1<<5)|CAN_ID_STD|CAN_RTR_DATA);
    buf[i++] = itoa_map[(id1_real&0x0f)];
    buf[i++] = itoa_map[((id1_real>>4)&0x0f)];
    buf[i++] = itoa_map[((id1_real>>8)&0x0f)];
    buf[i++] = itoa_map[((id1_real>>12)&0x0f)];
    //id3
    uint16_t id1_real_mask = ((id1_mask<<5)|(1<<3)); // IDE need for 0
    buf[i++] = itoa_map[(id1_real_mask&0x0f)];
    buf[i++] = itoa_map[((id1_real_mask>>4)&0x0f)];
    buf[i++] = itoa_map[((id1_real_mask>>8)&0x0f)];
    buf[i++] = itoa_map[((id1_real_mask>>12)&0x0f)];

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}




int can_filter_mask_32bit(uint8_t *buf,int32_t can_index,int32_t bank,uint32_t id0,uint32_t id0_mask,uint32_t max_len){
    int i = 0;
    int data_len = 21;
    // 7 protocal info+ protocal data
    if(max_len<7+data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = '4';
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];
    // can id
    if(can_index >=0 && can_index <2 ){
        buf[i++] = '0'+can_index;
    } else {
        return -1;
    }
    if(bank>=0 && bank<=27){
        buf[i++] = itoa_map[(bank&0x0f)];
        buf[i++] = itoa_map[((bank>>4)&0x0f)];
    } else {
        return -1;
    }
    buf[i++] = '0'; //mask
    buf[i++] = '1'; //32bit
    //id0
    uint32_t id0_real = ((id0<<3)|CAN_ID_EXT|CAN_RTR_DATA);
    buf[i++] = itoa_map[(id0_real&0x0f)];
    buf[i++] = itoa_map[((id0_real>>4)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>8)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>12)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>16)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>20)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>24)&0x0f)];
    buf[i++] = itoa_map[((id0_real>>28)&0x0f)];

    //id1
    uint32_t id0_real_mask = ((id0_mask<<3)|CAN_ID_EXT|CAN_RTR_DATA);
    buf[i++] = itoa_map[(id0_real_mask&0x0f)];
    buf[i++] = itoa_map[((id0_real_mask>>4)&0x0f)];
    buf[i++] = itoa_map[((id0_real_mask>>8)&0x0f)];
    buf[i++] = itoa_map[((id0_real_mask>>12)&0x0f)];
    buf[i++] = itoa_map[((id0_real_mask>>16)&0x0f)];
    buf[i++] = itoa_map[((id0_real_mask>>20)&0x0f)];
    buf[i++] = itoa_map[((id0_real_mask>>24)&0x0f)];
    buf[i++] = itoa_map[((id0_real_mask>>28)&0x0f)];

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}


int can_send_data(uint8_t *buf,int32_t can_index,int32_t can_id,uint32_t id_type,const uint8_t *data,uint8_t len,uint32_t max_len){
    int i = 0;
    int data_len = 26;
    // 7 protocal info+ protocal data
    if(max_len<7+data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = '5';
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];
    // can id
    if(can_index >=0 && can_index <2 ){
        buf[i++] = '0'+can_index;
    } else {
        return -1;
    }

    buf[i++] = itoa_map[len&0x0f]; //mask
    uint32_t can_id_temp = can_id;
    if(id_type == CAN_ID_EXT_USER){
        can_id_temp = (can_id_temp|0x80000000);
    }

    buf[i++] = itoa_map[(can_id_temp&0x0f)];
    buf[i++] = itoa_map[((can_id_temp>>4)&0x0f)];
    buf[i++] = itoa_map[((can_id_temp>>8)&0x0f)];
    buf[i++] = itoa_map[((can_id_temp>>12)&0x0f)];
    buf[i++] = itoa_map[((can_id_temp>>16)&0x0f)];
    buf[i++] = itoa_map[((can_id_temp>>20)&0x0f)];
    buf[i++] = itoa_map[((can_id_temp>>24)&0x0f)];
    buf[i++] = itoa_map[((can_id_temp>>28)&0x0f)];

    for(int j = 0; j!= len; j++){
        buf[i++] = itoa_map[(data[j]&0x0f)];
        buf[i++] = itoa_map[((data[j]>>4)&0x0f)];
    }
    for(int j = 0; j!= 8-len;j++){
        buf[i++]='F';
        buf[i++]='F';
    }

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}


int change_usb_type(uint8_t * buf, uint32_t usb_type, uint32_t max_len){
    int i = 0;
    int data_len = 1;
    if(max_len < 7 + data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = '7';
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];
    if(usb_type == USB_TYPE_HOST){
        buf[i++] = '1';
    } else {
        buf[i++] = '0';
    }

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}


int can_config_rs(uint8_t * buf, uint32_t can_index,uint32_t has_rs, uint32_t max_len){
    int i = 0;
    int data_len = 2;
    if(max_len < 7 + data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = '8';
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];

    // can id
    if(can_index >=0 && can_index <2 ){
        buf[i++] = '0'+can_index;
    } else {
        return -1;
    }

    if(has_rs){
        buf[i++] = '1';
    } else {
        buf[i++] = '0';
    }

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}


int get_usb_type(uint8_t * buf, uint32_t max_len){
    int i = 0;
    int data_len = 0;
    if(max_len < 7 + data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = '9';
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}


int get_can_rs(uint8_t * buf,uint32_t can_index, uint32_t max_len){
    int i = 0;
    int data_len = 1;
    if(max_len < 7 + data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = 0x3D;
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];
    // can id
    if(can_index >=0 && can_index <2 ){
        buf[i++] = '0'+can_index;
    } else {
        return -1;
    }

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}

int get_gpio_value(uint8_t * buf,uint32_t gpio_index, uint32_t max_len){
    int i = 0;
    int data_len = 1;
    if(max_len < 7 + data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = 0x3B;
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];
    // can id
    if(gpio_index >=0 && gpio_index <5 ){
        buf[i++] = '0'+gpio_index;
    } else {
        return -1;
    }

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}

int set_timer_task(uint8_t * buf,uint32_t timer_index, uint32_t task_type,timer_s * pnow,timer_s * paim ,uint32_t max_len){
    int i = 0;
    int data_len = 26;
    if(max_len < 7 + data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = 0x3E;
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];
    // timer id
    if(timer_index >=0 && timer_index <10 ){
        buf[i++] = itoa_map[(timer_index&0x0f)];
    } else {
        return -1;
    }

    buf[i++] = itoa_map[(task_type&0x0f)];

    buf[i++] = itoa_map[pnow->year&0x0f];
    buf[i++] = itoa_map[(pnow->year>>4)&0x0f];
    buf[i++] = itoa_map[pnow->month&0x0f];
    buf[i++] = itoa_map[(pnow->month>>4)&0x0f];
    buf[i++] = itoa_map[pnow->day&0x0f];
    buf[i++] = itoa_map[(pnow->day>>4)&0x0f];
    buf[i++] = itoa_map[pnow->hour&0x0f];
    buf[i++] = itoa_map[(pnow->hour>>4)&0x0f];
    buf[i++] = itoa_map[pnow->minute&0x0f];
    buf[i++] = itoa_map[(pnow->minute>>4)&0x0f];
    buf[i++] = itoa_map[pnow->second&0x0f];
    buf[i++] = itoa_map[(pnow->second>>4)&0x0f];

    buf[i++] = itoa_map[paim->year&0x0f];
    buf[i++] = itoa_map[(paim->year>>4)&0x0f];
    buf[i++] = itoa_map[paim->month&0x0f];
    buf[i++] = itoa_map[(paim->month>>4)&0x0f];
    buf[i++] = itoa_map[paim->day&0x0f];
    buf[i++] = itoa_map[(paim->day>>4)&0x0f];
    buf[i++] = itoa_map[paim->hour&0x0f];
    buf[i++] = itoa_map[(paim->hour>>4)&0x0f];
    buf[i++] = itoa_map[paim->minute&0x0f];
    buf[i++] = itoa_map[(paim->minute>>4)&0x0f];
    buf[i++] = itoa_map[paim->second&0x0f];
    buf[i++] = itoa_map[(paim->second>>4)&0x0f];

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}


int cancel_timer_task(uint8_t * buf,uint32_t timer_index, uint32_t max_len){
    int i = 0;
    int data_len = 1;
    if(max_len < 7 + data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = 0x40;
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];
    // can id
    if(timer_index >=0 && timer_index <10 ){
        buf[i++] = itoa_map[(timer_index&0x0f)];
    } else {
        return -1;
    }

    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}



int get_coprocessor_version(uint8_t * buf, uint32_t max_len){
    int i = 0;
    int data_len = 0;
    if(max_len < 7 + data_len){
        return -1;
    }
    buf[i++] = 0x02;
    buf[i++] = 0x41;
    buf[i++] = itoa_map[(data_len&0x0f)];
    buf[i++] = itoa_map[((data_len>>4)&0x0f)];


    buf[i++] = 0x03;
    calc_msg_crc(buf,i);
    i = i+2;
    return i;
}

