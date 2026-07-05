package org.philimone.hds.forms.widget;

import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ColumnBarcodeView extends ColumnView {

    private TextView txtName;
    private Button btGetBarcode;
    private TextView txtBarcode;

    private ActivityResultLauncher<ScanOptions> barcodeLauncher;

    public ColumnBarcodeView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_barcode_item, attrs, column, callListener);
        createView();
    }

    public ColumnBarcodeView(ColumnGroupView view, @NonNull Column column, ExternalMethodCallListener callListener) {
        this(view, null, column, callListener);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btGetBarcode = findViewById(R.id.btGetBarcode);
        this.txtBarcode = findViewById(R.id.txtBarcode);

        btGetBarcode.setOnClickListener(v -> {
            onButtonScanBarcodeClicked();
        });

        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        updateLabelTexts();
        btGetBarcode.setVisibility(this.column.isReadOnly() ? GONE : VISIBLE);

        initBarcodeField(this.columnGroupView.getFormPanel());
    }

    @Override
    public void updateLabelTexts() {
        setTextHtml(txtName, column.getLabel());
    }

    /**
     * Call this inside your ColumnGroupView generation loop right after instantiation
     */
    private void initBarcodeField(FormFragment hostFragment) {
        // SELF-CONTAINED LAUNCHER REGISTRATION
        // Each question view registers its own execution loop directly tied to the Fragment lifecycle
        this.barcodeLauncher = hostFragment.registerForActivityResult(
                new ScanContract(),
                result -> {
                    if (result.getContents() != null) {
                        String scannedValue = result.getContents();
                        // Update UI
                        setValue(scannedValue);
                    }
                }
        );
    }

    private void onButtonScanBarcodeClicked() {

        if (barcodeLauncher != null) {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
            options.setPrompt(getContext().getString(R.string.bt_barcode_lbl));
            options.setCameraId(0);
            options.setBeepEnabled(false);
            options.setBarcodeImageEnabled(false);
            options.setOrientationLocked(false);

            // Fire the camera via the host activity result pipeline
            barcodeLauncher.launch(options);
        }
    }

    @Override
    public void updateValues() {
        txtBarcode.setText(this.column.getValue());
        btGetBarcode.setEnabled(!this.column.isReadOnly());
    }

    @Override
    public void refreshState() {
        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        btGetBarcode.setVisibility(this.column.isReadOnly() ? GONE : VISIBLE);
        btGetBarcode.setEnabled(!this.column.isReadOnly());
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

        return value==null ? "<"+ name + " />" : "<"+name+">"+value+"</ "+name+">";
    }

}
