package org.philimone.hds.forms.widget;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.AttributeSet;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import mz.betainteractive.utilities.StringUtil;

public class ColumnImageView extends ColumnView {

    private TextView txtName;
    private Button btTakePicture;
    private ImageView imgView;
    private TextView txtImageFile;

    //private ActivityResultLauncher<Intent> imageLauncher;

    public ColumnImageView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_image_item, attrs, columnModel, callListener);
        createView();
    }

    public ColumnImageView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
    }

    private void createView() {
        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btTakePicture = findViewById(R.id.btTakePicture);
        this.imgView = findViewById(R.id.imgView);
        this.txtImageFile = findViewById(R.id.txtImageFile);

        btTakePicture.setOnClickListener(v -> onButtonTakePictureClicked());

        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        refreshLabels();
        btTakePicture.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);

        initImageField(this.columnGroupView.getFormPanel());
        refreshModelToUI();
    }

    @Override
    public void refreshLabels() {
        setTextHtml(txtName, column.getLabel());
    }

    private void initImageField(FormFragment hostFragment) {
        // Registered on FormFragment
    }

    public void onImageCaptured(ActivityResult result) {
        FormFragment hostFragment = this.getActivity();
        String instanceFileName = hostFragment.getFormInstanceFileName();
        String instancesDirPath = hostFragment.getInstancesDirPath();
        String newFileName = instanceFileName + "_" + column.getName() + ".jpg";
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
            Toast.makeText(getContext(), getContext().getString(R.string.column_image_view_capture_cancelled_lbl), Toast.LENGTH_SHORT).show();
        }
    }

    private void onButtonTakePictureClicked() {
        openBuiltInCamera();
    }

    private void openBuiltInCamera() {
        FormFragment hostFragment = this.columnGroupView.getFormPanel();
        String instancesDirPath = hostFragment.getInstancesDirPath();

        if (instancesDirPath == null) {
            Toast.makeText(getContext(), getContext().getString(R.string.column_audio_view_dir_not_found_lbl), Toast.LENGTH_SHORT).show();
            return;
        }

        String newFileName = generateMediaFilename(".jpg");
        File destFile = new File(instancesDirPath, newFileName);

        Intent intent = new Intent(getContext(), CameraCaptureActivity.class);
        intent.putExtra(CameraCaptureActivity.EXTRA_OUTPUT_PATH, destFile.getAbsolutePath());

        getActivity().launchImageCapture(this, intent);
    }

    @Override
    public void refreshModelToUI() {
        String imageValue = columnModel.getValue();
        if (!StringUtil.isBlank(imageValue)) {
            Uri uri = resolveUri(imageValue);
            if (uri != null) {
                String fileName = uri.getLastPathSegment();
                txtImageFile.setText(fileName != null ? fileName : imageValue);
            } else {
                txtImageFile.setText(imageValue);
            }
            
            imgView.setVisibility(VISIBLE);

            try {
                InputStream is = getContext().getContentResolver().openInputStream(uri);
                if (is == null) throw new IOException("Could not open input stream");
                
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, options);
                is.close();
                options.inSampleSize = calculateInSampleSize(options, 400, 400);
                options.inJustDecodeBounds = false;

                is = getContext().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                imgView.setImageBitmap(bitmap);
                is.close();
            } catch (Exception e) {
                try {
                    String path = imageValue.startsWith("file://") ? Uri.parse(imageValue).getPath() : imageValue;
                    if (path != null && new File(path).exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        imgView.setImageBitmap(bitmap);
                    }
                } catch (Exception e2) {
                    imgView.setImageBitmap(null);
                }
            }
            
        } else {
            txtImageFile.setText(getContext().getString(R.string.column_image_view_no_image_lbl));
            imgView.setVisibility(GONE);
            imgView.setImageBitmap(null);
        }
        btTakePicture.setEnabled(!columnModel.isReadOnly());
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public void refreshInteractionState() {
        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        btTakePicture.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);
        btTakePicture.setEnabled(!columnModel.isReadOnly());
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
