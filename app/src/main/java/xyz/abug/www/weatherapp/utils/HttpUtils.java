package xyz.abug.www.weatherapp.utils;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Dell on 2017/6/7.
 * 网络连接
 */

public class HttpUtils {
    public static void sendOkHttpRequest(String address, Callback callback) {
        //客户端
        OkHttpClient client = new OkHttpClient();
        //请求
        Request request = new Request.Builder().url(address).build();
        //客户端发起请求返回相应
        client.newCall(request).enqueue(callback);
    }
}
