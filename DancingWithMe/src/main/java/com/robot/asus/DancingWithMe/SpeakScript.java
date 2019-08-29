package com.robot.asus.DancingWithMe;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import java.util.Locale;

import io.grpc.Contexts;

public class SpeakScript {

    private String[] startSpeak ={
            "",
            "Put your hands up!",
            "Open your arms!",
            "",
            "Open your arms with palms upward!"
    };
    private String[] zh_startSpeak ={
            "",
            "舉高你的雙手!",
            "張開你的雙手就像要擁抱我!",
            "",
            "張開你的雙手，並且手掌朝上!"
    };
    private String[] response  ={
            "",
            "Great! Shake your butt with me!",
            "Yeah! It's our SOLO time!",
            "",
            "Good job! Turn around with me!"};
    private String[] zh_response  ={
            "",
            "太棒了!跟我一起搖屁股!",
            "跳得好!一起開心跳舞吧!",
            "",
            "很棒!跟著我一起旋轉吧!"};
    private int watchOrientation;

    public SpeakScript() {
        int tempOrientation = (int)(Math.random()*4 + 1);
        while (tempOrientation == 3) {
            tempOrientation = (int)(Math.random()*4 + 1);
        }
        this.watchOrientation = tempOrientation;
    }

    public int getWatchOrientation() {
        return watchOrientation;
    }

    public String getResponse() {
        if (Locale.getDefault().getLanguage().equals("en")) {
            return response[watchOrientation];
        } else {
            return zh_response[watchOrientation];
        }
    }

    public String getStartSpeak() {
        if (Locale.getDefault().getLanguage().equals("en")) {
            return startSpeak[watchOrientation];
        } else {
            return zh_startSpeak[watchOrientation];
        }
    }

}
