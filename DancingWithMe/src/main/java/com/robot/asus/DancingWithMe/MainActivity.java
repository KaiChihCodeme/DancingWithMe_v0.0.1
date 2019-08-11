package com.robot.asus.DancingWithMe;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.VisionConfig;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.List;

public class MainActivity extends RobotActivity {
    private static DetectPersonXYZ[] answer;
    private static ActionDetecter actionDetecter;
    private static int times, isGetCount;
    private static int d1, d2;
    final Handler handler = new Handler();
    final Handler handler2 = new Handler();
    private Button stop_btn, start_btn;
    private static boolean isGetFace, isRunningChecker;

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
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

        robotAPI.motion.moveHead(0,60,MotionControl.SpeedLevel.Head.L2);
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
        robotAPI.robot.speak("Hello world. I am Zenbo. Nice to meet you.");

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

    private void stopDetectFace() {
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
}
