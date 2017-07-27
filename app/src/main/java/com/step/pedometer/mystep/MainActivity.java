package com.step.pedometer.mystep;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.step.pedometer.mystep.config.Constant;
import com.step.pedometer.mystep.service.StepService;
import com.step.pedometer.mystep.utils.CompassManager;
import com.step.pedometer.mystep.utils.ImageView;
import com.step.pedometer.mystep.utils.PathLoadAsync;
import com.step.pedometer.mystep.utils.PathView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  implements Handler.Callback {
    //循环取当前时刻的步数中间的时间间隔
    private long TIME_INTERVAL = 500;

    //pixel plus
    private float STEP_TO_PIXEL_Width = 0.6f;
    private float STEP_TO_PIXEL_Height = 0.4f;

    // pixel
    //private float STEP_TO_PIXEL_Width = 0.8f;
    //private float STEP_TO_PIXEL_Height = 0.6f;


    //private float compassFilterRate = 0.5f;
    private float compassFilterRate = 0.5f;
    //for input room number
    Button myButton1;
    public Spinner sp_start;
    public Spinner sp_end;
    public String str_start;
    public String str_end;

    private boolean isinit = true;
    //gui items
    private TextView text_step;    // show the steps
    private ImageView locmark=null; // show the location mark
    private ImageView map=null;


    //sensor
    private CompassManager mCompassManager;

    //state variable
    private int mMapWidth = 0;
    private int mMapHeight = 0;
    private int markerWidth = 100;
    private int markerHeight = 100;

    private float mOrierntation = 0; //current orientation from compass
    private float mOrierntationFilter = 0;
    private float mPosX = 1440/2; // current pose infered from stepper in pixel
    private float mPosY = 2560/2; // current pose infered from stepper in pixel

    //step navigation
    public long preStepCount = 0;
    public long curStepCount = 0;
    public long initStepCountButtonClick = 0;
    boolean initStep = true;

    //plot path
    private PathView pathView_;


    private Messenger messenger;
    private Messenger mGetReplyMessenger = new Messenger(new Handler(this));
    private Handler delayHandler;


    private CompassManager.CompassLister mCompassLister = new CompassManager.CompassLister() {
        @Override
        public void onOrientationChange(float orientation) {
            mOrierntation = (orientation - 15.0f +90.0f + 360.f)%360;

        }
    };

    //以bind形式开启service，故有ServiceConnection接收回调
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                messenger = new Messenger(service);
                Message msg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                msg.replyTo = mGetReplyMessenger;
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    //接收从服务端回调的步数
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.MSG_FROM_SERVER:
                //更新步数
                text_step.setText(msg.getData().getInt("step") - initStepCountButtonClick + "");
                delayHandler.sendEmptyMessageDelayed(Constant.REQUEST_SERVER, TIME_INTERVAL);

                preStepCount = curStepCount;
                curStepCount = msg.getData().getInt("step");
                if(initStep){
                    preStepCount = curStepCount;
                    initStep = false;
                }

                break;
            case Constant.REQUEST_SERVER:
                try {
                    Message msgl = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                    msgl.replyTo = mGetReplyMessenger;
                    messenger.send(msgl);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text_step = (TextView) findViewById(R.id.main_text_step);
        delayHandler = new Handler(this);


        mCompassManager = CompassManager.getInstance();
        mCompassManager.init(this);
        mCompassManager.addCompassLister(mCompassLister);


        myButton1=(Button)findViewById(R.id.button);
        myButton1.setOnClickListener(new MainActivity.ButtonClick());


        sp_start = (Spinner) findViewById(R.id.spinner_start);
        str_start = (String) sp_start.getSelectedItem();
        sp_start.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                //拿到被选择项的值
                str_start = (String) sp_start.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        sp_end = (Spinner) findViewById(R.id.spinner_end);
        str_end = (String) sp_end.getSelectedItem();
        sp_end.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                //拿到被选择项的值
                str_end = (String) sp_end.getSelectedItem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        setupService();

        map = (ImageView) findViewById(R.id.mapid);
        map.setImg(BitmapFactory.decodeResource(getResources(), R.drawable.l2big));


        locmark = (ImageView) findViewById(R.id.locmarkid);
        locmark.setImg(BitmapFactory.decodeResource(getResources(), R.drawable.marker));



        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { //HERE is the main loop
                        if(isinit){
                            mMapHeight = map.getHeight();
                            mMapWidth = map.getWidth();
                            map.setSize(mMapWidth,mMapHeight);



                            locmark.setSize(markerWidth,markerHeight);
                            mPosX = mMapWidth/2;
                            mPosY = mMapHeight/2;

                            pathView_ = (PathView) findViewById(R.id.path_view);
                            pathView_.setRatio(mMapWidth / 1714.0, mMapHeight / 3204.0);


                            isinit = false;
                        }else{
                            if(Math.abs(mOrierntationFilter - mOrierntation)>180.0f){
                                if(mOrierntation < 180){
                                    mOrierntation += 360.0f;
                                    mOrierntationFilter = (compassFilterRate*mOrierntation + (1.0f - compassFilterRate)*mOrierntationFilter)%360;
                                }
                                else{
                                    mOrierntationFilter += 360.0f;
                                    mOrierntationFilter = (compassFilterRate*mOrierntation + (1.0f - compassFilterRate)*mOrierntationFilter)%360;
                                }
                            }else{
                                mOrierntationFilter = (compassFilterRate*mOrierntation + (1.0f - compassFilterRate)*mOrierntationFilter)%360;
                            }


                            locmark.setAngle(mOrierntationFilter);

                            float rectAngle = 90.0f*(int)((mOrierntationFilter + 135.0f)/90.0f - 1.0f);


//                            mPosX = mPosX+(int)(Math.sin(Math.toRadians(mOrierntationFilter))*(curStepCount-preStepCount)*STEP_TO_PIXEL);
//                            mPosY = mPosY-(int)(Math.cos(Math.toRadians(mOrierntationFilter))*(curStepCount-preStepCount)*STEP_TO_PIXEL);
//
//
//                            int shifth = (int) (Math.cos(Math.toRadians((mOrierntationFilter+360)%90))*markerHeight + Math.sin(Math.toRadians((mOrierntationFilter+360)%90))*markerWidth)/2;
//                            int shiftw = (int) (Math.sin(Math.toRadians((mOrierntationFilter+360)%90))*markerHeight + Math.cos(Math.toRadians((mOrierntationFilter+360)%90))*markerWidth)/2;

                            mPosX = mPosX+(float)(Math.sin(Math.toRadians(rectAngle))*(curStepCount-preStepCount)*STEP_TO_PIXEL_Width);
                            mPosY = mPosY-(float)(Math.cos(Math.toRadians(rectAngle))*(curStepCount-preStepCount)*STEP_TO_PIXEL_Height);
                            int shifth = (int) (Math.cos(Math.toRadians((mOrierntationFilter+360)%90))*markerHeight + Math.sin(Math.toRadians((mOrierntationFilter+360)%90))*markerWidth)/2;
                            int shiftw = (int) (Math.sin(Math.toRadians((mOrierntationFilter+360)%90))*markerHeight + Math.cos(Math.toRadians((mOrierntationFilter+360)%90))*markerWidth)/2;

                            if(mPosX+shiftw > mMapWidth){
                                mPosX = mMapWidth-shiftw;
                            }else if(mPosX - shiftw < 0){
                                mPosX = shiftw;
                            }

                            if(mPosY+shiftw > mMapHeight){
                                mPosY = mMapHeight - shifth;
                            }else if(mPosY - shifth < 0){
                                mPosY = shifth;
                            }

                            locmark.setPos((int)mPosX-shiftw,(int)mPosY-shifth);


                        }



                    }
                });
            }
        }, 100, 20);

    }
    /**
     * 开启服务
     */
    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }



        @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        //取消服务绑定
        unbindService(conn);
        super.onDestroy();
        mCompassManager.unbind();
    }

    //创建一个类，来响应OnClickListener
    class ButtonClick implements View.OnClickListener
    {
        public void onClick(View v)
        {
            if(str_start == str_end){
                Toast.makeText(MainActivity.this, "您已经到达目标房间", Toast.LENGTH_LONG).show();
            }else {
                loadPath("2", str_start, str_end);
                Toast.makeText(MainActivity.this, "正在规划路径", Toast.LENGTH_LONG).show();

            }

        }


    }

    protected boolean loadPath (String floor, String start_room, String end_room) {
        if (pathView_ != null) {
            PathLoadAsync pathAsync = new PathLoadAsync();
            pathAsync.setView(pathView_,this);
            pathAsync.execute(floor, start_room, end_room);
            return true;
        } else {
            return false;
        }
    }

    public void SetPos(int posX, int posY){
        mPosX = posX;
        mPosY = posY;
    }

    Toast toast = null;

    public void startGetNearestCorner() {
        if (pathView_ == null) {
            return;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { //HERE is the main loop
                        String hint = pathView_.getCurrentHint((int)mPosX, (int)mPosY, 50.0);
                        Log.v("test_hint", hint);
                        if (hint != "") {
                            if (toast == null) {
                                toast = Toast.makeText(MainActivity.this, hint, Toast.LENGTH_LONG);

                            }
                            toast.setText(hint);
                            toast.show();
                        }
                    }
                });
            }
        }, 100, 50);
    }

}

