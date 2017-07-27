//package com.step.pedometer.mystep.utils;
package com.step.pedometer.mystep.utils;

import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
//import org.apache.commons.httpclient.*;
//import org.apache.commons.httpclient.methods.*;
//import org.apache.commons.httpclient.params.HttpMethodParams;
import com.step.pedometer.mystep.MainActivity;
import com.step.pedometer.mystep.R;

import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by jinglwa on 7/25/2017.
 */


public class PathLoadAsync extends AsyncTask<String, Integer, String> {
    private String response_ = "";
    private PathView pathView_ = null;

    MainActivity myMain=null;


//    public interface AsyncResponse {
//        void processFinish(String output);
//    }

    public void setView (PathView pview, MainActivity _myMain) {
        pathView_ = pview;
        myMain = _myMain;
    }
    @Override
    protected String doInBackground(String... params) {
        Log.v("test", "start connection");
        long startTime = System.currentTimeMillis();
        HttpURLConnection urlConnection = null;

        if (params.length < 3) {
            return "";
        }
        String floor = params[0];
        String start_room = params[1];
        String end_room = params[2];

        String temp = "";

        try {
            response_ = "";
//            String url_str = String.format("https://roomassistant.cloudapp.net/api/Paths/getPathArrayAsync?floor=%s&startNumber=%s&endNumber=%s", floor, start_room, end_room);
            String url_str = String.format("https://roomassistant.cloudapp.net/api/paths/getcornerarrayasync?floor=%s&startnumber=%s&endnumber=%s", floor, start_room, end_room);
            URL url = new URL(url_str);
            urlConnection = (HttpURLConnection) url.openConnection();
            long connectTime   = System.currentTimeMillis();
            long totalTime = connectTime - startTime;
            Log.v("test_connecttime", String.valueOf(totalTime/1000) + " s");

            InputStream inStream = new BufferedInputStream(urlConnection.getInputStream());

            connectTime   = System.currentTimeMillis();
            totalTime = connectTime - startTime;
            Log.v("test_buffertime", String.valueOf(totalTime/1000) + " s");

            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            while ((temp = bReader.readLine()) != null) {
                response_ += temp;
            }
        } catch (Exception e) {
//            this.mException = e;
        } finally{
            urlConnection.disconnect();
        }
        Log.v("test", "end connection");
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        Log.v("test_loadtime", String.valueOf(totalTime/1000) + " s");
        return response_;
    }


    @Override
    protected void onPostExecute(String res) {
        if (pathView_ != null) {
            long startTime = System.currentTimeMillis();

//            VectorPath vecPath = new VectorPath();
//            pathView_.setPoints(vecPath.readFromString(res, viewRatioX_, viewRatioY_));
            pathView_.loadPoints(res);

            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            Log.v("test_parsetime", String.valueOf(totalTime/1000) + " s");

            // post process points
//            postProcessPoints(pathView_.getPoint());

            myMain.SetPos((int)pathView_.pts_.get(0).x,(int)pathView_.pts_.get(0).y);
            myMain.startGetNearestCorner();

            myMain.initStepCountButtonClick = myMain.curStepCount;

        }

    }

//    protected void postProcessPoints(List<Point> pts) {
//
//    }

    public String getResponse(){
//        Log.e("test_response", response_);
        return response_;
    }
}


