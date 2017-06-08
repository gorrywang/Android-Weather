package xyz.abug.www.weatherapp.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import xyz.abug.www.weatherapp.utils.HttpUtils;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();

        //定时
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int hours = 60 * 60 * 8 * 1000;
        long time = SystemClock.elapsedRealtime() + hours;
        Intent intent1 = new Intent(this, AutoUpdateService.class);
        PendingIntent intent2 = PendingIntent.getService(this, 0, intent1, 0);
        manager.cancel(intent2);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, intent2);


        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气
     */
    private void updateWeather() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String id = sp.getString("id", null);
        if (id != null) {
            String url = "http://guolin.tech/api/weather?cityid=" + id + "&key=aac11d46b15448b5984151cb5e1f4814";
            HttpUtils.sendOkHttpRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String string = response.body().string();
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString("weatherDate", string);
                    edit.commit();
                }
            });
        }

    }
}
