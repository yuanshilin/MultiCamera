package com.android.mc_cameralibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.List;

public class MCCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback, Camera.PictureCallback {
    private final String TAG = getClass().getSimpleName();
    private MCCamera mcCamera;
    private int cameraId = -1;



    public MCCameraSurfaceView(Context context) {
        super(context);
        mcCamera = new MCCamera();
    }

    public MCCameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mcCamera = new MCCamera();
    }

    public MCCameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mcCamera = new MCCamera();
    }


    public void initMcCamera(int cameraId){
        cameraId = mcCamera.openCamera(cameraId);
        if (cameraId < 0 ){
            Log.e(TAG,"打开摄像头失败");
        }else{
            mcCamera.setPreviewCallback(this);
        }
    }

    public void releaseMcCamera(){
        mcCamera.setPreviewCallback(null);
        mcCamera.releaseCamera();
    }

    public void setOrChangePreviewSize(Camera.Size previewSize){
        if (mcCamera.setPreviewSize(previewSize)){
            Log.d(TAG,"设置预览分辨率成功");
        }
    }

    public void takePicture(){
        mcCamera.takePicture(this);
    }



    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
