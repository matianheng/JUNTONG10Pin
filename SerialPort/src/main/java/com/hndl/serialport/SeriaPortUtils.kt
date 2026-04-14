package com.hndl.serialport
import javax.inject.Singleton
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class SerialPortUtils {

    external fun startUartTaskJNI(port:String,baud:Int): Int

    external fun sendUartFunJNI(msg: ByteArray): Int

    external fun stopUartTaskJNI(run: Boolean): Unit

    external fun getWorkFlag(): Boolean

    /**
     * workMode 0 is TRANSPARENT_MODE , 1 is ANALYTICAL_MODE
     */
    external fun setWorkModeFunJNI(workMode: Int): Unit

    /**
     * heartBeat 0..15
     */
    external fun sendHeartBeat(heartBeat:Int): Int

    external  fun enterLowPower(): Int

    /**
     * canIndex 0..1
     * baud 125,250,500,1000   kbps
     */
    external fun openCanPort(canIndex:Int,baud:Int): Int

    /**
     * canIndex 0..1
     */
    external fun closeCanPort(canIndex:Int): Int

    /**
     * canIndex 0..1
     * bank 0..27  , 0..14 is for can0 , 15..27 is for can1
     * id0/id1/id2/id3 16bits canId for list mode
     */
    external fun setCanFilterListWith16bit(canIndex: Int,bank: Int,id0: Int, id1: Int, id2:Int, id3:Int): Int

    /**
     * canIndex 0..1
     * bank 0..27  , 0..14 is for can0 , 15..27 is for can1
     * id0/id1 32bits canId for list mode
     */
    external  fun setCanFilterListWith32bit(canIndex: Int,bank: Int,id0: Int, id1: Int): Int

    /**
     * canIndex 0..1
     * bank 0..27  , 0..14 is for can0 , 15..27 is for can1
     * id0/id1 16bits canId for mask mode
     * id0Mask/id1Mask 16bits mask for mask mode
     */
    external  fun setCanFilterMaskWith16bit(canIndex: Int,bank: Int,id0: Int, id0Mask: Int, id1:Int, id1Mask:Int): Int

    /**
     * canIndex 0..1
     * bank 0..27  , 0..14 is for can0 , 15..27 is for can1
     * id0 32bits canId for mask mode
     * id0Mask 32bits mask for mask mode,please take careful,32bit default is ext frame,if need std frame mask ,please use setCanFilterMaskWith16bit function
     */
    external  fun setCanFilterMaskWith32bit(canIndex: Int,bank: Int,id0: Int, id0Mask: Int): Int

    /**
     * canIndex 0..1
     * canId 11bit std frame ID or 29bit std frame id
     * frameType 0 is std ,1 is ext
     * dataLen 1..8
     * data can's data
     */
    external  fun sendCanData(canIndex:Int,canId:Int,frameType:Int,dataLen:Int,data:ByteArray):Int

    /**
     * usbMode 0 is otg mode , 1 is host mode, default for power on is host mode.
     */
    external  fun changeUsbMode(usbMode:Int): Int

    /**
     * canIndex 0..1
     * hasRs 0 is no Rs(120ohm) ,1 is has Rs(120ohm)
      */
    external  fun configCanRs(canIndex: Int,hasRs: Int):Int

    external  fun sendGetUsbModeMsg():Int

    /**
     * canIndex 0..1
     */
    external  fun sendGetCanRsMsg(canIndex: Int):Int

    /**
     * gpioValue 0..4
     */
    external  fun sendGetGpioValueMsg(gpioValue: Int): Int


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
    external fun openTimerTask(timerId: Int,taskType:Int,currentYear:Int,currentMon:Int,currentDay:Int,
                               currentHour:Int,currentMin:Int,currentSec:Int,aimYear:Int,aimMon:Int,aimDay:Int,
                               aimHour:Int,aimMin:Int,aimSec:Int):Int


    /**
     * timerId 0..9
     */
    external fun cancelTimerTask(timerId:Int):Int

    external fun getCoprocessorVersion():Int


    fun recvMsgCallback(msg: ByteArray) {
//        Log.e("MainActivity", "Can data: ${msg.mapIndexed { index, byte ->
//            "[$index]:${"%02X".format(byte)}(${byte.toInt() and 0xFF})"
//        }.joinToString(" ")}")
        callback?.invoke(msg)
    }

    /**
     * heartBeat 0..9
     */
    fun heartBeatCallback(heartBeat: Int){
    }

    fun requestSleepCallback(){

    }

    /**
     * canIndex 0..1
     * canBaud  [125,250,500,1000]
     * resultValue 0 is ok, 1 is err.
     */
    fun canOpenRespCallback(canIndex: Int,canBaud: Int ,resultValue: Int){

    }

    /**
     * canIndex 0..1
     * resultValue 0 is ok, 1 is err.
     */
    fun canCloseRespCallback(canIndex: Int ,resultValue: Int){

    }
    /**
     * canIndex 0..1
     * dataLen 1..8
     * canID  11bits or 29bits
     * canFrameType 0 is stdframe,1 is extframe
     * msg can's data[]
     * resultValue 0 is ok, 1 is err.
     */
    fun canSendRespCallback(canIndex:Int, dataLen:Int,canID:Int,canFrameType:Int,resultValue:Int,msg: ByteArray){

    }

    /**
     * canIndex 0..1
     * filterBank 0..27,can1 use 0..13,can2 use 14..27
     * filterMode 0 is idmask mode , 1 is idlist mode.
     * filterBits 16bits or 32bits.
     * resultValue 0 is ok, 1 is err.
     */
    fun canFilterRespCallback(canIndex: Int,filterBank: Int,filterMode: Int,filterBits: Int,resultValue:Int){

    }
    /**
     * canIndex 0..1
     * dataLen 1..8
     * canID  11bits or 29bits
     * canFrameType 0 is stdframe,1 is extframe
     * msg can's data[]
     */
    fun canRecvDataCallback(canIndex:Int, dataLen:Int,canID:Int,canFrameType:Int,msg: ByteArray){

    }

    /**
     * usbMode 0 is otg mode , 1 is host mode.
     */
    fun usbModeRespCallback(usbMode:Int){

    }

    /**
     * canIndex 0..1
     * hasRs 0 is no Rs ,1 is Has Rs
     */
    fun canRsRespCallback(canIndex:Int,hasRs:Int){

    }

    /**
     * canIndex 0..1
     * resetState 0 is can has error,but reset ok ,1 is can has error and reset fault.
     */
    fun canResetStateCallback(canIndex:Int,resetState:Int){

    }

    /**
     * gpioIndex 0..9
     * gpioValue 0 is low voltage , 1 is high voltage
     */
    fun gpioValueRespCallback(gpioIndex:Int,gpioValue:Int){

    }

    /**
     * timerId: 0..9
     * resultValue 0 is ok, 1 is error
     */
    fun setTimerRespCallback(timerId:Int,resultValue:Int){

    }

    /**
     * timerId: 0..9
     * resultValue 0 is ok, 1 is error
     */
    fun cancelTimerRespCallback(timerId: Int,resultValue: Int){

    }

    /**
     * version ver format is "01.11"
     */
    fun getMcuAppVersionRespCallback(version:ByteArray){

    }

    companion object {
        init {
            System.loadLibrary("can")
        }
    }
    private var callback:((ByteArray)->Unit)?=null
    fun recvMsg(callback:(ByteArray)->Unit) {
        this.callback = callback
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun openSerialPort(port:String, baud:Int){
        GlobalScope.launch(Dispatchers.IO){
            while(isActive){
                isOpen = true
                var result = startUartTaskJNI(port,baud)
                isOpen = false
            }
//            mutex.withLock{
//                if (isOpen){
//                    stopUartTaskJNI(false)
//                    while (isOpen){}
//                }
//                if (!isOpen){
//                    isOpen = true
//
//                    var result = startUartTaskJNI(port,baud)
//                    isOpen = false
//                }
//            }
        }
    }

    val mutex = Mutex()
    var isOpen = true

}