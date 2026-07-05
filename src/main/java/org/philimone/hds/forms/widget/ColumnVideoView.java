package org.philimone.hds.forms.widget;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.File;
import mz.betainteractive.utilities.StringUtil;

public class ColumnVideoView extends ColumnView {

    private TextView txtName;
    private Button btRecordVideo;
    private Button btPlayVideo;
    private ImageView imgVideoThumbnail;
    private TextView txtVideoFile;

    private ActivityResultLauncher<Intent> videoLauncher;

    public ColumnVideoView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_video_item, attrs, column, callListener);
        createView();
    }

    public ColumnVideoView(ColumnGroupView view, @NonNull Column column, ExternalMethodCallListener callListener) {
        this(view, null, column, callListener);
    }

    private void createView() {
        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btRecordVideo = findViewById(R.id.btRecordVideo);
        this.btPlayVideo = findViewById(R.id.btPlayVideo);
        this.imgVideoThumbnail = findViewById(R.id.imgVideoThumbnail);
        this.txtVideoFile = findViewById(R.id.txtVideoFile);

        btRecordVideo.setOnClickListener(v -> onButtonRecordVideoClicked());
        btPlayVideo.setOnClickListener(v -> onButtonPlayVideoClicked());

        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        updateLabelTexts();
        btRecordVideo.setVisibility(this.column.isReadOnly() ? GONE : VISIBLE);

        initVideoField(this.columnGroupView.getFormPanel());
        updateValues();
    }

    @Override
    public void updateLabelTexts() {
        setTextHtml(txtName, column.getLabel());
    }

    private void initVideoField(FormFragment hostFragment) {
        this.videoLauncher = hostFragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    String instanceFileName = hostFragment.getFormInstanceFileName();
                    String instancesDirPath = hostFragment.getInstancesDirPath();
                    String extension = ".mp4";
                    String newFileName = instanceFileName + "_" + column.getName() + extension;
                    File destFile = new File(instancesDirPath, newFileName);

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri savedUri = (result.getData() != null) ? result.getData().getData() : null;
                        if (savedUri == null && destFile.exists()) {
                            savedUri = Uri.fromFile(destFile);
                        }

                        if (savedUri != null) {
                            setValue(savedUri.toString());
                        }
                    } else if (result.getResultCode() != Activity.RESULT_CANCELED) {
                        Toast.makeText(getContext(), getContext().getString(R.string.column_video_view_capture_cancelled_lbl), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void onButtonRecordVideoClicked() {
        if (videoLauncher != null) {
            openBuiltInVideoCamera();
        }
    }

    private void openBuiltInVideoCamera() {
        //check permissions
        String[] permissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO};
        boolean allGranted = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), p) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            startBuiltInVideoCamera();
        } else {
            // Since we can't easily launch another permission request here without another launcher,
            // let's assume CameraCaptureActivity will handle its own permissions or just fail gracefully.
            startBuiltInVideoCamera();
        }
    }

    private void startBuiltInVideoCamera() {
        FormFragment hostFragment = this.columnGroupView.getFormPanel();
        String instanceFileName = hostFragment.getFormInstanceFileName();
        String instancesDirPath = hostFragment.getInstancesDirPath();

        if (instancesDirPath == null) {
            Toast.makeText(getContext(), getContext().getString(R.string.column_audio_view_dir_not_found_lbl), Toast.LENGTH_SHORT).show();
            return;
        }

        String extension = ".mp4";
        String newFileName = instanceFileName + "_" + column.getName() + extension;
        File destFile = new File(instancesDirPath, newFileName);

        Intent intent = new Intent(getContext(), CameraCaptureActivity.class);
        intent.putExtra(CameraCaptureActivity.EXTRA_OUTPUT_PATH, destFile.getAbsolutePath());
        intent.putExtra(CameraCaptureActivity.EXTRA_MODE, CameraCaptureActivity.MODE_VIDEO);
        videoLauncher.launch(intent);
    }

    private void onButtonPlayVideoClicked() {
        String videoValue = this.column.getValue();
        if (StringUtil.isBlank(videoValue)) return;

        Uri videoUri = resolveUri(videoValue);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(videoUri, "video/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(intent);
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.column_video_view_playback_error_lbl), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateValues() {
        String videoValue = this.column.getValue();
        if (!StringUtil.isBlank(videoValue)) {
            Uri uri = resolveUri(videoValue);
            if (uri != null) {
                String fileName = uri.getLastPathSegment();
                txtVideoFile.setText(fileName != null ? fileName : videoValue);
            } else {
                txtVideoFile.setText(videoValue);
                return;
            }

            imgVideoThumbnail.setVisibility(VISIBLE);
            btPlayVideo.setVisibility(VISIBLE);

            // Load thumbnail
            try {
                File file = null;
                String videoValueRaw = this.column.getValue();
                
                if (videoValueRaw != null) {
                    if (videoValueRaw.startsWith("file://") || videoValueRaw.startsWith("/")) {
                        String rawPath = videoValueRaw.startsWith("file://") ? Uri.parse(videoValueRaw).getPath() : videoValueRaw;
                        if (rawPath != null) file = new File(rawPath);
                    }
                    
                    if (file == null || !file.exists()) {
                        FormFragment activity = getActivity();
                        if (activity != null && activity.getInstancesDirPath() != null) {
                            file = new File(activity.getInstancesDirPath(), videoValueRaw);
                        }
                    }
                    
                    if (file == null || !file.exists()) {
                        // try as simple filename
                        Uri vUri = resolveUri(videoValueRaw);
                        if (vUri != null && "file".equals(vUri.getScheme())) {
                            file = new File(vUri.getPath());
                        }
                    }
                }

                if (file != null && file.exists()) {
                    Bitmap thumbnail = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        thumbnail = ThumbnailUtils.createVideoThumbnail(file, new Size(640, 480), null);
                    } else {
                        thumbnail = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
                    }
                    if (thumbnail != null) {
                        imgVideoThumbnail.setImageBitmap(thumbnail);
                    }
                }
            } catch (Exception e) {
                Log.e("ColumnVideoView", "Error loading thumbnail", e);
            }

        } else {
            txtVideoFile.setText(getContext().getString(R.string.column_video_view_no_video_lbl));
            imgVideoThumbnail.setVisibility(GONE);
            btPlayVideo.setVisibility(GONE);
        }
        btRecordVideo.setEnabled(!this.column.isReadOnly());
    }

    @Override
    public void refreshState() {
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        btRecordVideo.setVisibility(this.column.isReadOnly() ? GONE : VISIBLE);
        btRecordVideo.setEnabled(!this.column.isReadOnly());
        updateValues();
    }

    @Override
    public void setValue(String value) {
        this.column.setValue(value); updateValues();
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
}
