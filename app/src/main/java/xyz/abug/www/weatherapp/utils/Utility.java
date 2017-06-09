package xyz.abug.www.weatherapp.utils;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xyz.abug.www.weatherapp.db.City;
import xyz.abug.www.weatherapp.db.County;
import xyz.abug.www.weatherapp.db.Province;
import xyz.abug.www.weatherapp.gson.Weather;


/**
 * Created by Dell on 2017/6/7.
 * 解析json
 */

public class Utility {
    /**
     * 处理解析省级数据
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            //有数据
            try {
                JSONArray allProvince = new JSONArray(response);
                for (int i = 0; i < allProvince.length(); i++) {
                    JSONObject object = allProvince.getJSONObject(i);
                    int id = object.getInt("id");
                    String name = object.getString("name");
                    Province province = new Province();
                    province.setProvinceName(name);
                    province.setProvinceCode(id);
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 处理解析市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            //有数据
            try {
                JSONArray allProvince = new JSONArray(response);
                for (int i = 0; i < allProvince.length(); i++) {
                    JSONObject object = allProvince.getJSONObject(i);
                    int id = object.getInt("id");
                    String name = object.getString("name");
                    City city = new City();
                    city.setCityName(name);
                    city.setCityCode(id);
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 处理解析区级数据
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            //有数据
            try {
                JSONArray allProvince = new JSONArray(response);
                for (int i = 0; i < allProvince.length(); i++) {
                    JSONObject object = allProvince.getJSONObject(i);
                    String name = object.getString("name");
                    String weather_id = object.getString("weather_id");
                    County county = new County();
                    county.setCountyName(name);
                    county.setCityId(cityId);
                    county.setWeatherId(weather_id);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析天气
     */
    public static Weather handleWeatherResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            //有数据
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray heWeather = jsonObject.getJSONArray("HeWeather");
                String s = heWeather.getJSONObject(0).toString();
                return new Gson().fromJson(s, Weather.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 解析Ip地址
     */
    public static String handleIpResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String cip = jsonObject.getString("cip");
            return cip;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 解析天气id
     */
    public static String handleIdResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray heWeather5 = jsonObject.getJSONArray("HeWeather5");
            JSONObject jsonObject1 = heWeather5.getJSONObject(0);
            JSONObject basic = jsonObject1.getJSONObject("basic");
            String id = basic.getString("id");
            return id;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
