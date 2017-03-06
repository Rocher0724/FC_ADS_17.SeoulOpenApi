package choongyul.android.com.parking;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

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
    private String selectedGoo = "강남구";
    private String url = "http://openapi.seoul.go.kr:8088/566d677961726f6331397471525a50/json/SearchParkingInfo/1/1000/";
    Remote remote;
    Spinner spinner;
    String gooArray[];
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        gooArray = new String[]{
                "강남구","강동구","강북구","강서구","관악구",
                "광진구","구로구", "금천구", "노원구", "도봉구",
                "동대문구","동작구", "마포구", "서대문구", "서초구",
                "성동구","성북구","송파구", "양천구", "영등포구",
                "용산구","은평구", "종로구", "중구","중랑구"
        };
        spinner = (Spinner) findViewById(R.id.spinner);
        // 스피너 데이터를 adapter를 사용하여 등록한다.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, gooArray);
        //                                                      ^ 컨텍스트      ^스피너에서 사용할 레이아웃    ,       ^ 배열데이터
        // 3.2 스피너에 아답터 등록
        spinner.setAdapter(adapter);
        // 3.3 스피너 리스너에 등록
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 어떤 position이 선택되었는지 표시하는 toast 메시지 출력
//                mMap.clear();
                Toast.makeText(MapsActivity.this, gooArray[position] + "선택하셨습니다.", Toast.LENGTH_SHORT).show();
                selectedGoo = gooArray[position];
                Log.e("Main","선택된 구 = " + selectedGoo);

//                onMapReady(mMap);
                mapFragment.getMapAsync(MapsActivity.this);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택하지 않았을때 무언가 하고싶을때
            }
        });


        // TODO 시군구좌표값 스피너에 설정
        // 키값 http://openapi.seoul.go.kr:8088/566d677961726f6331397471525a50/xml/SdeTlSccoSigW/1/30/
        // url = remote


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.e("Main","OnMapReady랑께");

        try {

            hangleParameter = URLEncoder.encode(selectedGoo, "UTF-8");

            Log.e("Main","flase랑께");

            Log.e("Main","한글파라미터 = " + hangleParameter);
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
        Log.e("Main getUrl","주어지는 url 은 " + url);

        return url;
    }

    @Override
    public void call(String jsonString) {
        Log.e("Main","call메소드");
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

                mMap.addMarker(marker);



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
