package com.robot.asus.DancingWithMe;


import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.asus.robotframework.API.ExpressionConfig;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;
import java.util.Locale;

public class ShowYeah extends RobotActivity {


    private ImageView chachaImageView,popImageView,sambaImageView,backImageView;
    private static int iCurrentCommandSerial;

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);

            if (serial == iCurrentCommandSerial && state == RobotCmdState.SUCCEED) {
                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                Log.d("onStageCheck", "Succeed" + serial + "");
            }
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }

        @Override
        public void onDetectFaceResult(List<DetectFaceResult> resultList) {
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

        public ShowYeah() {
            super(robotCallback, robotListenCallback);
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_yeah);

        chachaImageView = (ImageView)findViewById(R.id.chachaImageView);
        popImageView = (ImageView)findViewById(R.id.popImageView);
        sambaImageView = (ImageView)findViewById(R.id.sambaImageView);
        backImageView = (ImageView)findViewById(R.id.backImageView);

        chachaImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.putExtra("songName", R.raw.chacha2);
                intent.setClass(ShowYeah.this, MainActivity.class);
                startActivity(intent);
            }
        });
        popImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("songName", R.raw.chachaaa);
                intent.setClass(ShowYeah.this, MainActivity.class);
                startActivity(intent);
            }
        });
        sambaImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("songName", R.raw.paistropical);
                intent.setClass(ShowYeah.this, MainActivity.class);
                startActivity(intent);
            }
        });
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.robot.asus.MainDancing", "com.robot.asus.MainDancing.MainActivity" ));
                startActivity(intent);
                //android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });

        /*if (Locale.getDefault().getLanguage().equals("en")) {
            robotAPI.robot.setExpression(RobotFace.PLEASED, getResources().getString(R.string.MA_Hello), new ExpressionConfig().speed(85));
            iCurrentCommandSerial = robotAPI.robot.setExpression(RobotFace.ACTIVE, getResources().getString(R.string.MA_intro), new ExpressionConfig().speed(85));
        } else {
            robotAPI.robot.setExpression(RobotFace.PLEASED, getResources().getString(R.string.MA_Hello));
            iCurrentCommandSerial = robotAPI.robot.setExpression(RobotFace.ACTIVE, getResources().getString(R.string.MA_intro));
        }*/

    }

    @Override
    protected void onResume() {
            super.onResume();

        robotAPI.robot.speak(getResources().getString(R.string.MA_which));
    }



    }



