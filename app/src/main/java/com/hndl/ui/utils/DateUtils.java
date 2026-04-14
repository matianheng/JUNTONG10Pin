package com.hndl.ui.utils;

import android.app.AlarmManager;
import android.content.Context;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static Date getNetTime() {
        String webUrl = "http://www.ntsc.ac.cn";//中国科学院国家授时中心try
        URL url = null;
        try {
            url = new URL(webUrl);
            URLConnection uc = url.openConnection();
            uc.setReadTimeout(5000);
            uc.setConnectTimeout(5000);
            uc.connect();
            long correctTime = uc.getDate();
            Date date = new Date(correctTime);
            return date;
        } catch (MalformedURLException e) {
            return new Date();
        } catch (IOException e) {
            return new Date();
        }
    }

    public static  String minuteTotime(int s){
        String str="";
        if (s<60){
            str=s+"  s";
        }else if ((s/60)<60){
            str=(s/60)+"  min  "+(s%60)+"  s";
        }else {
            str=(s/60/60)+"  h  "+(s/60%60)+"  min  "+(s%60)+"  s";
        }
        return str;
    }

    /**
     * 设置系统日期
     * @param context
     * @param year
     * @param month
     * @param day
     */
    public static void setSysDate(Context context, int year, int month, int day){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);

        long when = c.getTimeInMillis();

        if(when / 1000 < Integer.MAX_VALUE){
            ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    /**
     * 设置系统的时间
     * @param context
     * @param hour
     * @param minute
     */
    public static void setSysTime(Context context,int hour,int minute){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        long when = c.getTimeInMillis();

        if(when / 1000 < Integer.MAX_VALUE){
            ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    /**
     * 计算两个日期之间的天数
     * @param startDate 起始日期，格式为yyyy-MM-dd
     * @param endDate 结束日期，格式为yyyy-MM-dd
     * @return 两个日期之间的天数
     */
    public static int calculateDays(String startDate, String endDate) {
        // 步骤1：获取起始日期和结束日期的年、月、日
        int startYear = Integer.parseInt(startDate.substring(0, 4));
        int startMonth = Integer.parseInt(startDate.substring(5, 7));
        int startDay = Integer.parseInt(startDate.substring(8, 10));

        int endYear = Integer.parseInt(endDate.substring(0, 4));
        int endMonth = Integer.parseInt(endDate.substring(5, 7));
        int endDay = Integer.parseInt(endDate.substring(8, 10));

        // 步骤2：判断起始日期和结束日期是否在同一年
        if (startYear == endYear) {
            // 步骤4：计算同一年内的天数
            return calculateDaysInSameYear(startYear, startMonth, startDay, endMonth, endDay);
        } else {
            // 步骤3：计算跨年天数
            int daysInFirstYear = calculateDaysInSameYear(startYear, startMonth, startDay, 12, 31);
            int daysInLastYear = calculateDaysInSameYear(endYear, 1, 1, endMonth, endDay);
            int daysInBetweenYears = calculateDaysInBetweenYears(startYear, endYear);

            // 步骤5：返回总天数
            return daysInFirstYear + daysInBetweenYears + daysInLastYear;
        }
    }

    /**
     * 计算同一年内的天数
     * @param year 年份
     * @param startMonth 起始月份
     * @param startDay 起始日期
     * @param endMonth 结束月份
     * @param endDay 结束日期
     * @return 同一年内的天数
     */
    private static int calculateDaysInSameYear(int year, int startMonth, int startDay, int endMonth, int endDay) {
        // 使用Calendar类进行日期计算
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, startMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, startDay);
        long startTimeInMillis = calendar.getTimeInMillis();

        calendar.set(Calendar.MONTH, endMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, endDay);
        long endTimeInMillis = calendar.getTimeInMillis();

        long daysInMillis = endTimeInMillis - startTimeInMillis;
        return (int) (daysInMillis / (24 * 60 * 60 * 1000)) + 1;
    }

    /**
     * 计算跨年天数
     * @param startYear 起始年份
     * @param endYear 结束年份
     * @return 跨年天数
     */
    private static int calculateDaysInBetweenYears(int startYear, int endYear) {
        int daysInBetweenYears = 0;
        for (int year = startYear + 1; year < endYear; year++) {
            // 判断是否为闰年
            if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
                daysInBetweenYears += 366;
            } else {
                daysInBetweenYears += 365;
            }
        }
        return daysInBetweenYears;
    }

    /**
     * 计算两个时间相差多少分钟
     * @return 两个日期之间的天数
     */
    public static int calculateMinutes (String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (startTime.length()<17){
            startTime=startTime+":00";
        }
        if (endTime.length()<17){
            endTime=endTime+":00";
        }
        try {
            Date startDate = sdf.parse(startTime); // 转换为起始日期对象
            Date endDate = sdf.parse(endTime); // 转换为结束日期对象

            long startTimeInMillis = startDate.getTime(); // 起始时间的毫秒数
            long endTimeInMillis = endDate.getTime(); // 结束时间的毫秒数

            long timeDiffInMillis = endTimeInMillis - startTimeInMillis; // 时间差（毫秒）
            long seconds = timeDiffInMillis / 1000; // 时间差（秒）
            long minutes = seconds / 60; // 时间差（分钟）

            return (int) minutes;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
