package xyz.abug.www.weatherapp.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dell on 2017/6/7.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }
}
