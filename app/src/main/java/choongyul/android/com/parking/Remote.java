package choongyul.android.com.parking;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by myPC on 2017-03-06.
 */

public class Remote {

    // Http url 커넥션으로 사이트에서 정보를 긁어올 것이다.
    public void getData(final Callback obj) {

        String urlString = obj.getUrl();


        if(!urlString.startsWith("http")) {
            urlString = "http://" + urlString;
        }

        new AsyncTask<String,Void,String>() {

            ProgressDialog dialog = new ProgressDialog(obj.getContext());
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                // 프로그래스 다이얼로그 세팅
                dialog.setProgress(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("불러오는중...");
                dialog.show();
            }

            @Override
            protected String doInBackground(String... params) {
                String urlString = params[0];

                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String lineOfData = "";
                        while ((lineOfData = br.readLine()) != null) {
                            result.append(lineOfData);
                        }
                        connection.disconnect();
                        return result.toString();

                    } else {
                        Log.e("HTTOConnection", "Error Code = " + responseCode);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;

            }
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                // 결과 값 출력
//                Log.i("Remote",result);
                dialog.dismiss();
                // remote 객체를생성한 측의 callback 함수 호출
                obj.call(result);

            }

        }.execute(urlString);
    }

    public interface Callback{
        public Context getContext();
        public String getUrl();
        public void call(String jsonString);
    }

}
