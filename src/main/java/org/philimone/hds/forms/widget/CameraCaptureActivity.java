package org.philimone.hds.forms.widget;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.philimone.hds.forms.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraCaptureActivity extends AppCompatActivity {
    private static final String TAG = "CameraCaptureActivity";
    public static final String EXTRA_OUTPUT_PATH = "output_path";
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_IMAGE = "image";
    public static final String MODE_VIDEO = "video";

    private PreviewView previewView;
    private Button btCapture;
    private String outputFilePath;
    private String mode;
    
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_capture_layout);

        outputFilePath = getIntent().getStringExtra(EXTRA_OUTPUT_PATH);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_IMAGE;

        previewView = findViewById(R.id.cameraPreview);
        btCapture = findViewById(R.id.btCapture);
        btCapture.setOnClickListener(v -> {
            if (MODE_IMAGE.equals(mode)) {
                takePicture();
            } else {
                if (recording != null) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), 200);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                if (MODE_IMAGE.equals(mode)) {
                    imageCapture = new ImageCapture.Builder().build();
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                } else {
                    Recorder recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build();
                    videoCapture = VideoCapture.withOutput(recorder);
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePicture() {
        if (imageCapture == null) return;

        File outputFile = new File(outputFilePath);
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(CameraCaptureActivity.this, "Photo Saved!", Toast.LENGTH_SHORT).show();
                
                Intent resultIntent = new Intent();
                Uri savedUri = outputFileResults.getSavedUri();
                if (savedUri == null) {
                    savedUri = Uri.fromFile(outputFile);
                }
                resultIntent.setData(savedUri);
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    private void startRecording() {
        if (videoCapture == null) return;

        btCapture.setEnabled(false);

        File outputFile = new File(outputFilePath);
        FileOutputOptions fileOutputOptions = new FileOutputOptions.Builder(outputFile).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        recording = videoCapture.getOutput()
                .prepareRecording(this, fileOutputOptions).withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                        btCapture.setBackgroundResource(android.R.drawable.ic_menu_close_clear_cancel);
                        btCapture.setEnabled(true);
                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) videoRecordEvent;
                        if (!finalizeEvent.hasError()) {
                            Toast.makeText(CameraCaptureActivity.this, "Video saved!", Toast.LENGTH_SHORT).show();
                            
                            Intent resultIntent = new Intent();
                            Uri savedUri = finalizeEvent.getOutputResults().getOutputUri();
                            if (savedUri == null || "file".equals(savedUri.getScheme())) {
                                savedUri = Uri.fromFile(outputFile);
                            }
                            resultIntent.setData(savedUri);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            if (recording != null) {
                                recording.close();
                                recording = null;
                            }
                            Log.e(TAG, "Video recording error: " + finalizeEvent.getError());
                        }
                    }
                });
    }

    private void stopRecording() {
        if (recording != null) {
            recording.stop();
            recording = null;
        }
    }

    private String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        if (MODE_VIDEO.equals(mode)) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        return permissions.toArray(new String[0]);
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
