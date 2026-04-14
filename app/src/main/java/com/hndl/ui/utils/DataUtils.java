package com.hndl.ui.utils;

import com.blankj.utilcode.util.TimeUtils;
import com.hndl.ui.AppData;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class DataUtils {

    public static void addList(String string){
        for (String str : AppData.mAlarmingList) {
            if (string.equals(str)){
                return;
            }
        }
        AppData.mAlarmingList.add(string);
    }

    public static void removeList(String string){
        AppData.mAlarmingList.remove(string);
    }
}
