package com.hndl.ui.data;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.TimeUtils;
import com.hndl.ui.model.OverloadRecordModel;

import java.text.SimpleDateFormat;

/**
 * 持久化数据
 */
public class PersistentData {

    /**
     * 超载信息
     */

    public static String start_date= TimeUtils.getNowString(new SimpleDateFormat("yyyy-MM-dd HH:mm"))+"";  //开始时间

    public static int work_arm=0;  //

    public static int magnification=0;  //幅度

    public static int leg=0;  //

    public static int fifth_leg=0;  //

    public static int work_area=0;  //

    public static String l1="";  //

    public static String a="";  //

    public static String p1="";  //

    public static String p2="";  //

    public static String amplitude="";  //

    public static String frontal_weight="";  //

    public static String actual_weight="";  //

    public static String moment_percentage="";  //

    public static String end_date="2023-12-28 12:28";  //

    public static OverloadRecordModel overloadRecordModel=null;
}
