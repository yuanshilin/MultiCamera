package com.android.mc_camera;

import android.hardware.Camera;

public class Size {
    private int width,height;
    public Size(int w, int h){
        width = w;
        height = h;
    }

    public Size(Camera.Size size){
        width = size.width;
        height = size.height;
    }

    public Size(String string){
        String[] strings = string.split("x");
        if(strings.length != 2){
            width = 640;
            height = 480;
        }else{
            width = Integer.parseInt(strings[0]);
            height = Integer.parseInt(strings[1]);
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return width+"x"+height;
    }
}
