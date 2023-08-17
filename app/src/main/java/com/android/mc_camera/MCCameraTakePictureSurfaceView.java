package com.android.mc_camera;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class MCCameraTakePictureSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback, Camera.PictureCallback {
    private final String TAG = getClass().getSimpleName();
    private MCCamera mcCamera;
    private TakePictureCallBack takePictureCallBack;
    private OnCameraStateListener onCameraStateListener;
    private boolean isDebugFps = true;
    private CameraManager cameraManager =null;
    private List<String> previewSizeListString, pictureSizeListString, cameraIdListString;

    //用于计量帧率的
    private long lastTime = 0;
    private int frameNum = 0;
    private int maxFrameNum = 10;


    public MCCameraTakePictureSurfaceView(Context context) {
        super(context);
        mcCamera = new MCCamera();
    }

    public MCCameraTakePictureSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mcCamera = new MCCamera();
    }

    public MCCameraTakePictureSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mcCamera = new MCCamera();
    }

    /**
     * 设置拍照数据回调
     * @param takePictureCallBack 回调参数
     */
    public void setTakePictureCallBack(TakePictureCallBack takePictureCallBack) {
        this.takePictureCallBack = takePictureCallBack;
    }

    /**
     * 设置FPS数据回调
     * @param onCameraStateListener 回调参数
     */
    public void setOnCameraStateListener(OnCameraStateListener onCameraStateListener) {
        this.onCameraStateListener = onCameraStateListener;
    }

    /**
     * 初始化摄像头，主要是打开摄像头
     * @param cameraId 摄像头 ID
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initMcCamera(int cameraId, Size previewSize){
        cameraId = mcCamera.openCamera(cameraId);
        if (cameraId < 0 ){
            Log.e(TAG,"打开摄像头失败");
        }else{
            if (previewSize == null){
                setOrChangePreviewSize(getOneSupportSize());
            }else {
                setOrChangePreviewSize(previewSize);
            }
            getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            getHolder().addCallback(this);
            mcCamera.setPreviewDisplay(getHolder());
            mcCamera.setPreviewCallback(this);
            mcCamera.startPreview();
            if (previewSizeListString == null){
                previewSizeListString = new ArrayList<>();
                cameraSizeListToStringList(previewSizeListString,mcCamera.getSupportPreviewSizes());
            }
            if (pictureSizeListString == null){
                pictureSizeListString = new ArrayList<>();
                cameraSizeListToStringList(pictureSizeListString,mcCamera.getSupportPictureSizes());
            }
            if (cameraIdListString == null){
                cameraIdListString = new ArrayList<>();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
                }
                if (cameraManager != null){
                    try {
                        String[] cameraIdlist = cameraManager.getCameraIdList();
                        Log.d(TAG, "yuanshilin: cameraIdlist.length = "+cameraIdlist.length);
                        for(int i = 0; i < cameraIdlist.length; i++){
                            cameraIdListString.add(String.valueOf(i));
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
//                    for(int i = 0; i < Camera.getNumberOfCameras(); i++){
//                        cameraIdListString.add(String.valueOf(i));
//                    }
                }

            }
        }
    }

    /**
     * 释放摄像头
     */
    public void releaseMcCamera(){
        mcCamera.setPreviewCallback(null);
        mcCamera.releaseCamera();
        previewSizeListString = null;
        pictureSizeListString = null;
        cameraIdListString = null;
    }

    public Size getOneSupportSize(){
        List<String> sizes = getPreviewSizeListString();
        if (sizes == null || sizes.size() == 0){return new Size(640,480);}else{
            return new Size(sizes.get(sizes.size() - 1));
        }
    }

    /**
     * 设置预览分辨率
     * @param previewSize 相片分辨率
     */
    public void setOrChangePreviewSize(Size previewSize){
        if (!mcCamera.setPreviewSize(previewSize)){
            Log.e(TAG,"设置预览分辨率失败");
        }
        mcCamera.setPreviewCallback(this);
    }

    /**
     * 设置拍照相片分辨率
     * @param pictureSize 相片分辨率
     */
    public void setOrChangePictureSize(Size pictureSize){
        if (!mcCamera.setPictureSize(pictureSize)){
            Log.e(TAG,"设置相片分辨率失败");
        }
        mcCamera.setPreviewCallback(this);
    }

    /**
     * 是否日志显示FPS
     * @param debugFps true 日志显示FPS， false 日志不显示FPS
     */
    public void setDebugFps(boolean debugFps) {
        isDebugFps = debugFps;
    }

    /**
     * 拍照
     */
    public void takePicture(){
        mcCamera.takePicture(this);
    }

    /**
     * 显示相机信息
     */
    public void dump(){
        mcCamera.dump();
    }

    /**
     * 获取支持的预览分辨率列表
     * @return 支持的预览分辨率列表
     */
    public List<String> getPreviewSizeListString() {
        return previewSizeListString;
    }

    /**
     * 获取支持的照片分辨率列表
     * @return 支持的照片分辨率列表
     */
    public List<String> getPictureSizeListString() {
        return pictureSizeListString;
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
        Size pictureSize = mcCamera.getPictureSize();

        if (onCameraStateListener != null){
            onCameraStateListener.onCameraSizeListener(previewSize,pictureSize,mcCamera.getCameraId());
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
        if (stringSizes == null || sizes == null){return;}
        for (Camera.Size size: sizes){
            stringSizes.add(size.width+"x"+size.height);
        }
    }



    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        if (takePictureCallBack != null){
            takePictureCallBack.pictureCallBack(bytes);
        }
        mcCamera.startPreview();
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
                if (onCameraStateListener != null){
                    onCameraStateListener.onFpsListener(fps);
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
        releaseMcCamera();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    public interface TakePictureCallBack{
        public void pictureCallBack(byte[] data);
    }

    public interface OnCameraStateListener{
        public void onFpsListener(int fps);
        public void onCameraSizeListener(Size previewSize, Size pictureSize, int cameraId);
    }
}
