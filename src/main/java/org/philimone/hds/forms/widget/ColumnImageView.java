package org.philimone.hds.forms.widget;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mz.betainteractive.utilities.StringUtil;

public class ColumnImageView extends ColumnView {

    private TextView txtName;
    private Button btTakePicture;
    private ImageView imgView;
    private TextView txtImageFile;

    private ActivityResultLauncher<Intent> imageLauncher;

    public ColumnImageView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_image_item, attrs, column, callListener);
        createView();
    }

    public ColumnImageView(ColumnGroupView view, @NonNull Column column, ExternalMethodCallListener callListener) {
        this(view, null, column, callListener);
    }

    private void createView() {
        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btTakePicture = findViewById(R.id.btTakePicture);
        this.imgView = findViewById(R.id.imgView);
        this.txtImageFile = findViewById(R.id.txtImageFile);

        btTakePicture.setOnClickListener(v -> onButtonTakePictureClicked());

        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        updateLabelTexts();
        btTakePicture.setVisibility(this.column.isReadOnly() ? GONE : VISIBLE);

        initImageField(this.columnGroupView.getFormPanel());
        updateValues();
    }

    @Override
    public void updateLabelTexts() {
        setTextHtml(txtName, column.getLabel());
    }

    private void initImageField(FormFragment hostFragment) {
        this.imageLauncher = hostFragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    String instanceFileName = hostFragment.getFormInstanceFileName();
                    String instancesDirPath = hostFragment.getInstancesDirPath();
                    String newFileName = instanceFileName + "_" + column.getName() + ".jpg";
                    File destFile = new File(instancesDirPath, newFileName);
                    
                    Log.d("ColumnImageView", "resultCode: " + result.getResultCode());
                    Log.d("ColumnImageView", "destFile: " + destFile.getAbsolutePath() + " exists: " + destFile.exists());

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri savedUri = (result.getData() != null) ? result.getData().getData() : null;
                        Log.d("ColumnImageView", "savedUri from data: " + savedUri);
                        if (savedUri == null && destFile.exists()) {
                            savedUri = Uri.fromFile(destFile);
                        }
                        
                        if (savedUri != null) {
                            Log.d("ColumnImageView", "calling setValue: " + savedUri);
                            setValue(savedUri.toString());
                        }
                    } else if (result.getResultCode() != Activity.RESULT_CANCELED) {
                        Toast.makeText(getContext(), getContext().getString(R.string.column_image_view_capture_cancelled_lbl), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void onButtonTakePictureClicked() {
        if (imageLauncher != null) {
            openBuiltInCamera();
        }
    }

    private void openBuiltInCamera() {
        Log.d("built-in-camera", "testing");
        FormFragment hostFragment = this.columnGroupView.getFormPanel();
        String instanceFileName = hostFragment.getFormInstanceFileName();
        String instancesDirPath = hostFragment.getInstancesDirPath();

        if (instancesDirPath == null) {
            Toast.makeText(getContext(), getContext().getString(R.string.column_audio_view_dir_not_found_lbl), Toast.LENGTH_SHORT).show();
            return;
        }

        String newFileName = instanceFileName + "_" + column.getName() + ".jpg";
        File destFile = new File(instancesDirPath, newFileName);

        Intent intent = new Intent(getContext(), CameraCaptureActivity.class);
        intent.putExtra(CameraCaptureActivity.EXTRA_OUTPUT_PATH, destFile.getAbsolutePath());
        imageLauncher.launch(intent);
    }

    @Override
    public void updateValues() {
        String imageValue = this.column.getValue();
        Log.d("ColumnImageView", "updateValues: " + imageValue);
        if (!StringUtil.isBlank(imageValue)) {
            Uri uri = resolveUri(imageValue);
            Log.d("ColumnImageView", "resolved uri: " + uri);
            if (uri != null) {
                String fileName = uri.getLastPathSegment();
                txtImageFile.setText(fileName != null ? fileName : imageValue);
            } else {
                txtImageFile.setText(imageValue);
            }
            
            imgView.setVisibility(VISIBLE);

            // Load bitmap carefully
            try {
                InputStream is = getContext().getContentResolver().openInputStream(uri);
                if (is == null) throw new IOException("Could not open input stream");
                
                // Get dimensions and Calculate sample size
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, options);
                is.close();
                options.inSampleSize = calculateInSampleSize(options, 400, 400);
                options.inJustDecodeBounds = false;

                is = getContext().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                Log.d("ColumnImageView", "bitmap: " + bitmap + " w: " + (bitmap != null ? bitmap.getWidth() : 0));
                imgView.setImageBitmap(bitmap);
                is.close();
            } catch (Exception e) {
                Log.e("ColumnImageView", "Error loading image", e);
                // try loading as direct file if content uri failed
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
        btTakePicture.setEnabled(!this.column.isReadOnly());
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
    public void refreshState() {
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        btTakePicture.setVisibility(this.column.isReadOnly() ? GONE : VISIBLE);
        btTakePicture.setEnabled(!this.column.isReadOnly());
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
}
