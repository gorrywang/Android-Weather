package xyz.abug.www.weatherapp.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dell on 2017/6/7.
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
