package com.hndl.ui.utils;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AppUtil {
    //获取到所有的包名并检查是否有指定包名
    @SuppressLint("QueryPermissionsNeeded")
    public  static  boolean isAppInstalled(Context context, String packageName){
        //应用信息列表
        List<ResolveInfo> apps = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //获取到设备上安装的所有应用信息
        apps = context.getPackageManager().queryIntentActivities(intent, 0);
        for (int i = 0; i < apps.size(); i++) {
            ResolveInfo info = apps.get(i);
            //包名
            String pname = info.activityInfo.packageName;
            //主activity信息
            CharSequence cls = info.activityInfo.name;
            //app名字
            CharSequence appname = info.activityInfo.loadLabel(context.getPackageManager());
            Log.e("Unity","应用信息: appname = "+appname+" packageName = "+pname+" Activity = "+cls);
            if(pname.equals(packageName)){
                return true;
            }
        }
        return false;
    }
    //通过包名跳转到该app
    public static void JumpToActivity(Context context, String packageName, String className) {
        ComponentName componetName = new ComponentName(packageName,className);
        Intent intent= new Intent();
        intent.setComponent(componetName);
        //创建一个任务栈
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
