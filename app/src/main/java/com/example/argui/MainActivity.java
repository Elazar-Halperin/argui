package com.example.argui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CAMERA = 43391;
    boolean cameraPermissionGranted;

    CameraManager cameraManager;
    TextureView textureView;
    CameraDevice cameraDevice;
    CameraCaptureSession cameraCaptureSession;
    Handler handler;
    HandlerThread handlerThread;
    CaptureRequest.Builder capReq;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraPermissionGranted = false;
        getCameraPermission();

        textureView = findViewById(R.id.textureView);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        handlerThread = new HandlerThread("videoThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });


    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        CameraCaptureSession.StateCallback capCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession camCap) {
                cameraCaptureSession = camCap;
                try {
                    cameraCaptureSession.setRepeatingRequest(capReq.build(), null, null);
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

            }
        };

        CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice cd) {
                cameraDevice = cd;
                try {
                    capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    Surface surface = new Surface(textureView.getSurfaceTexture());
                    capReq.addTarget(surface);

                    List<Surface> surfaceList = new ArrayList<>();
                    surfaceList.add(surface);
                    cameraDevice.createCaptureSession(surfaceList, capCallback, null);

                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {

            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {

            }
        };


        String[] cameraIds = new String[0];
        try {
            cameraIds = cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }

        String cameraId = cameraIds[1];


        try {
            cameraManager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }

    }

    private void getCameraPermission() {
        // Check if App already has permissions for camera
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED) {
            // App has permissions to listen incoming SMS messages
            cameraPermissionGranted = true;

        } else {
            // Request permissions from user
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_CAMERA) {
            if (permissions.length > 0 && grantResults.length > 0) {
                // Check if the user granted CAMERA permission
                if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // The user granted CAMERA permission.
                    // You can now proceed with camera-related functionality.
                    cameraPermissionGranted = true;
                }
            }
        }
        Log.d("adnan", cameraPermissionGranted + "");
    }
}