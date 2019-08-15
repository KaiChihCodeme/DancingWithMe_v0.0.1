package im.fju.watchdancingsensor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    private Button show_button;
    private static final String TAG = "Watch MainActivity";



    //手錶辨識用
    private static TextView textShake, textOrientation;
    private int  direction = 0, lastDirection = 0;
    private float SmallDirectionCheckThreshold = 3f, DirectionCheckThreshold = 6f;
    private Sensor mAccelerometer;
    private static boolean isFollow;
    private static float[] accDataList = new float[3];
    //手錶辨識用

    //Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef = db.collection("dancing").document("watchState");
    private DocumentReference docRefFollow = db.collection("dancing").document("follow");
    private Map<String, Object> watchState = new HashMap<>();
    private final Handler handlerWatch = new Handler();
    //Firebase

    private static final int MY_PERMISSIONS_REQUEST_BODY_SENSORS = 100;
    SensorManager mSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);




        mTextView = (TextView) findViewById(R.id.text);
        show_button = (Button)findViewById(R.id.show_button);
        show_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isFollow) {
                    isFollow = false;
                    docRefFollow.update("isFollow", isFollow);
                }else {
                    isFollow = true;
                    docRefFollow.update("isFollow", isFollow);
                }

            }
        });

        //要求權限
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.BODY_SENSORS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.BODY_SENSORS},
                        MY_PERMISSIONS_REQUEST_BODY_SENSORS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //手錶運動辨識-------------------------------------

        Sensey.getInstance().init(this);
        textOrientation = findViewById(R.id.textOrientation);
        textShake = findViewById(R.id.textShakeHand);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        watchState.put("orientation", -1);
        docRef.set(watchState);
        postFirebase();
        //手錶運動辨識-------------------------------------

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(accelerometer);

        Sensey.getInstance().stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(accelerometer, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Sensey.getInstance().init(this);

    }



    SensorEventListener accelerometer = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {


            accDataList[0] = event.values[0];
            accDataList[1] = event.values[1];
            accDataList[2] = event.values[2];
            SmallCheckDirection();
            CheckDirection();


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    public void CheckDirection() {


        if (accDataList[0] > DirectionCheckThreshold) {
            textOrientation.setText("左傾");
            direction = 1;
        } else if (accDataList[0] < -DirectionCheckThreshold) {
            textOrientation.setText("右傾");
            direction = 3;
        } else if (accDataList[1] < -DirectionCheckThreshold) {
            textOrientation.setText("前傾");
            direction = 2;
        } else if (accDataList[1] > DirectionCheckThreshold) {
            textOrientation.setText("後傾");
            direction = 4;

        }


        if (accDataList[1] > DirectionCheckThreshold && accDataList[0] > DirectionCheckThreshold) {
            textOrientation.setText("左後傾");
            direction = 8;

        } else if (accDataList[1] > DirectionCheckThreshold && accDataList[0] < -DirectionCheckThreshold) {
            textOrientation.setText("右後傾");
            direction = 7;

        } else if (accDataList[1] < -DirectionCheckThreshold && accDataList[0] > DirectionCheckThreshold) {
            textOrientation.setText("左前傾");
            direction = 5;

        } else if (accDataList[1] < -DirectionCheckThreshold && accDataList[0] < -DirectionCheckThreshold) {
            textOrientation.setText("右前傾");
            direction = 6;

        }
    }

    public void SmallCheckDirection() {

        if (accDataList[0] > SmallDirectionCheckThreshold) {
            textOrientation.setText("小左傾");
            direction = 0;
        } else if (accDataList[0] < -SmallDirectionCheckThreshold) {
            textOrientation.setText("小右傾");
            direction = 0;
        } else if (accDataList[1] < -SmallDirectionCheckThreshold) {
            textOrientation.setText("小前傾");
            direction = 0;
        } else if (accDataList[1] > SmallDirectionCheckThreshold) {
            textOrientation.setText("小後傾");
            direction = 0;

        } else {
            textOrientation.setText("平");
            direction = 0;
        }


        if (accDataList[1] > SmallDirectionCheckThreshold && accDataList[0] > SmallDirectionCheckThreshold) {
            textOrientation.setText("小左後傾");
            direction = 0;

        } else if (accDataList[1] > SmallDirectionCheckThreshold && accDataList[0] < -SmallDirectionCheckThreshold) {
            textOrientation.setText("小右後傾");
            direction = 0;

        } else if (accDataList[1] < -SmallDirectionCheckThreshold && accDataList[0] > SmallDirectionCheckThreshold) {
            textOrientation.setText("小左前傾");
            direction = 0;

        } else if (accDataList[1] < -SmallDirectionCheckThreshold && accDataList[0] < -SmallDirectionCheckThreshold) {
            textOrientation.setText("小右前傾");
            direction = 0;

        }
    }

    public void postFirebase() {

        handlerWatch.post(new Runnable() {
            @Override
            public void run() {

                if (direction == lastDirection) {
                    //與之前的方向一樣就不做任何事
                    Log.d("sameN", direction + lastDirection + "");
                } else {

                    lastDirection = direction;
                    watchState.put("orientation", direction);
                    docRef.set(watchState);

                    Log.d("number1", direction + "");

                }

                handlerWatch.postDelayed(this, 1500);
            }
        });
    }



}
