package com.robot.asus.DancingWithMe;

public class DetectPersonXYZ {
    private int personId;
    private float x;
    private float y;
    private float z;
    private float[] xyzList;


    public DetectPersonXYZ(int personId, float x, float y, float z) {
        this.personId = personId;
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public int getPersonId() {
        return personId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public String getPersonIdstring() {
        String a = String.valueOf(personId);
        return a;
    }

    public String getXstring() {
        String a = String.valueOf(x);
        return a;
    }

    public String getYstring() {
        String a = String.valueOf(y);
        return a;
    }

    public String getZstring() {
        String a = String.valueOf(z);
        return a;
    }

    public String getAllString() {
        String a = String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(z);
        return a;
    }

    public float[] getXyzList() {
        xyzList = new float[10];

        xyzList[1] = z;
        xyzList[2] = z;
        xyzList[3] = x;
        xyzList[4] = x;
        xyzList[5] = y;
        xyzList[6] = y;

        return xyzList;
    }

}
