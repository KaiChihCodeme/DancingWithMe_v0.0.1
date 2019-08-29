package com.robot.asus.DancingWithMe;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.asus.robotframework.API.ExpressionConfig;
import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotCommand;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.SpeakConfig;
import com.asus.robotframework.API.VisionConfig;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends RobotActivity {
    private static DetectPersonXYZ[] answer;
    private static ActionDetecter actionDetecter;
    private static int times, isGetCount, watchOrientation;
    private static int iCurrentCommandSerial, iFirstSpeaking;
    final Handler handler = new Handler();
    final Handler handler2 = new Handler();
    final Handler handlerForTips = new Handler();
    static final Handler handlerTime = new Handler();
    private Button stop_btn, start_btn, endButton;
    private static boolean isGetFace, isRunningChecker;
    private MediaPlayer music_cha = new MediaPlayer();
    final Handler handler_for_timer = new Handler();
    private int count_for_timer;
    private static boolean isMoving;
    private static int temp, TotalTime, score;
    private int songName;
    private int draw_num;
    private static float volume=1;
    int count_tips = 0;
    SpeakScript speakScript;


    private ListenerRegistration registration;
    private ListenerRegistration registration2;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef = db.collection("dancing").document("watchState");
    private DocumentReference docRefFollow = db.collection("dancing").document("follow");
    private DocumentReference docRefHealth = db.collection("dancing").document("health");
    Map<String, Object> FirebaseData;


    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);

            if (serial == iCurrentCommandSerial && state == RobotCmdState.ACTIVE) {
                stopDetectFace();
                //robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                Log.d("onStageCheck", "Active" + serial + "");
                Log.d("onStageCheck", "Active" + serial + "");
            }
            if (serial == iCurrentCommandSerial && state == RobotCmdState.SUCCEED) {
                startDetectFace();
                robotAPI.robot.setExpression(RobotFace.EXPECTING);
                /*if (!isMoving) {
                    robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                } else {
                    isMoving = false;
                }*/
                Log.d("onStageCheck", "Succeed" + serial + "");
                Log.d("onStageCheck", "Succeed" + serial + "");
            }
            if (serial == iCurrentCommandSerial && state == RobotCmdState.PENDING) {
                startDetectFace();
                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                Log.d("onStageCheck", "PENDING" + serial + "");
            }
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }

        @Override
        public void onDetectFaceResult(List<DetectFaceResult> resultList) {
            super.onDetectFaceResult(resultList);

            if (times == 99999) {
                times = 0;
            }

            answer[times] = new DetectPersonXYZ(resultList.get(0).getTrackID(), resultList.get(0).getFaceLoc().x,
                    resultList.get(0).getFaceLoc().y, resultList.get(0).getFaceLoc().z);

            isGetFace = true;
            isGetCount = 0;

            times++;
        }

    };

    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        songName = intent.getIntExtra("songName", R.raw.chachaaa);

        iCurrentCommandSerial = robotAPI.robot.setExpression(RobotFace.HAPPY, getResources().getString(R.string.MA_lets), new ExpressionConfig().speed(90));


        //計時器
        totalTime();

        robotAPI.motion.moveHead(0, 40, MotionControl.SpeedLevel.Head.L2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        times = 0;
        temp = -1;
        score = 0;
        TotalTime = 0;

        stop_btn = (Button) findViewById(R.id.stop);
        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("testMusucFad","123");

                robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.STOP);
                stopDetectFace();

            }
        });
        start_btn = (Button) findViewById(R.id.start);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.STOP);
                startDetectFace();
            }
        });
        endButton = (Button) findViewById(R.id.endButton);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                Log.d("scoreScore", score + "");
                postEnd();
                Intent intent = new Intent();
                intent.putExtra("score", score);
                intent.setClass(MainActivity.this, ResultActivity.class);
                startActivity(intent);

            }
        });

        getFirebase();
        getFirebaseFollow();

        //999跟0的還沒解決!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!有十萬分之一的機率會錯誤
        answer = new DetectPersonXYZ[1000000];
        //999跟0的還沒解決!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!有萬分之一的機率會錯誤
    }

    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();


        startDetectFace();
        isGetFaceChecker();
        music_cha = MediaPlayer.create(this, songName);
        music_cha.start();

        postStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacksAndMessages(null);
        handlerTime.removeCallbacksAndMessages(null);
        handler2.removeCallbacksAndMessages(null);
        handler_for_timer.removeCallbacksAndMessages(null);

        stopDetectFace();
        robotAPI.robot.stopSpeak();
        music_cha.stop();


    }

    @Override
    protected void onStop() {
        super.onStop();

        registration.remove();
        registration2.remove();

    }


    private static void startDetectFace() {
        // start detect face
        VisionConfig.FaceDetectConfig config = new VisionConfig.FaceDetectConfig();
        config.enableDebugPreview = false;  // set to true if you need preview screen
        config.intervalInMS = 100;
        config.enableDetectHead = true;
        config.enableFacePosture = true;
        config.enableHeadGazeClassifier = true;
        robotAPI.vision.requestDetectFace(config);
    }

    private static void stopDetectFace() {
        // stop detect person
        robotAPI.vision.cancelDetectFace();

    }

    public void actionChecker() {
        Log.d("actionChecker", "hi");

        handler.post(new Runnable() {


            @Override
            public void run() {
                //檢查動作是否完成

                Log.d("MAAAAAAA", answer[times - 1].getAllString());

                actionDetecter = new ActionDetecter(answer[times - 1]);

                if (temp != actionDetecter.getActionResult()) {
                    switch (actionDetecter.getActionResult()) {

                        case 0:
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.STOP);
                            //  robotAPI.robot.setExpression(RobotFace.SINGING);
                            Log.d("RobotMotion", "stop");
                            break;

                        case 1:
                            robotAPI.robot.setExpression(RobotFace.EXPECTING);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.STOP);
                            robotAPI.motion.moveHead(0, 30
                                    , MotionControl.SpeedLevel.Head.L2);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.FORWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.FORWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.FORWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.FORWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.FORWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.FORWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.FORWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.FORWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.FORWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.FORWARD);

                            score += 2;

                            Log.d("RobotMotion", "FORWARD");
                            break;
                        case 2:
                            robotAPI.robot.setExpression(RobotFace.SINGING);
                            robotAPI.motion.moveHead(0, 45
                                    , MotionControl.SpeedLevel.Head.L2);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.STOP);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.BACKWARD);
                            score += 2;
                            Log.d("RobotMotion", "BACKWARD");
                            break;
                        case 3:
                            //isMoving = true;
                            robotAPI.robot.setExpression(RobotFace.HAPPY);
                            iCurrentCommandSerial = robotAPI.motion.moveBody(0f, 0.7f, 0f);
                            score += 4;
                            break;
                        case 4:
                            //isMoving = true;
                            robotAPI.robot.setExpression(RobotFace.CONFIDENT);
                            iCurrentCommandSerial = robotAPI.motion.moveBody(0f, -0.7f, 0f);
                            score += 4;
                            break;
                    }
                }
                temp = actionDetecter.getActionResult();
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void isGetFaceChecker() {
         handler2.post(new Runnable() {
            @Override
            public void run() {

                isGetCount++;


                Log.d("isRunningChecker", Boolean.toString(isRunningChecker));
                if (isGetFace) {
                    if (!isRunningChecker) {
                        isRunningChecker = true;
                        actionChecker();
                    }
                } else {

                    handler.removeCallbacksAndMessages(null);
                    isRunningChecker = false;
                }
                if (isGetCount == 100) {

                    robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.STOP);
                    robotAPI.robot.setExpression(RobotFace.SINGING);
                    Log.d("RobotMotion", "stop");
                    temp = 0;
                    isGetFace = false;
                }


                handler2.postDelayed(this, 10);
            }
        });
    }

    public void getFirebase() {


        //firebase
        registration2 =  docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    FirebaseData = snapshot.getData();
                    watchOrientation = ((Number) FirebaseData.get("orientation")).intValue();

                    switch (watchOrientation) {

                        case 1:
                            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.INTERESTED, 22);
                            score += 10;
                            break;
                        case 2:
                            robotAPI.utility.playEmotionalAction(RobotFace.EXPECTING, 15);
                            stopDetectFace();
                            count_for_timer = -1;
                            score += 10;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("cccc", Integer.toString(count_for_timer));
                                    //obotAPI.cancelCommandBySerial(iCurrentCommandSerial);
                                    robotAPI.cancelCommandAll();
                                    robotAPI.robot.setExpression(RobotFace.PLEASED);
                                    startDetectFace();
                                }
                            }, 7000);
                        case 10:
                            //後傾
                            score += 10;
                            robotAPI.utility.playEmotionalAction(RobotFace.HAPPY, 27);
                            stopDetectFace();
                            count_for_timer = -1;

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("cccc", Integer.toString(count_for_timer));
                                    //obotAPI.cancelCommandBySerial(iCurrentCommandSerial);
                                    robotAPI.cancelCommandAll();
                                    robotAPI.robot.setExpression(RobotFace.SHY);
                                    startDetectFace();
                                }
                            }, 7000);

                            /*
                            handler_for_timer.post(new Runnable() {
                                @Override
                                public void run() {
                                    count_for_timer += 1;
                                    Log.d("cccc", Integer.toString(count_for_timer));
                                    if (count_for_timer == 15) {
                                        handler_for_timer.removeCallbacks(this);
                                        robotAPI.cancelCommandBySerial(iCurrentCommandSerial);
                                        robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                                        startDetectFace();
                                    }
                                    handler_for_timer.postDelayed(this, 1000);
                                }
                            });*/
                            break;
                    }

                    Log.d("DataCheck", watchOrientation + "");

                } else {
                    Log.d("TAG", source + " data: null");
                }
            }
        });
        //firebase
    }

    public void getFirebaseFollow() {


        //firebase
        registration = docRefFollow.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    FirebaseData = snapshot.getData();
                    Boolean isFollow = (Boolean) FirebaseData.get("isFollow");

                    if (isFollow) {
                        robotAPI.utility.followUser();
                        stopDetectFace();
                    } else {
                        robotAPI.cancelCommand(RobotCommand.CANCEL);
                        iCurrentCommandSerial = robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.STOP);
                        robotAPI.motion.moveHead(0, 60, MotionControl.SpeedLevel.Head.L2);

                    }


                } else {
                    Log.d("TAG", source + " data: null");
                }
            }
        });
        //firebase
    }


    public void totalTime() {
        draw_num = lotteryNum(25, 40);

        handlerTime.post(new Runnable() {
            @Override
            public void run() {

                TotalTime++;

                if (TotalTime == draw_num) {
                    tips();

                }

                if (TotalTime == 90) {
                    startFadeOut();
                    robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                    Log.d("scoreScore", score + "");
                    postEnd();

                    handler.removeCallbacksAndMessages(null);
                    handler2.removeCallbacksAndMessages(null);
                    Intent intent = new Intent();
                    intent.putExtra("score", score);
                    registration.remove();
                    registration2.remove();
                    intent.setClass(MainActivity.this, ResultActivity.class);
                    startActivity(intent);


                }

                handlerTime.postDelayed(this, 1000);
            }
        });
    }

    private void postStart() {
        docRefHealth.update("isStarted", true);
    }

    private void postEnd() {
        docRefHealth.update("isEnded", true);
    }

    private int lotteryNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    private void tips() {
        speakScript = new SpeakScript();
        robotAPI.robot.speak(speakScript.getStartSpeak());
        Log.d("testYifan", speakScript.getResponse() + "\n" + speakScript.getWatchOrientation());

        handlerForTips.post(new Runnable() {
            @Override
            public void run() {
                count_tips++;
                if (count_tips > 15) {
                    Log.d("testYifan", 1 + "");
                    if (!(TotalTime >= 80)) {
                        count_tips = 0;
                        draw_num = lotteryNum(TotalTime + 5, TotalTime + 20);
                    }
                } else if (watchOrientation == speakScript.getWatchOrientation()) {
                    robotAPI.robot.speak(speakScript.getResponse(), new SpeakConfig().volume(100));
                    if (!(TotalTime >= 80)) {
                        count_tips = 0;
                        Log.d("testYifan", "enter");
                        draw_num = lotteryNum(TotalTime + 5, TotalTime + 20);
                    }
                }  else if (watchOrientation == speakScript.getWatchOrientation() + 6) {
                    robotAPI.robot.speak(speakScript.getResponse(), new SpeakConfig().volume(100));
                    if (!(TotalTime >= 80)) {
                        count_tips = 0;
                        Log.d("testYifan", "enter");
                        draw_num = lotteryNum(TotalTime + 5, TotalTime + 20);
                    }
                }else {
                    Log.d("testYifan", "fuck");
                    handlerForTips.postDelayed(this, 1000);
                }
            }
        });
    }


    private void startFadeOut(){

        // The duration of the fade
        final int FADE_DURATION = 3000;

        // The amount of time between volume changes. The smaller this is, the smoother the fade
        final int FADE_INTERVAL = 250;

        // Calculate the number of fade steps
        int numberOfSteps = FADE_DURATION / FADE_INTERVAL;

        // Calculate by how much the volume changes each step
        final float deltaVolume = volume / numberOfSteps;

        // Create a new Timer and Timer task to run the fading outside the main UI thread
        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                //Do a fade step
                fadeOutStep(deltaVolume);

                //Cancel and Purge the Timer if the desired volume has been reached
                if(volume <= 0){
                    timer.cancel();
                    timer.purge();
                    stopPlayer();
                }
            }
        };

        timer.schedule(timerTask,FADE_INTERVAL,FADE_INTERVAL);
    }


    private void fadeOutStep(float deltaVolume){
        music_cha.setVolume(volume, volume);
        Log.d("testMusucFad",volume+""+deltaVolume);
        volume -= deltaVolume;
    }

    // Release the player from memory
    private void stopPlayer() {

        if (music_cha != null) {

            music_cha.release();
           music_cha= null;
        }
    }




}

