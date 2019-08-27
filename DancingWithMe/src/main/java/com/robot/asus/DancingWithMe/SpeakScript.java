package com.robot.asus.DancingWithMe;

public class SpeakScript {
    private String[] startSpeak ={
            "",
            "Put your hands up",
            "Open ur arms!",
            "",
            "Raise ur hands in front of your face and watch ur watch"
    };
    private String[] response  ={
            "",
            "Shake your butt with me",
            "It's SOLO time!",
            "",
            "Turn around with me"};
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
        return response[watchOrientation];
    }

    public String getStartSpeak() {
        return startSpeak[watchOrientation];
    }

}
