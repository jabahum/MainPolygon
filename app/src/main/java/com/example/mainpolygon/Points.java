package com.example.mainpolygon;

import android.graphics.Point;

public class Points {

    Double x;
    Double y;





    public Points(Double x, Double y) {
        this.x = x;
        this.y = y;
    }



    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Points{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
