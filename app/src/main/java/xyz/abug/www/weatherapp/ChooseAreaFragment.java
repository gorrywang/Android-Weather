package xyz.abug.www.weatherapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import xyz.abug.www.weatherapp.db.City;
import xyz.abug.www.weatherapp.db.County;
import xyz.abug.www.weatherapp.db.Province;
import xyz.abug.www.weatherapp.utils.HttpUtils;
import xyz.abug.www.weatherapp.utils.Utility;

/**
 *
 * Created by Dell on 2017/6/7.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleView;
    private Button backButton;
    protected ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 区列表
     */
    private List<County> countyList;
    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private City selectedCity;
    /**
     * 当前选中的级别
     */
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleView = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        //初始化adapter
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    //选择城市
                    selectedProvince = provinceList.get(position);
                    //查询
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    //查询区县
                    queryCounty();
                } else if (currentLevel == LEVEL_COUNTY) {
                    //更新数据
                    SharedPreferences sp = getActivity().getSharedPreferences("text", getContext().MODE_PRIVATE);
                    SharedPreferences.Editor edit = sp.edit();
                    edit.clear();
                    edit.putString("id", countyList.get(position).getWeatherId());
                    edit.commit();
                    //发送广播
                    Intent intent = new Intent("xyz.abug.www.hhh");
                    getActivity().sendBroadcast(intent);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    //查询市
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    //查询省
                    queryProvinces();
                }
            }
        });
        //查询省
        queryProvinces();
    }

    /**
     * 查询省
     */
    private void queryProvinces() {
        titleView.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            //有数据
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            //无数据，下载省数据
            String address = "http://guolin.tech/api/china/";
            queryFromService(address, "province");
        }
    }

    /**
     * 查询市
     */
    private void queryCity() {
        titleView.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            //有数据
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            //无数据，下载省数据
            String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
            queryFromService(address, "city");
        }
    }

    /**
     * 查询区
     */
    private void queryCounty() {
        titleView.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            //有数据
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            //无数据，下载省数据
            String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode() + "/" + selectedCity.getCityCode();
            queryFromService(address, "county");

        }
    }

    /**
     * 下载数据
     *
     * @param address
     * @param province
     */
    private void queryFromService(String address, final String province) {
        showProgressDialog();
        HttpUtils.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
                closeProgressDialog();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String data = response.body().string();
                boolean result = false;
                if ("province".equals(province)) {
                    //省
                    result = Utility.handleProvinceResponse(data);
                } else if ("city".equals(province)) {
                    //市
                    result = Utility.handleCityResponse(data, selectedProvince.getId());
                } else if ("county".equals(province)) {
                    //区
                    result = Utility.handleCountyResponse(data, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(province)) {
                                //省
                                queryProvinces();
                            } else if ("city".equals(province)) {
                                //市
                                queryCity();
                            } else if ("county".equals(province)) {
                                //区
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("加载中……");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }


}
