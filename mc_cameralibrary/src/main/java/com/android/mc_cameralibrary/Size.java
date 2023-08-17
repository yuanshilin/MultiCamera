package com.android.mc_cameralibrary;

public class Size {
    private int width,height;
    public Size(int w, int h){
        width = w;
        height = h;
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
}
