package com.hndl.ui.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.LocaleList;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MyUtils {

    /**
     * 获取屏幕宽；返回int
     */
    public static final int getDisplayPxHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 获取屏幕宽；返回int
     */
    public static final int getDisplayPxWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @param （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @param （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int canIndex=1;
    public static byte[] constructSerialData(byte cmd, int canId, @NonNull byte[] canData) {
        byte[] canPkgData = constructCanData(canIndex, canData.length, canId, canData);

        int length = canPkgData.length + 7;
        byte[] serialData = new byte[length];
        serialData[0] = 0x02;
        serialData[1] = cmd;

        serialData[2] = intToHexChar(canPkgData.length & 0x0f);
        serialData[3] = intToHexChar((canPkgData.length >> 4) & 0x0f);

        // fill can dataFE
        System.arraycopy(canPkgData, 0, serialData, 4, canPkgData.length);

        serialData[length - 3] = 0x03;

        // calc crc
        int crcValue = calcCRC(serialData);

        serialData[length - 2] = intToHexChar(crcValue & 0x0f);
        serialData[length - 1] = intToHexChar((crcValue >> 4) & 0x0f);

        return serialData;
    }

    public static byte[] constructSerialFilter(byte cmd,@NonNull byte[] canData) {
        int length = canData.length + 7;
        byte[] serialData = new byte[length];
        serialData[0] = 0x02;
        serialData[1] = cmd;

        serialData[2] = intToHexChar(canData.length & 0x0f);
        serialData[3] = intToHexChar((canData.length >> 4) & 0x0f);

        // fill can dataFE
        System.arraycopy(canData, 0, serialData, 4, canData.length);

        serialData[length - 3] = 0x03;

        // calc crc
        int crcValue = calcCRC(serialData);

        serialData[length - 2] = intToHexChar(crcValue & 0x0f);
        serialData[length - 1] = intToHexChar((crcValue >> 4) & 0x0f);

        return serialData;
    }

    /**
     * CRC校验
     *
     * @param data
     * @return
     */
    public static int calcCRC(byte[] data) {
        int crcValue = 0;
        for (int i = 1; i < data.length - 3; i++) {
            crcValue = crcValue + (int) data[i];
        }

        return crcValue;
    }

    public static byte[] constructCanData(int canIndex, int canLength, int canId, byte[] canData) {
        int len = 1 + 1 + 8 + canData.length * 2;
        byte[] canPkgData = new byte[len];

        canPkgData[0] = intToHexChar(canIndex);
        canPkgData[1] = intToHexChar(canLength);

        // 将CanID 转换为 8个字节的数组
//        canPkgData[2] = (byte)(canId & 0x0f);
//        canPkgData[3] = (byte)(canId>>4 & 0x0f);
        for (int i = 0; i < 8; i++) {
            int value = canId >> (i * 4);
            canPkgData[i + 2] = intToHexChar(value & 0x0f);
        }

        // 将 CanData数据段，每个数据分为两个字节存储
        for (int i = 0; i < canLength; i++) {
            int value = canData[i];
            int startIndex = 10 + i * 2;
            canPkgData[startIndex] = intToHexChar(value & 0x0f);
            canPkgData[startIndex + 1] = intToHexChar(value >> 4 & 0x0f);
        }
        // fill can dataFE
//        System.arraycopy(canData,0,canPkgData,10,canData.length);

        return canPkgData;
    }

    public static int getInteger(byte value) {
        return (value & 0xFF);
    }

    public static int hexChar2Int(int value) {
        switch (value) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '[':
            case '\\':
            case ']':
            case '^':
            case '_':
            case '`':
            default:
                return -1;
            case 'A':
            case 'a':
                return 10;
            case 'B':
            case 'b':
                return 11;
            case 'C':
            case 'c':
                return 12;
            case 'D':
            case 'd':
                return 13;
            case 'E':
            case 'e':
                return 14;
            case 'F':
            case 'f':
                return 15;
        }
    }

    public static byte intToHexChar(int value) {
        byte result;
        switch (value) {
            case 0:
                result = '0';
                break;
            case 1:
                result = '1';
                break;
            case 2:
                result = '2';
                break;
            case 3:
                result = '3';
                break;
            case 4:
                result = '4';
                break;
            case 5:
                result = '5';
                break;
            case 6:
                result = '6';
                break;
            case 7:
                result = '7';
                break;
            case 8:
                result = '8';
                break;
            case 9:
                result = '9';
                break;
            case 10:
                result = 'A';
                break;
            case 11:
                result = 'B';
                break;
            case 12:
                result = 'C';
                break;
            case 13:
                result = 'D';
                break;
            case 14:
                result = 'E';
                break;
            case 15:
                result = 'F';
                break;
            default:
                throw new NumberFormatException();
        }

        return result;
    }

    public static Bitmap changeBitmap(Bitmap bitmap){
        Matrix matrix = new Matrix();
        //转换角度 rotation
        int rotation = -90;
        matrix.setRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 安装apk
     *
     * @param
     */
    public static void installApk(Context mContext,String saveFileName) {
        File apkfile = new File(saveFileName);
        Uri uri = Uri.fromFile(apkfile);
        Intent intent = new Intent();
        intent.setClassName("com.android.packageinstaller",
                "com.android.packageinstaller.PackageInstallerActivity");
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        //android.os.Process.killProcess(android.os.Process.myPid());//如果不加，最后不会提示完成、打开。
    }

    /**
     * 图片闪烁动画
     * @param iv
     */
    public static void pictureFlickering(ImageView iv,int type){
        AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation1.setDuration(1000);
        alphaAnimation1.setRepeatCount(Animation.INFINITE);
        alphaAnimation1.setRepeatMode(Animation.RESTART);
        iv.setAnimation(alphaAnimation1);
        if (type==1) {
            alphaAnimation1.start();
        }else {
            alphaAnimation1.cancel();
        }
    }

    /**
     * 获取app屏幕亮度
     *
     * @param activity
     * @return
     */
    public static int getScreenBrightness(Activity activity) {
        int value = 0;
        ContentResolver cr = activity.getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
        }
        return value;
    }

    /**
     * 语音设置
     * @param mLocale
     */
    public static void setLanguage(Locale mLocale) {
        try {
            Class localPicker = Class.forName("com.android.internal.app.LocalePicker");
            Method updateLocale = localPicker.getDeclaredMethod("updateLocale",
                    Locale.class);
            updateLocale.invoke(null,mLocale);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
                 | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /*设置语言*/
    public static void setAppLanguage(Context context,Locale locale) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        //Android 7.0以上的方法
        if (Build.VERSION.SDK_INT >= 24) {
            configuration.setLocale(locale);
            configuration.setLocales(new LocaleList(locale));
            context.createConfigurationContext(configuration);
            //实测，updateConfiguration这个方法虽然很多博主说是版本不适用
            //但是我的生产环境androidX+Android Q环境下，必须加上这一句，才可以通过重启App来切换语言
            resources.updateConfiguration(configuration, metrics);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //Android 4.1 以上方法
            configuration.setLocale(locale);
            resources.updateConfiguration(configuration, metrics);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, metrics);
        }
    }

    /**
     * 取出数组中的最大值
     *
     * @param arr
     * @return
     */
    public static int getMax(Integer[] arr) {
        if (arr.length>0) {
            int max = arr[0];
            for (int i = 1; i < arr.length; i++) {
                if (arr[i] > max) {
                    max = arr[i];
                }
            }
            return max;
        }
        return 0;
    }

    public static short get16Data(byte x1,byte x2){
        byte[] bytes = new byte[] {x1,x2}; // 你的16位有符号数的字节数据
        short value;
        value = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
        return value;
    }

    public static int get32Data(byte x1,byte x2,byte x3,byte x4){
        byte[] bytes = new byte[] {x1,x2,x3,x4}; // 你的32位有符号数的字节数据
        int value;
        value = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return value;
    }

    public static String getTwoDecimal(double a){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.CHINA);
        DecimalFormat df = new DecimalFormat("#0.00",symbols);
        String s=df.format(a);
        if (s.equals("-0.00")){
            s="0.00";
        }
        return s;
    }

    public static String getOneDecimal(double a){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.CHINA);
        DecimalFormat df = new DecimalFormat("#0.0",symbols);
        String s=df.format(a);
        if (s.equals("-0.0")){
            s="0.0";
        }
        return s;
    }

    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 截屏
     * @param context
     */
    public static void getScreenshot(Activity context){
        View rootView = context.getWindow().getDecorView().getRootView();
        Bitmap screenshot = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(screenshot);
        rootView.draw(canvas);
        try {
            FileOutputStream fos = new FileOutputStream("/sdcard/screenshot.png");
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据语言类型设置文本大小
     *
     * @param text      TextView控件
     */
    public static void setTextSizeByLanguage(TextView text) {
        int languageState = MMKVUtils.getInstance().decodeInt("languageState");
        if (languageState!=0){
            text.setTextSize(10);
        }
    }

}
