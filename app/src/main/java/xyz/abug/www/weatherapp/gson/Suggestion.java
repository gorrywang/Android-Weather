package xyz.abug.www.weatherapp.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dell on 2017/6/7.
 */

public class Suggestion {

    @SerializedName("comf")
    public Comfort cmfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    public class Comfort{
        @SerializedName("txt")
        public String info;
    }

    public class CarWash{
        @SerializedName("txt")
        public String info;
    }

    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
