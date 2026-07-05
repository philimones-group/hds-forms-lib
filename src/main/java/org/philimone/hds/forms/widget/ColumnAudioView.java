package org.philimone.hds.forms.widget;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.widget.dialog.AudioRecorderSelector;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import mz.betainteractive.utilities.StringUtil;

public class ColumnAudioView extends ColumnView {

    public static final String PAUSE_BUTTON_CHARACTER = "❚❚";
    public static final String PLAY_BUTTON_CHARACTER = "▶";
    private TextView txtName;
    private Button btRecordAudio;
    private Button btPlayAudio;
    private TextView txtAudioFile;
    private TextView txtAudioTimer;
    private SeekBar audioSeekBar;
    private LinearLayout layoutPlayerPanel;
    private LinearLayout layoutAudioFile;

    private ActivityResultLauncher<String[]> audioPermissionLauncher;
    private MediaPlayer mediaPlayer;
    private final Handler seekHandler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = false;

    public ColumnAudioView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_audio_item, attrs, column, callListener);
        createView();
    }

    public ColumnAudioView(ColumnGroupView view, @NonNull Column column, ExternalMethodCallListener callListener) {
        this(view, null, column, callListener);
    }

    private void createView() {
        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btRecordAudio = findViewById(R.id.btRecordAudio);
        this.btPlayAudio = findViewById(R.id.btPlayAudio);
        this.txtAudioFile = findViewById(R.id.txtAudioFile);
        this.txtAudioTimer = findViewById(R.id.txtAudioTimer);
        this.audioSeekBar = findViewById(R.id.audioSeekBar);
        this.layoutPlayerPanel = findViewById(R.id.layoutPlayerPanel);
        this.layoutAudioFile = findViewById(R.id.layoutAudioFile);

        btRecordAudio.setOnClickListener(v -> onButtonRecordAudioClicked());
        btPlayAudio.setOnClickListener(v -> onButtonPlayAudioClicked());

        // WhatsApp feature: Allow scrubbing/seeking through the audio tracking line manually
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                    txtAudioTimer.setText(formatTime(progress));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        updateLabelTexts();
        btRecordAudio.setVisibility(this.column.isReadOnly() ? GONE : VISIBLE);

        layoutPlayerPanel.setVisibility(GONE);

        initAudioField(this.columnGroupView.getFormPanel());
        updateValues();
    }

    @Override
    public void updateLabelTexts() {
        setTextHtml(txtName, column.getLabel());
    }

    private void initAudioField(FormFragment hostFragment) {
        this.audioPermissionLauncher = hostFragment.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean granted = true;
                    for (Boolean b : result.values()) {
                        if (!b) {
                            granted = false;
                            break;
                        }
                    }

                    if (granted) {
                        openAudioRecorder();
                    } else {
                        Toast.makeText(getContext(), R.string.column_audio_view_error_recording, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void onButtonRecordAudioClicked() {
        //check permissions
        String[] permissions = new String[]{android.Manifest.permission.RECORD_AUDIO};
        boolean allGranted = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), p) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            openAudioRecorder();
        } else {
            audioPermissionLauncher.launch(permissions);
        }
    }

    private void openAudioRecorder() {
        FormFragment hostFragment = this.columnGroupView.getFormPanel();
        String instanceFileName = hostFragment.getFormInstanceFileName();
        String instancesDirPath = hostFragment.getInstancesDirPath();

        if (instancesDirPath == null) {
            Toast.makeText(getContext(), getContext().getString(R.string.column_audio_view_dir_not_found_lbl), Toast.LENGTH_SHORT).show();
            return;
        }

        String extension = ".amr";
        String newFileName = instanceFileName + "_" + column.getName() + extension;
        File destFile = new File(instancesDirPath, newFileName);

        AudioRecorderSelector dialog = new AudioRecorderSelector(getContext(), destFile.getAbsolutePath(), filePath -> {
            setValue(Uri.fromFile(new File(filePath)).toString());
        });
        dialog.show();
    }

    private void onButtonPlayAudioClicked() {
        String audioUriString = this.column.getValue();
        if (audioUriString == null || audioUriString.isEmpty()) return;

        if (isPlaying) {
            pauseAudioPlayback();
        } else {
            Uri uri = resolveUri(audioUriString);
            startAudioPlayback(uri);
        }
    }

    private void startAudioPlayback(Uri uri) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(getContext(), uri);
                mediaPlayer.prepare();
                audioSeekBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.setOnCompletionListener(mp -> stopAudioPlayback());
            }

            mediaPlayer.start();
            isPlaying = true;
            btPlayAudio.setText(PAUSE_BUTTON_CHARACTER); // Change icon to Pause character
            updateSeekBarLoop();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), R.string.column_audio_view_playback_error_lbl, Toast.LENGTH_SHORT).show();
            stopAudioPlayback();
        }
    }

    private void pauseAudioPlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        isPlaying = false;
        btPlayAudio.setText(PLAY_BUTTON_CHARACTER); // Change icon back to Play character
        seekHandler.removeCallbacks(updaterRunnable);
    }

    private void stopAudioPlayback() {
        seekHandler.removeCallbacks(updaterRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        btPlayAudio.setText("▶");
        audioSeekBar.setProgress(0);
        txtAudioTimer.setText("00:00");
    }

    // Background thread updater runnable to refresh seekbar milestones smoothly
    private final Runnable updaterRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                int currentPos = mediaPlayer.getCurrentPosition();
                audioSeekBar.setProgress(currentPos);
                txtAudioTimer.setText(formatTime(currentPos));
                seekHandler.postDelayed(this, 100); // Trigger update every 100ms
            }
        }
    };

    private void updateSeekBarLoop() {
        seekHandler.post(updaterRunnable);
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    @Override
    public void updateValues() {
        String audioValue = this.column.getValue();
        if (!StringUtil.isBlank(audioValue)) {
            Uri uri = resolveUri(audioValue);
            txtAudioFile.setText(uri.getLastPathSegment());
            layoutPlayerPanel.setVisibility(VISIBLE); // Reveal WhatsApp slider panel
        } else {
            txtAudioFile.setText(getContext().getString(R.string.column_audio_view_no_recorded_lbl));
            layoutPlayerPanel.setVisibility(GONE);    // Collapse slider panel
        }
        btRecordAudio.setEnabled(!this.column.isReadOnly());
    }

    @Override
    public void refreshState() {
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        btRecordAudio.setVisibility(this.column.isReadOnly() ? GONE : VISIBLE);
        btRecordAudio.setEnabled(!this.column.isReadOnly());
        updateValues();
    }

    @Override
    public void setValue(String value) {
        this.column.setValue(value);
        updateValues();
    }

    @Override
    public String getValue() {
        return this.column.getValue();
    }

    @Override
    public String getValueAsXml() {
        String value = getValue();
        String name = this.column.getName();
        if (value != null) { value = Uri.parse(value).getLastPathSegment(); }
        return value == null ? "<" + name + " />" : "<" + name + ">" + value + "</" + name + ">";
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAudioPlayback();
    }
}
