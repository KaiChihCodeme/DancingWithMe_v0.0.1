package com.robot.asus.DancingWithMe;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotCommand;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.VisionConfig;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class MainActivity extends RobotActivity {
    private static DetectPersonXYZ[] answer;
    private static ActionDetecter actionDetecter;
    private static int times, isGetCount, watchOrientation;
    private static int iCurrentCommandSerial;
    final Handler handler = new Handler();
    final Handler handler2 = new Handler();
    private Button stop_btn, start_btn;
    private static boolean isGetFace, isRunningChecker;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef = db.collection("dancing").document("watchState");
    private DocumentReference docRefFollow = db.collection("dancing").document("follow");
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
                Log.d("onStageCheck","Active"+serial+"");
            }
            if (serial == iCurrentCommandSerial && state == RobotCmdState.SUCCEED) {
                startDetectFace();
                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                Log.d("onStageCheck","Succeed"+serial+"");
            }
            if (serial == iCurrentCommandSerial && state == RobotCmdState.PENDING) {
                startDetectFace();
                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                Log.d("onStageCheck","PENDING"+serial+"");
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

        robotAPI.motion.moveHead(0, 70, MotionControl.SpeedLevel.Head.L2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        times = 0;

        stop_btn = (Button) findViewById(R.id.stop);
        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        //robotAPI.robot.speak("Hello world. I am Zenbo. Nice to meet you.");

        startDetectFace();
        isGetFaceChecker();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopDetectFace();
    }

    private static void startDetectFace() {
        // start detect face
        VisionConfig.FaceDetectConfig config = new VisionConfig.FaceDetectConfig();
        config.enableDebugPreview = true;  // set to true if you need preview screen
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
            int temp;

            @Override
            public void run() {
                //檢查動作是否完成

                Log.d("MAAAAAAA", answer[times - 1].getAllString());

                actionDetecter = new ActionDetecter(answer[times - 1]);

                if (temp != actionDetecter.getActionResult()) {
                    switch (actionDetecter.getActionResult()) {

                        case 0:
                            robotAPI.motion.remoteControlBody(MotionControl.Direction.Body.STOP);
                            //robotAPI.utility.playAction(22);
                            Log.d("RobotMotion", "stop");
                            break;

                        case 1:
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

                            Log.d("RobotMotion", "FORWARD");
                            break;
                        case 2:
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
                            Log.d("RobotMotion", "BACKWARD");
                            break;
                        case 3:
                            iCurrentCommandSerial = robotAPI.motion.moveBody(0f, 0.7f, 0f);
                            break;
                        case 4:
                            iCurrentCommandSerial = robotAPI.motion.moveBody(0f, -0.7f, 0f);
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
                    isGetFace = false;
                }


                handler2.postDelayed(this, 10);
            }
        });
    }

    public void getFirebase() {


        //firebase
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.SERIOUS, 22);
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
        docRefFollow.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

}

