package choongyul.android.com.parking;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , Remote.Callback{

    private GoogleMap mMap;

    private String hangleParameter = "";
    private String url = "http://openapi.seoul.go.kr:8088/566d677961726f6331397471525a50/json/SearchParkingInfo/1/1000/";
    Remote remote;
    Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        spinner = (Spinner) findViewById(R.id.spinner);
        // TODO 시군구좌표값 스피너에 설정
        // 키값 http://openapi.seoul.go.kr:8088/566d677961726f6331397471525a50/xml/SdeTlSccoSigW/1/30/
        // url = remote


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            hangleParameter = URLEncoder.encode("중구","UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        url = url + hangleParameter;


        // 1. 공영 주차장 마커 전체를 화면에 출력
        remote = new Remote();
        remote.getData(this);

        // 2. 중심점을 서울로 이동
        LatLng seoul = new LatLng(37.5666696, 126.977942);
//        mMap.addMarker(new MarkerOptions().position(seoul).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul,10));


    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void call(String jsonString) {

        //MainActivity 화면에 뭔가를 세팅해 주면 remote 에서 이 함수를 호출한다.

        JSONObject urlObject = null;
        try {
            // 1. json String 전체를 JSONObject로 변환
            urlObject = new JSONObject(jsonString);

        // 2. JSONObject 중에 최상위의 object를 꺼낸다.
            JSONObject rootObject = urlObject.getJSONObject("SearchParkingInfo"); // 루트 뎁스 이름을 꺼내온다.
            // 3. 사용하려는주차장 정보들을 JSONArray로 꺼낸다.
            //    이 데이터를 rootObject 바로 아래에 실제 정보가 있지만 계층 구조상 더 아래에 존재할 수도 있다.
            JSONArray rows = rootObject.getJSONArray("row");
            int arrayLength = rows.length();

            List<String> parkCode = new ArrayList<>();

            for (int i = 0; i < arrayLength; i++) {

                JSONObject park = rows.getJSONObject(i);

                // 중복제거
               String code = park.getString("PARKING_CODE");
                if( parkCode.contains(code)) {
                    continue;
                }
                parkCode.add(code);



                double lat = getDouble(park, "LAT");
                double lng = getDouble(park, "LNG");

                int capacity = getInt(park, "CAPACITY");
                int current = getInt(park, "CUR_PARKING");
                int space = capacity - current;

                LatLng parking = new LatLng(lat, lng);
                MarkerOptions marker = new MarkerOptions();
                marker.position(parking);
                marker.title(space + " / " + capacity);

                mMap.addMarker(marker).showInfoWindow();



//                mMap.addMarker(new MarkerOptions().position(parking).title(space + " / " + capacity)).showInfoWindow();

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 제이슨은 제이슨 오브젝트와 어레이로만 구성되어있다.
        // 대괄호는 어레이
    }

    private double getDouble(JSONObject obj, String key) {
        double result = 0;
        try{
            result = obj.getDouble(key);
        } catch (JSONException e) {

        }

        return result;
    }
    private int getInt(JSONObject obj, String key) {
        int result = 0;
        try{
            result = obj.getInt(key);
        } catch (JSONException e) {

        }

        return result;
    }
}
