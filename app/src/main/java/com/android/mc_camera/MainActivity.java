package com.android.mc_camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.awt.font.TextAttribute;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MCCameraTakePictureSurfaceView.TakePictureCallBack,MCCameraTakePictureSurfaceView.OnCameraStateListener{
    private final String TAG = getClass().getSimpleName();

    private MCCameraTakePictureSurfaceView mcCameraSurfaceView;

    private TextView fpsView,previewSizeShow,pictureSizeShow, cameraIdShow;
    private Button previewSizeSet, pictureSizeSet, chooseCamera, takePicture, switchToRecord;
    private ImageView picShow;
    private Toast showPicPath;


    private ListPopupWindow previewSizePop, pictureSizePop, cameraIdPop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();

        mcCameraSurfaceView.setDebugFps(true);
        mcCameraSurfaceView.initMcCamera(0,null);
        mcCameraSurfaceView.setTakePictureCallBack(this);
        mcCameraSurfaceView.setOnCameraStateListener(this);
    }

    private void initEvent() {
        previewSizeSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mcCameraSurfaceView.getPreviewSizeListString() != null && mcCameraSurfaceView.getPreviewSizeListString().size() > 0) {
                    previewSizePop.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, mcCameraSurfaceView.getPreviewSizeListString()));
                    previewSizePop.setWidth(ListPopupWindow.WRAP_CONTENT);
                    previewSizePop.setHeight(ListPopupWindow.WRAP_CONTENT);
                    previewSizePop.setAnchorView(previewSizeSet);
                    previewSizePop.setModal(true);
                    previewSizePop.show();
                }
            }
        });

        pictureSizeSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mcCameraSurfaceView.getPictureSizeListString() != null && mcCameraSurfaceView.getPictureSizeListString().size() > 0) {
                    pictureSizePop.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, mcCameraSurfaceView.getPictureSizeListString()));
                    pictureSizePop.setWidth(ListPopupWindow.WRAP_CONTENT);
                    pictureSizePop.setHeight(ListPopupWindow.WRAP_CONTENT);
                    pictureSizePop.setAnchorView(pictureSizeSet);
                    pictureSizePop.setModal(true);
                    pictureSizePop.show();
                }
            }
        });

        chooseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mcCameraSurfaceView.getCameraIdListString() != null && mcCameraSurfaceView.getCameraIdListString().size() > 0) {
                    cameraIdPop.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, mcCameraSurfaceView.getCameraIdListString()));
                    cameraIdPop.setWidth(ListPopupWindow.WRAP_CONTENT);
                    cameraIdPop.setHeight(ListPopupWindow.WRAP_CONTENT);
                    cameraIdPop.setAnchorView(chooseCamera);
                    cameraIdPop.setModal(true);
                    cameraIdPop.show();
                }
            }
        });

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (showPicPath != null){
//                    showPicPath.cancel();
//                }
                mcCameraSurfaceView.takePicture();
            }
        });


        switchToRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,RecordActivity.class);
                startActivity(intent);
                finish();
            }
        });


        previewSizePop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                previewSizePop.dismiss();
                Log.w(TAG,mcCameraSurfaceView.getPreviewSizeListString().get(i));
                mcCameraSurfaceView.setOrChangePreviewSize(new Size(mcCameraSurfaceView.getPreviewSizeListString().get(i)));
                mcCameraSurfaceView.getCameraSizeState();
            }
        });

        pictureSizePop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                pictureSizePop.dismiss();
                Log.w(TAG,mcCameraSurfaceView.getPictureSizeListString().get(i));
                mcCameraSurfaceView.setOrChangePictureSize(new Size(mcCameraSurfaceView.getPictureSizeListString().get(i)));
                mcCameraSurfaceView.getCameraSizeState();
            }
        });

        cameraIdPop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cameraIdPop.dismiss();
                String newCameraId = mcCameraSurfaceView.getCameraIdListString().get(i);
                Log.w(TAG,newCameraId);
                mcCameraSurfaceView.releaseMcCamera();
                mcCameraSurfaceView.initMcCamera(Integer.parseInt(newCameraId),null);
                mcCameraSurfaceView.getCameraSizeState();
            }
        });
    }

    private void initView() {
        mcCameraSurfaceView = findViewById(R.id.McCameraSurfaceView);
        fpsView = findViewById(R.id.fpsShow);
        previewSizeSet = findViewById(R.id.previewSizeSet);
        pictureSizeSet = findViewById(R.id.pictureSizeSet);
        chooseCamera = findViewById(R.id.chooseCamera);
        previewSizeShow = findViewById(R.id.previeSizeShow);
        pictureSizeShow = findViewById(R.id.pictureSizeShow);
        cameraIdShow = findViewById(R.id.cameraIdShow);
        switchToRecord = findViewById(R.id.switchToRecord);
        takePicture = findViewById(R.id.takePicture);
        picShow = findViewById(R.id.picShow);
        previewSizePop = new ListPopupWindow(this);
        pictureSizePop = new ListPopupWindow(this);
        cameraIdPop = new ListPopupWindow(this);
    }


    private void setPreviewSizeShow(Size preveiwSize){
        if (previewSizeShow == null){return;}
        String message = "预览分辨率：";
        if (preveiwSize == null){
            message += "未知";
        }else{
            message += preveiwSize.toString();
        }
        final String finalMessage = message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                previewSizeShow.setText(finalMessage);
            }
        });

    }

    private void setPictureSizeShow(Size pictureSize){
        if (pictureSizeShow == null){return;}
        String message = "拍照分辨率：";
        if (pictureSize == null){
            message += "未知";
        }else{
            message += pictureSize.toString();
        }
        final String finalMessage = message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pictureSizeShow.setText(finalMessage);
            }
        });

    }

    private void setCameraIdShow(final int cameraId){
        if (cameraIdShow == null){return;}
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraIdShow.setText("相机ID："+cameraId);
            }
        });

    }


    private void setPicShow(final Bitmap bitmap){
        if (bitmap == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                picShow.setImageBitmap(bitmap);
            }
        });
    }

    private void showFps(final int fps){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fpsView.setText("FPS:"+fps);
            }
        });
    }

    private void Toast(final String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (showPicPath == null) {
                  showPicPath =  Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT);
                }
                showPicPath.show();
            }
        });
    }

    @Override
    protected void onResume() {
        Log.w(TAG,"onResume");
        super.onResume();
        mcCameraSurfaceView.initMcCamera(0,null);
        mcCameraSurfaceView.setOnCameraStateListener(this);
        mcCameraSurfaceView.setTakePictureCallBack(this);
    }

    @Override
    protected void onPause() {
        Log.w(TAG,"onPause");
        super.onPause();
        mcCameraSurfaceView.releaseMcCamera();
        mcCameraSurfaceView.setOnCameraStateListener(null);
        mcCameraSurfaceView.setTakePictureCallBack(null);
    }

    @Override
    public void pictureCallBack(byte[] data) {
        setPicShow(BitmapFactory.decodeByteArray(data,0,data.length));
        String result = MCCamera.savePicture(data,"/sdcard","a");
//        Toast(result);
    }

    @Override
    public void onFpsListener(int fps) {
        showFps(fps);
    }

    @Override
    public void onCameraSizeListener(Size previewSize, Size pictureSize, int cameraId) {
        setPictureSizeShow(pictureSize);
        setPreviewSizeShow(previewSize);
        setCameraIdShow(cameraId);
    }
}
