package org.philimone.hds.forms.widget;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.widget.dialog.AudioRecorderSelector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

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
    private MediaPlayer mediaPlayer;
    private final Handler seekHandler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = false;

    public ColumnAudioView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_audio_item, attrs, columnModel, callListener);
        createView();
    }

    public ColumnAudioView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
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

        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        refreshLabels();
        btRecordAudio.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);

        layoutPlayerPanel.setVisibility(GONE);

        initAudioField(this.columnGroupView.getFormPanel());
        refreshModelToUI();
    }

    @Override
    public void refreshLabels() {
        setTextHtml(txtName, column.getLabel());
    }

    private void initAudioField(FormFragment hostFragment) {
        // Registered on FormFragment
    }

    public void onPermissionsGranted(Map<String, Boolean> result) {
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

    private void onButtonRecordAudioClicked() {
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
            getActivity().launchAudioPermissions(this, permissions);
        }
    }

    private void openAudioRecorder() {
        FormFragment hostFragment = this.columnGroupView.getFormPanel();
        String instancesDirPath = hostFragment.getInstancesDirPath();

        if (instancesDirPath == null) {
            Toast.makeText(getContext(), getContext().getString(R.string.column_audio_view_dir_not_found_lbl), Toast.LENGTH_SHORT).show();
            return;
        }

        String newFileName = generateMediaFilename(".amr");
        File destFile = new File(instancesDirPath, newFileName);

        AudioRecorderSelector dialog = new AudioRecorderSelector(getContext(), destFile.getAbsolutePath(), filePath -> {
            setValue(Uri.fromFile(new File(filePath)).toString());
            afterUserInput();
        });
        dialog.show();
    }

    private void onButtonPlayAudioClicked() {
        String audioUriString = columnModel.getValue();
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
            btPlayAudio.setText(PAUSE_BUTTON_CHARACTER);
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
        btPlayAudio.setText(PLAY_BUTTON_CHARACTER);
        seekHandler.removeCallbacks(updaterRunnable);
    }

    private void stopAudioPlayback() {
        seekHandler.removeCallbacks(updaterRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        btPlayAudio.setText(PLAY_BUTTON_CHARACTER);
        audioSeekBar.setProgress(0);
        txtAudioTimer.setText("00:00");
    }

    private final Runnable updaterRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                int currentPos = mediaPlayer.getCurrentPosition();
                audioSeekBar.setProgress(currentPos);
                txtAudioTimer.setText(formatTime(currentPos));
                seekHandler.postDelayed(this, 100);
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
    public void refreshModelToUI() {
        String audioValue = columnModel.getValue();
        if (!StringUtil.isBlank(audioValue)) {
            Uri uri = resolveUri(audioValue);
            txtAudioFile.setText(uri.getLastPathSegment());
            layoutPlayerPanel.setVisibility(VISIBLE);
        } else {
            txtAudioFile.setText(getContext().getString(R.string.column_audio_view_no_recorded_lbl));
            layoutPlayerPanel.setVisibility(GONE);
        }
        btRecordAudio.setEnabled(!columnModel.isReadOnly());
    }

    @Override
    public void refreshInteractionState() {
        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        btRecordAudio.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);
        btRecordAudio.setEnabled(!columnModel.isReadOnly());
    }

    @Override
    public void setValue(String value) {
        this.columnModel.setValue(value);
        refreshModelToUI();
    }

    @Override
    public String getValue() {
        return this.columnModel.getValue();
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
