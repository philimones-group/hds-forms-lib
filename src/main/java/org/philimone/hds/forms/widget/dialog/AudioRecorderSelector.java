package org.philimone.hds.forms.widget.dialog;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.philimone.hds.forms.R;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

public class AudioRecorderSelector extends AppCompatDialog {

    private Context mContext;
    private TextView txtRecordTimer;
    private Button btStartRecord;
    private Button btStopRecord;
    private Button btDialogCancel;

    private MediaRecorder mediaRecorder;
    private String outputFilePath;
    private boolean isRecording = false;
    private long startTime = 0;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    private OnSelectedListener listener;

    public AudioRecorderSelector(@NonNull Context context, String outputFilePath, OnSelectedListener listener) {
        super(context);
        this.mContext = context;
        this.outputFilePath = outputFilePath;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.audio_recorder_selector);

        initialize();
    }

    private void initialize() {
        this.txtRecordTimer = findViewById(R.id.txtRecordTimer);
        this.btStartRecord = findViewById(R.id.btStartRecord);
        this.btStopRecord = findViewById(R.id.btStopRecord);
        this.btDialogCancel = findViewById(R.id.btDialogCancel);

        this.btStartRecord.setOnClickListener(v -> startRecording());
        this.btStopRecord.setOnClickListener(v -> stopRecording());
        this.btDialogCancel.setOnClickListener(v -> cancelRecording());

        setCancelable(false);
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(outputFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            startTime = System.currentTimeMillis();
            updateTimer();

            btStartRecord.setEnabled(false);
            btStopRecord.setEnabled(true);
            btDialogCancel.setEnabled(false);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.column_audio_view_error_recording, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                // handle case where no audio data has been received
                e.printStackTrace();
            }
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            timerHandler.removeCallbacks(timerRunnable);

            if (listener != null) {
                listener.onAudioRecorded(outputFilePath);
            }
            dismiss();
        }
    }

    private void cancelRecording() {
        if (isRecording) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            timerHandler.removeCallbacks(timerRunnable);
        }
        
        File file = new File(outputFilePath);
        if (file.exists()) {
            file.delete();
        }
        
        dismiss();
    }

    private void updateTimer() {
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                txtRecordTimer.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    public interface OnSelectedListener {
        void onAudioRecorded(String filePath);
    }
}
