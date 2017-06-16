package xyz.abug.www.weatherapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import xyz.abug.www.weatherapp.gson.Forecast;
import xyz.abug.www.weatherapp.gson.Weather;
import xyz.abug.www.weatherapp.service.AutoUpdateService;
import xyz.abug.www.weatherapp.utils.HttpUtils;
import xyz.abug.www.weatherapp.utils.Utility;
import xyz.abug.www.weatherapp.utils.Utils;

import static xyz.abug.www.weatherapp.R.id.main_text_status;

public class MainActivity extends AppCompatActivity {
    private TextView mTextWd, mTextCityName, mTextStatus, mTextTime, mTextAqi, mTextPm25, mTextSsd, mTextXczs, mTextYdzs, mTextTitle;
    private LinearLayout mLinear;
    private SharedPreferences preferences;
    private DrawerLayout mDrawer;
    private MyRe mMyRe;
    private ImageView imageView, imgDw;
    private SwipeRefreshLayout shuaxin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        bindID();
        AutoUpdate();
        downPic();
        mMyRe = new MyRe();
        IntentFilter intentFilter = new IntentFilter("xyz.abug.www.hhh");
        registerReceiver(mMyRe, intentFilter);
        //获取ip
        if (HttpUtils.isNetworkAvailable(MainActivity.this)) {
            boolean firstRun = Utils.isFirstRun(MainActivity.this);
            if (firstRun) {
//                Toast.makeText(MainActivity.this, "数据：" + firstRun, Toast.LENGTH_SHORT).show();
                getIp();
            } else {
                lookDate();
            }
        } else {
            Toast.makeText(MainActivity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
            lookDate();
        }
    }

    /**
     * 获取IP地址
     */
    private void getIp() {
        //获取json
        HttpUtils.sendOkHttpRequest("https://ipip.yy.com/get_ip_info.php", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                Log.e("tag", string + "");
                String s1 = string.split("var returnInfo = ")[1];
                String split = s1.split(";")[0];
                String s = Utility.handleIpResponse(split);

                String decode = Utils.decode(s);
                Log.e("tag", decode + "");
                HttpUtils.sendOkHttpRequest("https://api.heweather.com/v5/search?city=" + decode + "&key=aac11d46b15448b5984151cb5e1f4814", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("tag", "错误");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lookDate();
                            }
                        });

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String string = response.body().string();
                        final String id = Utility.handleIdResponse(string);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("tag", id);
                                //放到缓存sp
                                if (preferences == null) {
                                    preferences = getSharedPreferences("text", Context.MODE_PRIVATE);
                                }
                                String string1 = preferences.getString("id", "CN101010100");
                                if (!string1.equals(id)) {
                                    SharedPreferences.Editor edit = preferences.edit();
                                    edit.clear();
                                    edit.putString("id", id);
                                    edit.putInt("dw", 1);
                                    edit.commit();
                                }
                                Toast.makeText(MainActivity.this, "当前城市已定位", Toast.LENGTH_SHORT).show();
                                lookDate();
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 服务自动更新
     */
    private void AutoUpdate() {
        Intent intent = new Intent(MainActivity.this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 下载数据
     */
    private void downPic() {
        new Thread() {
            @Override
            public void run() {
                Glide.get(MainActivity.this).clearDiskCache();
            }
        }.start();
        Glide.with(this).load("https://bing.ioliu.cn/v1/rand?&w=480&h=800").skipMemoryCache(false).into(imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMyRe);
    }

    /**
     * 数据
     */
    private void lookDate() {
        if (preferences == null) {
            preferences = getSharedPreferences("text", Context.MODE_PRIVATE);
        }
        String id = preferences.getString("id", "CN101010100");
        String weatherDate = preferences.getString("weatherDate", null);
        if (weatherDate != null) {
            //不等于空
            //显示数据
            Weather weather = Utility.handleWeatherResponse(weatherDate);
            showDate(weather);
        } else {
            //空
            //下载数据
            requestWeather(id);

        }
    }

    /**
     * 显示数据
     */
    private void showDate(Weather weather) {
        //是否定位
        int dw = preferences.getInt("dw", 0);
        if (dw == 0) {
            imgDw.setVisibility(View.GONE);
        } else {
            imgDw.setVisibility(View.VISIBLE);
        }
        //城市名称
        mTextCityName.setText(weather.basic.cityName);
        try {
            //当前气温
            mTextWd.setText(weather.now.temperature + "°C");
            //当前状态
            mTextStatus.setText(weather.now.more.info);
            //更新时间
            mTextTime.setText((weather.basic.update.updateTime.trim()).split(" ")[1]);
            //aqi
            mTextAqi.setText(weather.aqi.city.aqi + "");
            //pm2.5
            mTextPm25.setText(weather.aqi.city.pm25 + "");
            //舒适度
            mTextSsd.setText("舒适度：" + weather.suggestion.cmfort.info);
            //洗车指数
            mTextXczs.setText("洗车指数：" + weather.suggestion.carWash.info);
            //运动指数
            mTextYdzs.setText("运动指数：" + weather.suggestion.sport.info);
            //日期指数
            List<Forecast> list = weather.forecastList;
            mLinear.removeAllViews();
            for (Forecast forecast : list) {
                View inflate = LayoutInflater.from(MainActivity.this).inflate(R.layout.forcast_item, null);
                TextView mDate = (TextView) inflate.findViewById(R.id.date_text);
                TextView mInfo = (TextView) inflate.findViewById(R.id.info_text);
                TextView mMax = (TextView) inflate.findViewById(R.id.max_text);
                TextView mMin = (TextView) inflate.findViewById(R.id.min_text);
                LinearLayout item_linear = (LinearLayout) inflate.findViewById(R.id.item_linear);
                mDate.setText(forecast.date);
                mInfo.setText(forecast.more.info);
                mMax.setText(forecast.temperature.max);
                mMin.setText(forecast.temperature.min);
                item_linear.setPadding(25, 25, 25, 25);
                mLinear.addView(inflate);

            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "当前城市数据不完整", Toast.LENGTH_SHORT).show();
            //当前气温
            mTextWd.setText("--");
            //当前状态
            mTextStatus.setText("--");
            //更新时间
            mTextTime.setText("--");
            //aqi
            mTextAqi.setText("--");
            //pm2.5
            mTextPm25.setText("--");
            //舒适度
            mTextSsd.setText("--");
            //洗车指数
            mTextXczs.setText("--");
            //运动指数
            mTextYdzs.setText("--");
            //日期指数
            mLinear.removeAllViews();
        }
        shuaxin.setRefreshing(false);
    }

    /**
     * 请求数据,子线程
     *
     * @param id
     */
    private void requestWeather(final String id) {

        if (!HttpUtils.isNetworkAvailable(MainActivity.this)) {
            shuaxin.setRefreshing(false);
            return;
        }

        shuaxin.setRefreshing(true);
        String url = "http://guolin.tech/api/weather?cityid=" + id + "&key=aac11d46b15448b5984151cb5e1f4814";
        HttpUtils.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(string);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor edit = preferences.edit();
                            edit.putString("id", id);
                            edit.putString("weatherDate", string);
                            edit.commit();
                            //显示数据
                            showDate(weather);
                        }
                    }
                });
            }
        });
    }

    private void bindID() {
        mTextWd = (TextView) findViewById(R.id.main_text_wendu);
        mTextCityName = (TextView) findViewById(R.id.main_text_cityname);
        mTextStatus = (TextView) findViewById(main_text_status);
        mTextTime = (TextView) findViewById(R.id.main_text_updatetime);
        mTextAqi = (TextView) findViewById(R.id.main_text_aqi);
        mTextPm25 = (TextView) findViewById(R.id.main_text_pm25);
        mTextSsd = (TextView) findViewById(R.id.main_text_ssd);
        mTextXczs = (TextView) findViewById(R.id.main_text_xczs);
        mTextYdzs = (TextView) findViewById(R.id.main_text_ydzs);
        mLinear = (LinearLayout) findViewById(R.id.forecast_layout);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        imageView = (ImageView) findViewById(R.id.imageview);
        imgDw = (ImageView) findViewById(R.id.image_dw);
        shuaxin = (SwipeRefreshLayout) findViewById(R.id.shuaxin);
        shuaxin.setColorSchemeResources(R.color.colorPrimary);
        shuaxin.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                String id = preferences.getString("id", "CN101010100");
//                requestWeather(id);
                shuaxin.setRefreshing(true);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "暂无更新", Toast.LENGTH_SHORT).show();
                                shuaxin.setRefreshing(false);
                            }
                        });
                    }
                }.start();

            }
        });
        mTextTitle = (TextView) findViewById(R.id.main_text_morecity);
        mTextTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(GravityCompat.START);
            }
        });

    }

    class MyRe extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //关闭并更新
            mDrawer.closeDrawer(GravityCompat.START);
            lookDate();
        }
    }
}
