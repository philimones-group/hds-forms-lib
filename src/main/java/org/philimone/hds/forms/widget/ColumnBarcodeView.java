package org.philimone.hds.forms.widget;

import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.model.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.ColumnModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

public class ColumnBarcodeView extends ColumnView {

    private TextView txtName;
    private Button btGetBarcode;
    private TextView txtBarcode;

    public ColumnBarcodeView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_barcode_item, attrs, columnModel, callListener);
        createView();
    }

    public ColumnBarcodeView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btGetBarcode = findViewById(R.id.btGetBarcode);
        this.txtBarcode = findViewById(R.id.txtBarcode);

        btGetBarcode.setOnClickListener(v -> {
            onButtonScanBarcodeClicked();
        });

        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        refreshLabels();
        btGetBarcode.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);

        initBarcodeField(this.columnGroupView.getFormPanel());
    }

    @Override
    public void refreshLabels() {
        setTextHtml(txtName, column.getLabel());
    }

    private void initBarcodeField(FormFragment hostFragment) {
        // Registered on FormFragment
    }

    public void onBarcodeResult(ScanIntentResult result) {
        if (result.getContents() != null) {
            String scannedValue = result.getContents();
            setValue(scannedValue);
            afterUserInput();
        }
    }

    private void onButtonScanBarcodeClicked() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setPrompt(getContext().getString(R.string.bt_barcode_lbl));
        options.setCameraId(0);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(false);
        options.setOrientationLocked(false);

        getActivity().launchBarcodeScanner(this, options);
    }

    @Override
    public void refreshModelToUI() {
        txtBarcode.setText(columnModel.getValue());
    }

    @Override
    public void refreshInteractionState() {
        txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        btGetBarcode.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);
        btGetBarcode.setEnabled(!columnModel.isReadOnly());
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

        return value==null ? "<"+ name + " />" : "<"+name+">"+value+"</ "+name+">";
    }

}
