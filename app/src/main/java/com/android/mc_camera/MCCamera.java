package com.android.mc_camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.awt.font.TextAttribute;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author MC
 * @version 1.0
 *
 */
public class MCCamera {
    private final String TAG = getClass().getSimpleName();

    private Camera camera;
    private int cameraId = -2; //未调用initCamera

    private boolean isPreview = false; //是否在预览
    private boolean isRelease = true;  //相机是否在使用（打开）

    public MCCamera(){
        Log.w(TAG,"MCCamera");
    }


    private int __initCamera(int cameraId){
        if (cameraId < 0) return -1; //已经open过了，但是没有成功
        if (camera == null){
            try {
                camera = Camera.open(cameraId);
            }catch (Exception e){
                Log.e(TAG,e.getMessage());
                camera = null;
                isRelease = true;
                return -1; //已经open过了，但是没有成功
            }
        }
        isRelease = false;
        return cameraId;
    }

    private boolean __setPreviewDisplay(SurfaceHolder surfaceHolder){
        if (camera != null){
            try {
                camera.setPreviewDisplay(surfaceHolder);
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
                return false;
            }
        }
        return true;
    }

    private boolean __setPreviewCallBack(Camera.PreviewCallback previewCallBack){
        if (camera == null){return false;}
        camera.setPreviewCallback(previewCallBack);
        return true;
    }

    private boolean __startPreview(){
        if (camera == null) {
            Log.e(TAG,"camera is null, can't start preview!!");
            isPreview = false;
            return isPreview;
        };
        try {
            camera.startPreview();
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
            isPreview = false;
            return isPreview;
        }
        isPreview = true;
        return isPreview;
    }

    private void __stopPreview(){
        if (camera == null){
            Log.e(TAG,"camera is null, can't stop preview!!");
            return;
        }
        if (isPreview){
            try {
                camera.stopPreview();
            }catch (Exception e){
                Log.e(TAG,e.getMessage());
                return;
            }
            isPreview = false;
        }
    }

    private void __takePicture(Camera.PictureCallback pictureCallback){
        if(camera != null){
            camera.takePicture(null,null,pictureCallback);
        }
    }

    private final int setPreviewRotationSucessful = 0x01 << 0;
    private final int setPictureRotationSucessful = 0x01 << 2;
    private final int setPreviewSizeSucessful = 0x01 << 3;
    private final int setPictureSizeSucessful = 0x01 << 4;
    private final int setPreviewFormateSucessful = 0x01 << 5;
    private final int setImageFormateSucessful = 0x01 << 6;
    private final int setFpsFormateSucessful = 0x01 << 7;
    private final int setFlashModeSucessful = 0x01 << 8;

    /**
     * 设置摄像头参数
     * @param previewRotation 预览方向,为0,90,180,270 以外数字不进行设置
     * @param pictureRotation 拍照方向,为0,90,180,270 以外数字不进行设置
     * @param previewSize 预览分辨率，为null不进行设置
     * @param pictureSize   拍照分辨率，为null不进行设置
     * @param previewFormate    预览帧格式，为-1不进行设置
     * @param imageFormate  相片格式，为-1 不进行设置
     * @param minFps    最小fps，为-1不进行fps设置
     * @param maxFps    最大fps，为-1不进行fps设置
     * @param flashMode 闪光灯模式，为null不进行设置
     * @return  返回int型数据，& 对应标志位是否等于1 判断对应属性是否设置成功
     *
     */
    private int __setPropety(int previewRotation, int pictureRotation, Size previewSize, Size pictureSize, int previewFormate, int imageFormate, int minFps, int maxFps, String flashMode){
        int result = 0;
        if (camera == null){return result;}

        boolean isRePreview = false;
        if (isPreview){
            isRePreview = true; //设置参数的时候关闭预览，等会还要打开预览
            __stopPreview(); // 停止预览
        }

        Camera.Parameters parameters = camera.getParameters();
        if (previewRotation == 0
                || previewRotation == 90
                || previewRotation == 180
                || previewRotation == 270){
            camera.setDisplayOrientation(previewRotation);
            result |= setPreviewRotationSucessful;
        }

        if (pictureRotation == 0
                || pictureRotation == 90
                || pictureRotation == 180
                || pictureRotation == 270){
            parameters = camera.getParameters();
            parameters.setRotation(pictureRotation);
            camera.setParameters(parameters);
            result |= setPictureRotationSucessful;
        }

        if (previewSize != null){
            parameters = camera.getParameters();
            parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
            camera.setParameters(parameters);
            result |= setPreviewSizeSucessful;
        }

        if (pictureSize != null){
            parameters = camera.getParameters();
            parameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
            camera.setParameters(parameters);
            result |= setPictureSizeSucessful;
        }

        if (previewFormate == ImageFormat.DEPTH16 ||
                previewFormate == ImageFormat.DEPTH_POINT_CLOUD ||
                previewFormate == ImageFormat.FLEX_RGBA_8888 ||
                previewFormate == ImageFormat.FLEX_RGB_888 ||
                previewFormate == ImageFormat.JPEG ||
                previewFormate == ImageFormat.NV16 ||
                previewFormate == ImageFormat.NV21 ||
                previewFormate == ImageFormat.PRIVATE ||
                previewFormate == ImageFormat.RAW10 ||
                previewFormate == ImageFormat.RAW12 ||
                previewFormate == ImageFormat.RAW_PRIVATE ||
                previewFormate == ImageFormat.RAW_SENSOR ||
                previewFormate == ImageFormat.RGB_565 ||
                previewFormate == ImageFormat.UNKNOWN ||
                previewFormate == ImageFormat.YUV_420_888 ||
                previewFormate == ImageFormat.YUV_422_888 ||
                previewFormate == ImageFormat.YUV_444_888 ||
                previewFormate == ImageFormat.YUY2 ||
                previewFormate == ImageFormat.YV12 ){
            parameters = camera.getParameters();
            parameters.setPreviewFormat(previewFormate);
            camera.setParameters(parameters);
            result |= setPreviewFormateSucessful;
        }

        if (imageFormate == ImageFormat.DEPTH16 ||
                imageFormate == ImageFormat.DEPTH_POINT_CLOUD ||
                imageFormate == ImageFormat.FLEX_RGBA_8888 ||
                imageFormate == ImageFormat.FLEX_RGB_888 ||
                imageFormate == ImageFormat.JPEG ||
                imageFormate == ImageFormat.NV16 ||
                imageFormate == ImageFormat.NV21 ||
                imageFormate == ImageFormat.PRIVATE ||
                imageFormate == ImageFormat.RAW10 ||
                imageFormate == ImageFormat.RAW12 ||
                imageFormate == ImageFormat.RAW_PRIVATE ||
                imageFormate == ImageFormat.RAW_SENSOR ||
                imageFormate == ImageFormat.RGB_565 ||
                imageFormate == ImageFormat.UNKNOWN ||
                imageFormate == ImageFormat.YUV_420_888 ||
                imageFormate == ImageFormat.YUV_422_888 ||
                imageFormate == ImageFormat.YUV_444_888 ||
                imageFormate == ImageFormat.YUY2 ||
                imageFormate == ImageFormat.YV12 ){
            parameters = camera.getParameters();
            parameters.setPictureFormat(imageFormate);
            camera.setParameters(parameters);
            result |= setImageFormateSucessful;
        }
        if ((minFps <= maxFps)
                && minFps > 0
                && maxFps > 0) {
            parameters = camera.getParameters();
            parameters.setPreviewFpsRange(minFps,maxFps);
            camera.setParameters(parameters);
            result |= setFpsFormateSucessful;
        }

        if (flashMode != null
            &&(flashMode .equals(Camera.Parameters.FLASH_MODE_AUTO)  ||
                flashMode .equals(Camera.Parameters.FLASH_MODE_OFF) ||
                flashMode .equals(Camera.Parameters.FLASH_MODE_ON) ||
                flashMode .equals(Camera.Parameters.FLASH_MODE_RED_EYE) ||
                flashMode .equals(Camera.Parameters.FLASH_MODE_TORCH))){
            parameters = camera.getParameters();
            parameters.setFlashMode(flashMode);
            camera.setParameters(parameters);
            result |= setFlashModeSucessful;
        }

        if(isRePreview){ //需要重新打开预览
            __startPreview();
        }

        return result;
    }


    private void __releaseCamera(){
        if (camera != null){
            setPreviewCallback(null);
            __stopPreview();
            try {
                camera.lock();
                camera.release();
                camera = null;
                isRelease = true;
            }catch (Exception e){
                Log.e(TAG,e.getMessage());
                isRelease = false;
            }
        }
    }

    /**
     * 打开Camera
     * @param cameraId 相机ID
     * @return 成功返回相机ID ，失败返回-1
     */
    public int openCamera(int cameraId){
        this.cameraId = __initCamera(cameraId);
        return this.cameraId;
    }

    /**
     * 返回摄像头
     * @return camera实例
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * 释放摄像头
     */
    public void releaseCamera(){
        __releaseCamera();
    }

    /**
     * 设置预览帧显示SurfaceHolder
     * @param surfaceHolder SurfaceHolder
     * @return 成功返回true， 失败返回 false
     */
    public boolean setPreviewDisplay(SurfaceHolder surfaceHolder){
        return __setPreviewDisplay(surfaceHolder);
    }

    /**
     * 设置预览方向
     * @param rotation 0,90,180.270
     * @return 成功返回 true ，失败返回 false。
     */
    public boolean setPreviewRotation(int rotation){
        return ((__setPropety(rotation,-1,null,null,-1,-1,-1,-1,null) & setPreviewRotationSucessful) != 0);
    }

    /**
     * 设置照片方向
     * @param rotation 0,90,180.270
     * @return 成功返回 true ，失败返回 false
     */
    public boolean setPictureRotation(int rotation){
        return ((__setPropety(-1,rotation,null,null,-1,-1,-1,-1,null) & setPictureRotationSucessful) != 0);
    }

    /**
     * 拍照
     * @param pictureCallback 拍照数据回调
     */
    public void takePicture(Camera.PictureCallback pictureCallback){
        __takePicture(pictureCallback);
    }

    /**
     * 设置预览分辨率
     * @param size  分辨率大小
     * @return 成功返回 true ，失败返回 false。注意：设置成功后也不一定能够生效，需要相机支持。
     */
    public boolean setPreviewSize(Size size){
        return ((__setPropety(-1,-1,size,null,-1,-1,-1,-1,null) & setPreviewSizeSucessful) != 0);
    }

    /**
     * 设置图片分辨率
     * @param size 分辨率大小
     * @return 成功返回 true ，失败返回 false。注意：设置成功后也不一定能够生效，需要相机支持。
     */
    public boolean setPictureSize(Size size){
        return ((__setPropety(-1,-1,null,size,-1,-1,-1,-1,null) & setPictureSizeSucessful) != 0);
    }

    /**
     * 设置预览帧格式
     * @param formate ImageFormat.DEPTH_POINT_CLOUD，ImageFormat.FLEX_RGBA_8888，ImageFormat.FLEX_RGB_888，ImageFormat.JPEG，ImageFormat.NV16，ImageFormat.NV21，ImageFormat.PRIVATE，ImageFormat.RAW10，ImageFormat.RAW12，ImageFormat.RAW_PRIVATE，ImageFormat.RAW_SENSOR，ImageFormat.RGB_565，ImageFormat.UNKNOWN，ImageFormat.YUV_420_888，ImageFormat.YUV_422_888，ImageFormat.YUV_444_888，ImageFormat.YUY2，ImageFormat.YV12
     * @return 成功返回 true ，失败返回 false。注意：设置成功后也不一定能够生效，需要相机支持。
     */
    public boolean setPreviewFormate(int formate){
        return ((__setPropety(-1,-1,null,null,formate,-1,-1,-1,null) & setPreviewFormateSucessful) != 0);
    }

    /**
     * 设置图片帧格式
     * @param formate  ImageFormat.DEPTH_POINT_CLOUD，ImageFormat.FLEX_RGBA_8888，ImageFormat.FLEX_RGB_888，ImageFormat.JPEG，ImageFormat.NV16，ImageFormat.NV21，ImageFormat.PRIVATE，ImageFormat.RAW10，ImageFormat.RAW12，ImageFormat.RAW_PRIVATE，ImageFormat.RAW_SENSOR，ImageFormat.RGB_565，ImageFormat.UNKNOWN，ImageFormat.YUV_420_888，ImageFormat.YUV_422_888，ImageFormat.YUV_444_888，ImageFormat.YUY2，ImageFormat.YV12
     * @return 成功返回 true ，失败返回 false。注意：设置成功后也不一定能够生效，需要相机支持。
     */
    public boolean setImageFormate(int formate){
        return ((__setPropety(-1,-1,null,null,-1,formate,-1,-1,null) & setImageFormateSucessful) != 0);
    }

    /**
     * 设置预览帧率范围
     * @param minFps 最小帧率
     * @param maxFps 最大帧率
     * @return 成功返回 true ，失败返回 false。注意：设置成功后也不一定能够生效，需要相机支持。
     */
    public boolean setFps(int minFps, int maxFps){
        return ((__setPropety(-1,-1,null,null,-1,-1,minFps,maxFps,null) & setFpsFormateSucessful) != 0);
    }

    /**
     * 设置闪光灯模式
     * @param flashMode Camera.Parameters.FLASH_MODE_AUTO，Camera.Parameters.FLASH_MODE_OFF，Camera.Parameters.FLASH_MODE_ON，Camera.Parameters.FLASH_MODE_RED_EYE，Camera.Parameters.FLASH_MODE_TORCH
     * @return 成功返回 true ，失败返回 false。注意：设置成功后也不一定能够生效，需要相机支持。
     */
    public boolean setFlashMode(String flashMode){
        return ((__setPropety(-1,-1,null,null,-1,-1,-1,-1,flashMode) & setFlashModeSucessful) != 0);
    }

    /**
     * 开启预览
     * @return 成功返回true， 失败返回false
     */
    public boolean startPreview(){
        return __startPreview();
    }

    /**
     * 停止预览
     */
    public void stopPreview(){
        __stopPreview();
    }

    /**
     * 设置预览回调接口类
     * @param previewCallback 预览回调接口类
     * @return 成功返回true ，失败返回 false
     */
    public boolean setPreviewCallback(Camera.PreviewCallback previewCallback){
        return __setPreviewCallBack(previewCallback);
    }

    /**
     * 获取相机支持的预览分辨率列表
     * @return 成功返回对应列表，失败返回null
     */
    public List<Camera.Size> getSupportPreviewSizes(){
        return camera == null?null:camera.getParameters().getSupportedPreviewSizes();
    }

    /**
     * 获取相机支持的拍照分辨率列表
     * @return 成功返回对应列表，失败返回null
     */
    public List<Camera.Size> getSupportPictureSizes(){
        return camera == null?null:camera.getParameters().getSupportedPictureSizes();
    }

    /**
     * 获取相机支持的录像分辨率列表
     * @return 成功返回对应列表，失败返回null
     */
    public List<Camera.Size> getSupportVideoSizes(){
        if (camera == null){return  null;}
        List<Camera.Size> sizes = camera.getParameters().getSupportedVideoSizes();
        if (sizes == null){
            sizes = camera.getParameters().getSupportedPreviewSizes();
        }
        return sizes;
    }

    /**
     * 获取预览帧范围
     * @return 成功返回对应列表，失败返回null
     */
    public List<int[]> getSupportPreviewFpsRange(){
        return camera == null?null:camera.getParameters().getSupportedPreviewFpsRange();
    }

    /**
     * 获取当前预览分辨率
     * @return 当前预览分辨率
     */
    public Size getPreviewSize(){
        return camera == null?null:new Size(camera.getParameters().getPreviewSize());
    }

    /**
     * 获取当前拍照分辨率
     * @return 拍照分辨率
     */
    public Size getPictureSize(){
        return camera == null?null:new Size(camera.getParameters().getPictureSize());
    }

    /**
     * 获取相机ID
     * @return 相机ID
     */
    public int getCameraId() {
        return cameraId;
    }

    public void dump(){
        String dump = "";
        if (camera == null){
            dump += "Camera is not open\n";
        }else{
            dump += "Camera is open\n";
            dump += isPreview?"camera is previewing\n":"camera has no preview\n";
            dump += "camera preview size ("+camera.getParameters().getPreviewSize().width+"x"+camera.getParameters().getPreviewSize().height+") picture size  ("+camera.getParameters().getPictureSize().width+"x"+camera.getParameters().getPictureSize().height+")\n";
            int[] fpsRange = new int[2];
            camera.getParameters().getPreviewFpsRange(fpsRange);
            dump += "camera preview fps = [ "+fpsRange[0]+"-"+fpsRange[1]+" ]\n";

            dump += "camera supported preview size:\n";
            for (Camera.Size preViewsize : getSupportPreviewSizes()) {
                dump += preViewsize.width+"x"+preViewsize.height+"\n";
            }

            dump += "camera supported picture size:\n";
            for (Camera.Size pictureSize: getSupportPictureSizes()){
                dump += pictureSize.width+"x"+pictureSize.height+"\n";
            }
        }

        Log.d(TAG,dump);
    }

    public static String savePicture(byte[] data, String dirPath, String picName){
        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        if (dirPath == null || picName == null){return  "照片存储路径不正确";}
        File dir = new File(dirPath);
        if (!dir.exists()){
            dir.mkdirs();
        }else{
            if (dir.isFile()) {
                Log.e("SavePicture", dirPath+" is a file");
                return "照片路径有冲突";
            }
        }
        File file = new File(dir+"/"+picName+".jpg");
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            Log.w("SavePicture","Picture is saved "+dirPath+"/"+picName+".jpg");
            return "照片以保存为："+dirPath+"/"+picName+".jpg";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "文件不存在";
        } catch (IOException e) {
            e.printStackTrace();
            return "存储照片失败";
        }
    }

    /**
     * 这个函数不应该对外开放
     * 获取 Camera 实例，需要先调用initCamera() 方法对Camera进行初始化
     * @return 返回 Camera 实例
     */
    private Camera getCameraInstance(){
        switch (this.cameraId){
            case -1:
                throw new RuntimeException("Can't open camera!!!");
            case -2:
                throw new RuntimeException("Please openCamera(int cameraId) first");
            default:
                return this.camera;
        }
    }

}
