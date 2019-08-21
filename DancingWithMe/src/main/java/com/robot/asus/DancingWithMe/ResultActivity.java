package com.robot.asus.DancingWithMe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.race604.drawable.wave.WaveDrawable;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class ResultActivity extends RobotActivity {


    private int score;

    private static int iCurrentCommandSerial, iCurrentSpeakSerial;

    private ImageView mImageView, gohome, imageMedal;
    private WaveDrawable mWaveDrawable, mWaveDrawable2;

    private TextView textTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //保持畫面不讓zenbo臉打斷
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //取得分數
        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);

        feedBack(score);
        picWave(score);


        gohome = findViewById(R.id.gohome);
        gohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(ResultActivity.this, ShowYeah.class));

            }
        });


    }

    public void picWave(int score) {
        //圖片波動效果

        mImageView = (ImageView) findViewById(R.id.imageHeart);
        mWaveDrawable = new WaveDrawable(this, R.drawable.heart);
        mImageView.setImageDrawable(mWaveDrawable);
        mWaveDrawable.setLevel(5500);


        imageMedal = (ImageView) findViewById(R.id.imageMedal);

        ShimmerLayout shimmerText = (ShimmerLayout) findViewById(R.id.shimmer_text);


        if (score < 10) {


        } else if (score < 25) {



        } else if (score <40) {

            imageMedal.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.medal));
            shimmerText.startShimmerAnimation();


        } else {

            imageMedal.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.award));
            shimmerText.startShimmerAnimation();
        }



    }

    public void feedBack(int score) {

        if (score < 10) {
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.HAPPY, 25);
            iCurrentSpeakSerial = robotAPI.robot.speak("you should try to dance happier move more steps and do more poses ");

        } else if (score < 25) {
            iCurrentSpeakSerial = robotAPI.robot.speak("You did a good job! but you can try more actions and poses to let zenbo dance");
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.HAPPY, 22);
        } else if (score < 40) {
            iCurrentSpeakSerial = robotAPI.robot.speak("You did a great job! try different poses as much as you can ");
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.HAPPY, 25);
        } else {
            iCurrentSpeakSerial = robotAPI.robot.speak("wonderful! You are a dancing genius!");
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.HAPPY, 20);
        }

    }

    public ResultActivity() {
        super(robotCallback, robotListenCallback);
    }

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);

            if (serial == iCurrentSpeakSerial && state != RobotCmdState.ACTIVE) {

                Log.d("RobotDevSample", "command: " + iCurrentSpeakSerial + " SUCCEED");

                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                robotAPI.cancelCommandBySerial(iCurrentCommandSerial);

            }

        }

        @Override
        public void initComplete() {
            super.initComplete();

        }

        @Override
        public void onDetectFaceResult(List<DetectFaceResult> resultList) {
            super.onDetectFaceResult(resultList);


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
}
