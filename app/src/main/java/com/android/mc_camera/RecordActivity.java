package com.android.mc_camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class RecordActivity extends Activity implements MCCameraRecordSurfaceView.OnCameraRecordSizeListener, MCCameraRecordSurfaceView.OnRecordError{
    private final String TAG =getClass().getSimpleName();

    private Button recordPix, chooseCamera, record, switchToTakePicture;
    private TextView previewSizeShow, cameraIdShow;
    private MCCameraRecordSurfaceView mcCameraRecordSurfaceView;
    private ListPopupWindow recordSizePop, cameraIdPop;
    private Toast toastShow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_main);
        initView();
        initEvent();
    }


    private void initView() {
        recordPix = findViewById(R.id.recordPix);
        record = findViewById(R.id.record);
        chooseCamera = findViewById(R.id.chooseCamera);
        previewSizeShow = findViewById(R.id.previeSizeShow);
        cameraIdShow = findViewById(R.id.cameraIdShow);
        switchToTakePicture = findViewById(R.id.switchToTakePicture);
        mcCameraRecordSurfaceView = findViewById(R.id.displayPreview);
        recordSizePop = new ListPopupWindow(this);
        cameraIdPop = new ListPopupWindow(this);

        mcCameraRecordSurfaceView.setOnCameraRecordSizeListener(this);
    }


    private void initEvent() {
        recordPix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mcCameraRecordSurfaceView.getVideoSizeListString() != null && mcCameraRecordSurfaceView.getVideoSizeListString().size() > 0){
                    recordSizePop.setAdapter(new ArrayAdapter<String>(RecordActivity.this, android.R.layout.simple_list_item_1, mcCameraRecordSurfaceView.getVideoSizeListString()));
                    recordSizePop.setWidth(ListPopupWindow.WRAP_CONTENT);
                    recordSizePop.setHeight(ListPopupWindow.WRAP_CONTENT);
                    recordSizePop.setAnchorView(recordPix);
                    recordSizePop.setModal(true);
                    recordSizePop.show();
                }else{
                    Log.e(TAG,"无法获取录像分辨率列表");
                }
            }
        });

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (toastShow != null){
//                    toastShow.cancel();
//                }
                if (!mcCameraRecordSurfaceView.isRecording()) {
                    mcCameraRecordSurfaceView.startRecord();
                }else{
                    mcCameraRecordSurfaceView.stopRecord();
                }
                setRecordButtonText();
                mcCameraRecordSurfaceView.getCameraSizeState();
            }
        });

        chooseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mcCameraRecordSurfaceView.getCameraIdListString() != null && mcCameraRecordSurfaceView.getCameraIdListString().size() > 0) {
                    cameraIdPop.setAdapter(new ArrayAdapter<String>(RecordActivity.this, android.R.layout.simple_list_item_1, mcCameraRecordSurfaceView.getCameraIdListString()));
                    cameraIdPop.setWidth(ListPopupWindow.WRAP_CONTENT);
                    cameraIdPop.setHeight(ListPopupWindow.WRAP_CONTENT);
                    cameraIdPop.setAnchorView(chooseCamera);
                    cameraIdPop.setModal(true);
                    cameraIdPop.show();
                }else{
                    Log.e(TAG,"无法获取CameraID列表");
                }
            }
        });

        switchToTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecordActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        recordSizePop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                recordSizePop.dismiss();
                mcCameraRecordSurfaceView.setRecordSize(new Size(mcCameraRecordSurfaceView.getVideoSizeListString().get(i)));
                if (mcCameraRecordSurfaceView.isRecording()){
                    mcCameraRecordSurfaceView.stopRecord();
                    mcCameraRecordSurfaceView.startRecord();
                }
                mcCameraRecordSurfaceView.getCameraSizeState();
            }
        });

        cameraIdPop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cameraIdPop.dismiss();
                String newCamerId = mcCameraRecordSurfaceView.getCameraIdListString().get(i);
                mcCameraRecordSurfaceView.releaseMcCamera();
                mcCameraRecordSurfaceView.initMcCamera(Integer.parseInt(newCamerId),null);
                if (mcCameraRecordSurfaceView.isRecording()){
                    mcCameraRecordSurfaceView.stopRecord();
                }
                mcCameraRecordSurfaceView.getCameraSizeState();
            }
        });
    }

    private void setRecordButtonText(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                record.setText(mcCameraRecordSurfaceView.isRecording()?"停止录像":"开始录像");
            }
        });
    }

    private void Toast(final String string){
        if (string == null){return;}
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toastShow == null) {
                    toastShow = Toast.makeText(RecordActivity.this, string, Toast.LENGTH_SHORT);
                }
                toastShow.show();
            }
        });
    }

    private void setPreviewSizeShow(Size preveiwSize, boolean isRecord){
        if (previewSizeShow == null){return;}
        String message = "";
        if (isRecord){
            message = "录像分辨率：";
        }else {
            message = "预览分辨率：";
        }
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

    private void setCameraIdShow(final int cameraId){
        if (cameraIdShow == null){return;}
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraIdShow.setText("相机ID："+cameraId);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mcCameraRecordSurfaceView.setOnCameraRecordSizeListener(null);
        mcCameraRecordSurfaceView.setOnRecordError(null);
        mcCameraRecordSurfaceView.releaseMcCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mcCameraRecordSurfaceView.initMcCamera(0,null);
        mcCameraRecordSurfaceView.setOnCameraRecordSizeListener(this);
        mcCameraRecordSurfaceView.setOnRecordError(this);
    }

    @Override
    public void cameraRecordSize(Size previewSize, int cameraId) {
        setPreviewSizeShow(previewSize,mcCameraRecordSurfaceView.isRecording());
        setCameraIdShow(cameraId);
        Log.w(TAG,"预览分辨率"+previewSize.toString()+" CameraId = "+cameraId);
    }

    @Override
    public void recordError(String errorMessage) {
        setRecordButtonText();
        Toast(errorMessage);
    }
}
