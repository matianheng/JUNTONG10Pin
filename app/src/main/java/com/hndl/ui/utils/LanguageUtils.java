package com.hndl.ui.utils;

import android.annotation.SuppressLint;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Locale;

public class LanguageUtils {
    private static final String TAG = LanguageUtils.class.getSimpleName();

    //默认语言
    public static final Locale LOCALE_DEFAULT = Locale.ENGLISH;

    /**
     * 选择语言
     *
     * @param localeKey      localeKey
     * @param isUpdateSystem 是否更新系统语言
     * @return 选择成功true, 反之false
     */
    public static boolean switchLanguage(Context context, String localeKey, boolean isUpdateSystem) {
        if (context == null) {
            return false;
        }
        Locale locale = getLocale(localeKey);
        return switchLanguage(context, locale, isUpdateSystem);
    }

    /**
     * 选择语言
     *
     * @param locale         Local
     * @param isUpdateSystem 是否更新系统语言
     * @return 选择成功true, 反之false
     */
    public static boolean switchLanguage(Context context, Locale locale, boolean isUpdateSystem) {
        if (context == null) {
            return false;
        }
        //更新主程序语言
        boolean updateMainResult = updateMainLanguage(context, locale);
        if (!updateMainResult) {
            return false;
        }
        //更新系统语言
        if (isUpdateSystem) {
            updateSystemLanguage(locale);
        }
        return true;
    }

    //更新主程序语言
    private static boolean updateMainLanguage(Context context, Locale locale) {
        Resources resources = context.getResources();
        //更新主程序的语言
        Configuration config = resources.getConfiguration();
        //主程序语言和目标语言一致则不处理
        if (config.locale.equals(locale)) {
            Log.w(TAG, "主程序语言与目标语言一致，无需处理");
            return false;
        }
        config.locale = locale;
        //更新主程序语言
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        return true;
    }

    // 更新系统语言
    @SuppressLint("PrivateApi")
    private static void updateSystemLanguage(Locale locale) {
        try {
            Object objIActMag;
            Class<?> clzIActMag = Class.forName("android.app.IActivityManager");
            Class<?> clzActMagNative = Class.forName("android.app.ActivityManagerNative");
            Method mtdActMagNative$getDefault = clzActMagNative.getDeclaredMethod("getDefault");
            objIActMag = mtdActMagNative$getDefault.invoke(clzActMagNative);
            Method mtdIActMag$getConfiguration = clzIActMag.getDeclaredMethod("getConfiguration");
            Configuration config = (Configuration) mtdIActMag$getConfiguration.invoke(objIActMag);
            if (config == null) {
                Log.w("", "updateSystemLanguage failed, config = null");
                return;
            }
            config.locale = locale;
            Class<?> clzConfig = Class.forName("android.content.res.Configuration");
            java.lang.reflect.Field userSetLocale = clzConfig.getField("userSetLocale");
            userSetLocale.set(config, true);
            // 此处需要声明权限:android.permission.CHANGE_CONFIGURATION。防止重走生命周期需要configChanges加locale
            Class<?>[] clzParams = {Configuration.class};
            Method mtdIActMag$updateConfiguration = clzIActMag.getDeclaredMethod("updateConfiguration", clzParams);
            mtdIActMag$updateConfiguration.invoke(objIActMag, config);
            BackupManager.dataChanged("com.android.providers.settings");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Locale getLocale(final String lankey) {
        if (LangKey.ZH_CN.equals(lankey)) {
            return Locale.CHINA;
        } else if (LangKey.ZH_TW.equals(lankey)) {
            return Locale.TAIWAN;
        } else if (LangKey.EN_US.equals(lankey)) {
            return Locale.ENGLISH;
        } else if (LangKey.JA_JP.equals(lankey)) {
            return Locale.JAPAN;
        } else if (LangKey.KO_KR.equals(lankey)) {
            return Locale.KOREA;
        } else if (LangKey.LO_LA.equals(lankey)) {
            return new Locale("lo", "la");
        } else if (LangKey.KM_KH.equals(lankey)) {
            return new Locale("km", "kh");
        } else if (LangKey.TL_PH.equals(lankey)) {
            return new Locale("tl", "ph");
        } else if (LangKey.MY_MM.equals(lankey)) {
            return new Locale("my", "mm");
        } else {
            String[] locales = Resources.getSystem().getAssets().getLocales();
            for (String s : locales) {
                if (s.length() == 5) {
                    Log.e(TAG, "getLocale lankey = " + lankey + ", s = " + s);
                }
            }
            return getLocale(lankey, Resources.getSystem().getAssets().getLocales());
        }
    }

    private static Locale getLocale(String lanKey, String[] locales) {
        //android7.0 平台的语言分隔符是“-”，以前的版本是下划线_
        lanKey = lanKey.replace("_", "-");
        for (int i = 0; i < locales.length; i++) {
            final String s = locales[i];
            final int len = s.length();
            if (len == 5) {
                String language = s.substring(0, 5);
                String country = s.substring(3, 5);
                Locale l = new Locale(language, country);
                if (l.getLanguage().equalsIgnoreCase(lanKey)) {
                    return new Locale(s.substring(0, 2), s.substring(3, 5));
                }
            }
        }
        return LOCALE_DEFAULT;
    }

    public static class LangKey {
        //---------------语言名字key---------------
        //中文简体
        public static final String ZH_CN = "zh_cn";
        //中文繁体
        public static final String ZH_TW = "zh_tw";
        //英语
        public static final String EN_US = "en_us";
        //日语
        public static final String JA_JP = "ja_jp";
        //韩语
        public static final String KO_KR = "ko_kr";
        //柬埔寨语
        public static final String KM_KH = "km_kh";
        //泰国
        public static final String TH_TH = "th_th";
        //法语
        public static final String FR_FR = "fr_fr";
        //越南语
        public static final String VI_VN = "vi_vn";
        //菲律宾语
        public static final String TL_PH = "tl_ph";
        //马来西亚语
        public static final String MS_MY = "ms_my";
        //缅甸语
        public static final String MY_MM = "my_mm";
        //捷克语
        public static final String CS_CZ = "cs_cz";
        //印度尼西亚语
        public static final String IN_ID = "in_id";
        //老挝语
        public static final String LO_LA = "lo_la";
        //俄语
        public static final String RU_RU = "ru_ru";
        //印度
        public static final String HI_IN = "hi_in";
    }
}
