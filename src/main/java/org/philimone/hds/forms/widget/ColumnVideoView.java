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
import org.philimone.hds.forms.model.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.ColumnModel;

import androidx.activity.result.ActivityResult;
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

    //private ActivityResultLauncher<Intent> videoLauncher;

    public ColumnVideoView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_video_item, attrs, columnModel, callListener);
        createView();
    }

    public ColumnVideoView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
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

        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        refreshLabels();
        btRecordVideo.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);

        initVideoField(this.columnGroupView.getFormPanel());
        refreshModelToUI();
    }

    @Override
    public void refreshLabels() {
        setTextHtml(txtName, column.getLabel());
    }

    private void initVideoField(FormFragment hostFragment) {
        // Registered on FormFragment
    }

    public void onVideoCaptured(ActivityResult result) {
        FormFragment hostFragment = this.getActivity();
        String instancesDirPath = hostFragment.getInstancesDirPath();
        String newFileName = generateMediaFilename(".mp4");
        File destFile = new File(instancesDirPath, newFileName);

        if (result.getResultCode() == Activity.RESULT_OK) {
            Uri savedUri = (result.getData() != null) ? result.getData().getData() : null;
            if (savedUri == null && destFile.exists()) {
                savedUri = Uri.fromFile(destFile);
            }

            if (savedUri != null) {
                setValue(savedUri.toString());
                afterUserInput();
            }
        } else if (result.getResultCode() != Activity.RESULT_CANCELED) {
            Toast.makeText(getContext(), getContext().getString(R.string.column_video_view_capture_cancelled_lbl), Toast.LENGTH_SHORT).show();
        }
    }

    private void onButtonRecordVideoClicked() {
        openBuiltInVideoCamera();
    }

    private void openBuiltInVideoCamera() {
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
            startBuiltInVideoCamera();
        }
    }

    private void startBuiltInVideoCamera() {
        FormFragment hostFragment = this.columnGroupView.getFormPanel();
        String instancesDirPath = hostFragment.getInstancesDirPath();

        if (instancesDirPath == null) {
            Toast.makeText(getContext(), getContext().getString(R.string.column_audio_view_dir_not_found_lbl), Toast.LENGTH_SHORT).show();
            return;
        }

        String newFileName = generateMediaFilename(".mp4");
        File destFile = new File(instancesDirPath, newFileName);

        Intent intent = new Intent(getContext(), CameraCaptureActivity.class);
        intent.putExtra(CameraCaptureActivity.EXTRA_OUTPUT_PATH, destFile.getAbsolutePath());
        intent.putExtra(CameraCaptureActivity.EXTRA_MODE, CameraCaptureActivity.MODE_VIDEO);

        getActivity().launchVideoCapture(this, intent);
    }

    private void onButtonPlayVideoClicked() {
        String videoValue = columnModel.getValue();
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
    public void refreshModelToUI() {
        String videoValue = columnModel.getValue();
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

            try {
                File file = null;
                String videoValueRaw = columnModel.getValue();
                
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
        btRecordVideo.setEnabled(!columnModel.isReadOnly());
    }

    @Override
    public void refreshInteractionState() {
        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        btRecordVideo.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);
        btRecordVideo.setEnabled(!columnModel.isReadOnly());
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
}
