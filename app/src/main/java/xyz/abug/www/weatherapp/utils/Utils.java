package xyz.abug.www.weatherapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Dell on 2017/6/9.
 */

public class Utils {
    /**
     * 判断程序是否第一次运行
     *
     * @param context
     * @return
     */
    public static boolean isFirstRun(Context context) {
        SharedPreferences sp = context.getSharedPreferences("aaa", context.MODE_PRIVATE);
        boolean isFirstRun = sp.getBoolean("isFirstRun", true);
        if (isFirstRun) {
            //第一次运行
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isFirstRun", false);
            editor.commit();
        }
        return isFirstRun;
    }
}
