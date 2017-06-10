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

    public static String decode(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuffer retBuf = new StringBuffer();
        int maxLoop = unicodeStr.length();
        for (int i = 0; i < maxLoop; i++) {
            if (unicodeStr.charAt(i) == '\\') {
                if ((i < maxLoop - 5) && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr.charAt(i + 1) == 'U')))
                    try {
                        retBuf.append((char) Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        retBuf.append(unicodeStr.charAt(i));
                    }
                else
                    retBuf.append(unicodeStr.charAt(i));
            } else {
                retBuf.append(unicodeStr.charAt(i));
            }
        }
        return retBuf.toString();
    }
}
