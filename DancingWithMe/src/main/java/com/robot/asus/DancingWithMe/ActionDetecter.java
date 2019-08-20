package com.robot.asus.DancingWithMe;

public class ActionDetecter {
    private DetectPersonXYZ f;
    private DetectPersonXYZ s;
    private int actionNum;
    private float x;
    private float y;
    private float z;


    public ActionDetecter(DetectPersonXYZ f, DetectPersonXYZ s, int actionNum) {
        this.actionNum = actionNum;
        this.f = f;
        this.s = s;
    }

    public ActionDetecter(DetectPersonXYZ s) {
        //this.actionNum = actionNum;
        this.s = s;
    }

    public int getActionResult() {

        x = s.getX();
        y = s.getY();
        z = s.getZ();

        if (x > 1.3) {
            //zenbo往前
            return 1;
        }

        if (x < 1.1) {
            //zenbo往後
            return 2;
        }

        if (y > 0.5) {
            //zenbo往右
            return 3;
        }

        if (y < -0.5) {
            //往左
            return 4;
        }

        return 0;
    }
}
