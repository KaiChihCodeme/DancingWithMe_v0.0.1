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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.sql.Ref;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private TextView mTextView;
    private Button show_button;
    private static final String TAG = "Watch MainActivity";


    //手錶辨識用
    private static TextView textShake, textOrientation;
    private int direction = 0, lastDirection = 0;
    private float SmallDirectionCheckThreshold = 3f, DirectionCheckThreshold = 6f;
    private Sensor mAccelerometer;
    private static boolean isFollow;
    private static float[] accDataList = new float[3];
    //手錶辨識用

    //Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef = db.collection("dancing").document("watchState");
    private DocumentReference docRefFollow = db.collection("dancing").document("follow");
    private DocumentReference docRefHealth = db.collection("dancing").document("health");
    private Map<String, Object> watchState = new HashMap<>();
    private Map<String, Object> healthData = new HashMap<>();
    private final Handler handlerWatch = new Handler();
    //Firebase

    private static final int MY_PERMISSIONS_REQUEST_BODY_SENSORS = 100;
    SensorManager mSensorManager;

    /*final Handler handler_start = new Handler();
    final Handler handler_end = new Handler();*/
    final Handler handler = new Handler();
    Sensor mHeartRateSensor;

    ArrayList<String> data_heart_rate = new ArrayList<>();
    ArrayList<String> data_time_that = new ArrayList<>();

    long time_start, time_stop;
    double total_second = 0;
    long time_now;

    private static boolean isStarted, isEnded;
    private static String Id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextView = (TextView) findViewById(R.id.heart_rate_bpm);
        show_button = (Button) findViewById(R.id.show_button);
        show_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isFollow) {
                    isFollow = false;
                    docRefFollow.update("isFollow", isFollow);
                } else {
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


        //在背景0.5秒抓一次是不是true
        /*handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 500);
                Log.d(TAG, "postDelayed");
                getCheckStart();
            }
        });*/

        //註冊心跳偵測器
        mHeartRateSensor = Objects.requireNonNull(mSensorManager).getDefaultSensor(Sensor.TYPE_HEART_RATE);
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

        /**抓是否開始dance**/
        getCheckStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //handler.removeCallbacksAndMessages(null);
    }

    //按出去後回來繼續handler
    @Override
    protected void onRestart() {
        super.onRestart();
        //在背景0.5秒抓一次是不是true
        /*handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 500);
                Log.d(TAG, "postDelayed");
                getCheckStart();
            }
        });*/
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

        } else if (accDataList[2] < -6f){
            textOrientation.setText("翻轉");
            direction = 10;
        }
        else {
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

    private void startMeasure() {
        data_heart_rate.clear();
        data_time_that.clear();
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        time_start = System.currentTimeMillis();
        //Log.d("Sensor Status:", " Sensor registered: " + (mSe ? "yes" : "no"));
        Log.d(TAG, "start measure");
        show_button.setText("量測中...");
        //開始之後恢復在背景0.5秒抓一次是不是運動結束了(true)
        //doPostEnd();
        /*handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 500);
                Log.d(TAG, "postDelayed");
                doCheckEnd();
            }
        });*/
    }


    private void stopMeasure() {
        time_stop = System.currentTimeMillis();
        total_second = (double) (time_stop - time_start) / 1000.0;
        mSensorManager.unregisterListener(this);
        //把boolean改成false
        //doPostEnd();
        show_button.setText("量測完畢。");
        Log.e("時間:", Arrays.toString(data_time_that.toArray()));
        Log.e("心跳:", Arrays.toString(data_heart_rate.toArray()));
        //watch_data.add(data_heart_rate);
        //watch_data.add(data_time_that);
        //Collections.sort(data_time_that);
        /*if (data_heart_rate.size() != 0 || data_time_that.size() != 0) {
            //計算卡路里
            calculateCalories();
            //上傳心跳那些資料上去資料庫
            doInvokeAPI();
        }*/

        //上傳健康資料
        uploadData();

        //停止之後恢復在背景0.5秒抓一次是不是開始運動(true)
        /*handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 500);
                Log.d(TAG, "postDelayed");
                getCheckStart();
            }
        });*/
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        time_now = System.currentTimeMillis();
        double record_time = (double) (time_now - time_start) / 1000.0;
        double heartrate = event.values[0];
        DecimalFormat df = new DecimalFormat("##.00");
        heartrate = Double.parseDouble(df.format(heartrate));
        String mHeartRate = String.format(Locale.getDefault(), "%3.2f", heartrate);

        mTextView.setText(mHeartRate);

        record_time = Double.parseDouble(df.format(record_time));
        String mrecord_time = String.format(Locale.getDefault(), "%3.2f", record_time);
        data_heart_rate.add(mHeartRate);
        data_time_that.add(mrecord_time);
        Log.e(TAG, "HR: " + mHeartRate + "TIME:" + record_time);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "Accuracy: " + i);
    }

    private void getCheckStart() {
        docRefHealth.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, source + " data: " + snapshot.getData());
                    isStarted = Boolean.parseBoolean(snapshot.get("isStarted").toString());
                    isEnded = Boolean.parseBoolean(snapshot.get("isEnded").toString());
                    if (isStarted) {
                        //代表開始dance
                        startMeasure();
                        postFalseStart();
                    } else if (isEnded) {
                        stopMeasure();
                        postFalseEnd();
                    }
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });
    }

    //post false to isStarted in order to initial
    private void postFalseStart() {
        docRefHealth
                .update("isStarted", false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "isStared false successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating false to isStarted", e);
                    }
                });
    }

    //post false to isEnded in order to initial
    private void postFalseEnd() {
        docRefHealth
                .update("isEnded", false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "isEnded false successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating false to isEnded", e);
                    }
                });
    }

    private void uploadData() {
        double total_HR = 0;

        //避免完全沒偵測到心跳，如果沒偵測到就噴錯誤訊息到LOG，並且不做事
        try {
            if (data_heart_rate.size() != 0) {
                for (int j = 0; j < data_heart_rate.size(); j++) {
                    total_HR += Double.parseDouble(data_heart_rate.get(j));
                }

                double average_HR = total_HR / data_heart_rate.size();
                DecimalFormat df_for_HR = new DecimalFormat("##.00000");
                average_HR = Double.parseDouble(df_for_HR.format(average_HR));

                String stop_time = String.valueOf(time_stop);
                Id = stop_time.substring(0, 10);

                healthData.put("HR", data_heart_rate);
                healthData.put("average_HR", average_HR);
                healthData.put("time_moment", data_time_that);
                healthData.put("total_time", total_second);

                db.collection("dancing").document("health").collection(Id).document(Id).set(healthData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                //上傳最新資料
                                docRefHealth.update("id", Id);
                                docRefHealth.update("isNew", true)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error updating document", e);
                                            }
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });
            } else {
                Log.d(TAG, "沒心跳不做事");
            }
        } catch (NumberFormatException ex) {
            Log.e(TAG, ex.toString());
        }
    }

}
