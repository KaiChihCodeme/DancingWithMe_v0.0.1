package com.robot.asus.DancingWithMe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.race604.drawable.wave.WaveDrawable;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class ResultActivity extends RobotActivity {


    private int score;
    private TextView tvAv_HR;
    private String Av_HR;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef = db.collection("dancing").document("health");

    private String TAG = "ResultActivity";
    private static boolean isNew;

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

        tvAv_HR = findViewById(R.id.average_hr);

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
                System.exit(0);

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
            imageMedal.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.lotus));


        } else if (score < 25) {

            imageMedal.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.lotus));


        } else if (score <40) {

            imageMedal.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.medal));
            shimmerText.startShimmerAnimation();


        } else {

            imageMedal.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.award));
            shimmerText.startShimmerAnimation();
        }



    }

    public void feedBack(int score) {

        getAvHR();
        if (score < 10) {
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.HAPPY, 25);
            iCurrentSpeakSerial = robotAPI.robot.speak(getResources().getString(R.string.RA_lessTen));

        } else if (score < 25) {
            iCurrentSpeakSerial = robotAPI.robot.speak(getResources().getString(R.string.RA_lessTwentyF));
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.HAPPY, 22);
        } else if (score < 40) {
            iCurrentSpeakSerial = robotAPI.robot.speak(getResources().getString(R.string.RA_lessForty));
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.HAPPY, 25);
        } else {
            iCurrentSpeakSerial = robotAPI.robot.speak(getResources().getString(R.string.RA_great));
            iCurrentCommandSerial = robotAPI.utility.playEmotionalAction(RobotFace.HAPPY, 20);
        }

    }

    private void getAvHR() {
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                    isNew = Boolean.parseBoolean(snapshot.get("isNew").toString());
                    if (isNew) {
                        String id = snapshot.get("id").toString();
                        final DocumentReference docRefId = db.collection("dancing").document("health").collection(id).document(id);
                        docRefId.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                        float HR = Math.round(((Number)document.get("average_HR")).intValue());
                                        Av_HR = Float.toString(HR);
                                        tvAv_HR.setText(getResources().getString(R.string.RA_HR) + Av_HR);
                                        //將isNew歸零和id清空
                                        initIndex();
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });
                    }
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });
    }

    private void initIndex() {
        docRef.update("isNew", false);
        docRef.update("id", "emp");
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
