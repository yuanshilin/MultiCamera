package com.android.mc_camera;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MCCameraRecordSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private final String TAG = getClass().getSimpleName();


    private MediaRecorder mediaRecorder;

    private MCCamera mcCamera;
    private boolean isDebugFps = true;
    private boolean isRecording = false;
    private List<String> previewSizeListString,videoSizeListString, cameraIdListString;
    private Size recordSize;

    //用于计量帧率的
    private long lastTime = 0;
    private int frameNum = 0;
    private int maxFrameNum = 10;

    private OnCameraRecordFpsListener onCameraRecordFpsListener;
    private OnCameraRecordSizeListener onCameraRecordSizeListener;
    private OnRecordError onRecordError;



    public MCCameraRecordSurfaceView(Context context) {
        super(context);
        mcCamera = new MCCamera();
        initMcCamera(0,null);
    }

    public MCCameraRecordSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mcCamera = new MCCamera();
        initMcCamera(0,null);
    }

    public MCCameraRecordSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mcCamera = new MCCamera();
        initMcCamera(0,null);
    }

    public void setOnCameraRecordFpsListener(OnCameraRecordFpsListener onCameraRecordFpsListener) {
        this.onCameraRecordFpsListener = onCameraRecordFpsListener;
    }

    public void setOnCameraRecordSizeListener(OnCameraRecordSizeListener onCameraRecordSizeListener) {
        this.onCameraRecordSizeListener = onCameraRecordSizeListener;
    }

    public void setOnRecordError(OnRecordError onRecordError) {
        this.onRecordError = onRecordError;
    }

    /**
     * 初始化摄像头，主要是打开摄像头
     * @param cameraId 摄像头 ID
     */
    public void initMcCamera(int cameraId,Size previewSize){
        cameraId = mcCamera.openCamera(cameraId);
        if (cameraId < 0 ){
            Log.e(TAG,"打开摄像头失败");
        }else{
            recordSize = previewSize;
            getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            getHolder().addCallback(this);
            mcCamera.setPreviewDisplay(getHolder());
            mcCamera.setPreviewCallback(this);
            mcCamera.setPreviewSize(getOneSupportSize());
            mcCamera.startPreview();
            if (previewSizeListString == null){
                previewSizeListString = new ArrayList<>();
                cameraSizeListToStringList(previewSizeListString,mcCamera.getSupportPreviewSizes());
            }
            if (videoSizeListString == null){
                videoSizeListString = new ArrayList<>();
                cameraSizeListToStringList(videoSizeListString,mcCamera.getSupportVideoSizes());
            }
            if (cameraIdListString == null){
                cameraIdListString = new ArrayList<>();
                for(int i = 0; i < Camera.getNumberOfCameras(); i++){
                    cameraIdListString.add(String.valueOf(i));
                }
            }
        }
    }

    /**
     * 开始录像
     */
    public void startRecord(){
        if (mcCamera.getCamera() == null){
            Log.e(TAG,"摄像头未打开");
        }
        if (mediaRecorder == null){
            mediaRecorder = new MediaRecorder();
        }
        if (recordSize == null){
            recordSize = getOneSupportVideoSize();
        }
        mcCamera.getCamera().unlock();
        mediaRecorder.setCamera(mcCamera.getCamera());
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        Log.e(TAG,"设置录像分辨率为："+recordSize.toString());
        mediaRecorder.setVideoSize(recordSize.getWidth(),recordSize.getHeight());
        mediaRecorder.setVideoFrameRate(25);
        mediaRecorder.setPreviewDisplay(this.getHolder().getSurface());
        mediaRecorder.setOutputFile("/sdcard/b.mp4");


        boolean isError = false;
        String errorMessage = null;
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
            errorMessage = e.getMessage();
            isError = true;
        } catch (RuntimeException runtimeException){
            Log.e(TAG,runtimeException.getMessage());
            errorMessage = runtimeException.getMessage();
            isError = true;
        }finally {
            if (isError){
                isRecording = false;
                mediaRecorder = null;
                releaseMcCamera();
                initMcCamera(0,null);
                if (onRecordError != null){
                    onRecordError.recordError(errorMessage);
                }
            }
        }
    }

    /**
     * 是否正在录像
     * @return true 正在录像，false没有录像
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * 设置录像分辨率
     */
    public void setRecordSize(Size recordSize) {
        this.recordSize = recordSize;
    }

    /**
     * 停止录像
     */
    public void stopRecord(){
        if (mediaRecorder == null){
            isRecording = false;
            return;
        }
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
    }


    /**
     * 释放摄像头
     */
    public void releaseMcCamera(){
        if (isRecording){
            stopRecord();
        }
        mcCamera.setPreviewCallback(null);
        mcCamera.releaseCamera();
        previewSizeListString = null;
        videoSizeListString = null;
        cameraIdListString = null;
    }

    /**
     * 获取一个支持的分辨率
     * @return
     */
    public Size getOneSupportSize(){
        List<String> sizes = getPreviewSizeListString();
        if (sizes == null || sizes.size() == 0){return new Size(640,480);}else{
            return new Size(sizes.get(sizes.size() - 1));
        }
    }

    public Size getOneSupportVideoSize(){
        List<String> sizes = getVideoSizeListString();
        if (sizes == null || sizes.size() == 0){return new Size(640,480);}else{
            return new Size(sizes.get(sizes.size() - 1));
        }
    }

    /**
     * 获取支持的预览分辨率列表
     * @return 支持的预览分辨率列表
     */
    public List<String> getPreviewSizeListString() {
        return previewSizeListString;
    }

    /**
     * 获取支持的录像分辨率列表
     * @return 录像分辨率列表
     */
    public List<String> getVideoSizeListString() {
        return videoSizeListString;
    }

    /**
     * 是否日志显示FPS
     * @param debugFps true 日志显示FPS， false 日志不显示FPS
     */
    public void setDebugFps(boolean debugFps) {
        isDebugFps = debugFps;
    }


    /**
     * 获取支持的摄像头ID
     * @return 支持的摄像头ID
     */
    public List<String> getCameraIdListString() {
        return cameraIdListString;
    }

    /**
     * 获取相机的预览和拍照分辨率
     */
    public void getCameraSizeState(){
        Size previewSize = mcCamera.getPreviewSize();

        if (onCameraRecordSizeListener != null){
            onCameraRecordSizeListener.cameraRecordSize(previewSize,mcCamera.getCameraId());
        }
    }

    /**
     * 设置闪光灯模式
     * @param mode 闪光灯模式
     */
    public void setFlashMode(String mode){
        mcCamera.setFlashMode(mode);
    }

    private void cameraSizeListToStringList(List<String> stringSizes, List<Camera.Size> sizes){
        if (stringSizes == null || sizes == null){
            if (stringSizes == null){
                Log.e(TAG,"存放分辨率列表的容器为空");
            }
            if (sizes == null){
                Log.e(TAG,"从相机获取分辨率列表失败");
            }
            return;
        }
        for (Camera.Size size: sizes){
            stringSizes.add(size.width+"x"+size.height);
        }
    }

    /**
     * 显示相机信息
     */
    public void dump(){
        mcCamera.dump();
    }


    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        frameNum += 1;
        if (frameNum == maxFrameNum){
            frameNum = 0;
            int fps = 0;
            long nowTime = System.currentTimeMillis();
            fps = (int)(((float)maxFrameNum / (float)(nowTime - lastTime)) * 1000);
            lastTime = nowTime;
            if (isDebugFps){
                Log.w(TAG,"相机FPS："+fps);
                if (onCameraRecordFpsListener != null){
                    onCameraRecordFpsListener.onFpsListener(fps);
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mcCamera.setPreviewDisplay(surfaceHolder);
        mcCamera.startPreview();
        getCameraSizeState(); //初次显示分辨率
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopRecord();
        releaseMcCamera();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public interface OnCameraRecordFpsListener{
        public void onFpsListener(int fps);
    }

    public interface OnCameraRecordSizeListener{
        public void cameraRecordSize(Size previewSize,int cameraId);
    }

    public interface OnRecordError{
        public void recordError(String errorMessage);
    }
}
