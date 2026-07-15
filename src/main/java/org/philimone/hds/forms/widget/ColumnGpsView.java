package org.philimone.hds.forms.widget;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.listeners.ExternalMethodCallListener;
import org.philimone.hds.forms.model.ColumnModel;
import org.philimone.hds.forms.utilities.GpsFormatter;
import mz.betainteractive.utilities.StringUtil;
import org.philimone.hds.forms.widget.dialog.DialogFactory;
import org.philimone.hds.forms.widget.dialog.LoadingDialog;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ColumnGpsView extends ColumnView implements LocationListener {

    private TextView txtName;
    private LinearLayout getGpsLayout;
    private Button btGetGps;
    private TextView txtGpsLatitude;
    private TextView txtGpsLongitude;
    private TextView txtGpsAltitude;
    private TextView txtGpsAccuracy;
    private Location gpsLocationResult;

    private LocationManager locationManager;
    private LoadingDialog loadingDialog;

    //private ActivityResultLauncher<String[]> requestPermissions;

    public ColumnGpsView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        super(view, R.layout.column_gps_item, attrs, columnModel, callListener);

        initialize();
    }

    public ColumnGpsView(ColumnGroupView view, @NonNull ColumnModel columnModel, ExternalMethodCallListener callListener) {
        this(view, null, columnModel, callListener);
    }

    private void initialize(){
        createView();
        initPermissions();
    }

    private void initPermissions() {
        // Registered on FormFragment
    }

    public void onPermissionsGranted(Map<String, Boolean> permissionResults) {
        boolean granted = !permissionResults.values().contains(false);

        if (granted) {
            detectGpsLocation();
        } else {
            DialogFactory.createMessageInfo(this.getContext(), R.string.gps_title_lbl, R.string.gps_permissions_error).show();
        }
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.getGpsLayout = findViewById(R.id.getGpsLayout);
        this.btGetGps = findViewById(R.id.btGetGps);
        this.txtGpsLatitude = findViewById(R.id.txtGpsLatitude);
        this.txtGpsLongitude = findViewById(R.id.txtGpsLongitude);
        this.txtGpsAltitude = findViewById(R.id.txtGpsAltitude);
        this.txtGpsAccuracy = findViewById(R.id.txtGpsAccuracy);

        this.loadingDialog = new LoadingDialog(this.getContext());
        this.loadingDialog.setListener(() -> onCancelGpsDetection());

        this.btGetGps.setOnClickListener(v -> onGetGpsClicked());

        this.getGpsLayout.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);

        //if there is a gps values to be loaded we load
        if (!this.columnModel.getGpsValues().isEmpty()) {
            this.gpsLocationResult = getLocation(this.columnModel.getGpsValues());
        }
        
        refreshModelToUI();
    }

    @Override
    public void refreshLabels() {
        setTextHtml(txtName, column.getLabel());
    }

    private void clearGpsResultTexts() {
        this.txtGpsLatitude.setText("");
        this.txtGpsLongitude.setText("");
        this.txtGpsAltitude.setText("");
        this.txtGpsAccuracy.setText("");
    }

    private void showResults() {
        if (this.gpsLocationResult != null) {
            this.txtGpsLatitude.setText(Location.convert(this.gpsLocationResult.getLatitude(), Location.FORMAT_DEGREES));
            this.txtGpsLongitude.setText(Location.convert(this.gpsLocationResult.getLongitude(), Location.FORMAT_DEGREES));
            this.txtGpsAltitude.setText("" + this.gpsLocationResult.getAltitude());
            this.txtGpsAccuracy.setText("" + this.gpsLocationResult.getAccuracy());
        }
    }

    private void showLoadingDialog(@StringRes int msgResId, boolean show) {
        showLoadingDialog(this.getContext().getString(msgResId), show);
    }

    private void showLoadingDialog(String msg, boolean show) {
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.hide();
        }
    }

    private void ensurePermissionsGranted(final String... permissions) {
        boolean denied = Arrays.stream(permissions).anyMatch(permission -> ContextCompat.checkSelfPermission(this.getContext(), permission) == PackageManager.PERMISSION_DENIED);

        if (denied) {
            getActivity().launchGpsPermissions(this, permissions);
        } else {
            detectGpsLocation();
        }
    }

    private void onGetGpsClicked() {
        ensurePermissionsGranted(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void detectGpsLocation() {

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            DialogFactory.createMessageInfo(this.getContext(), R.string.gps_title_lbl, R.string.gps_permissions_error).show();
            return;
        }

        this.locationManager = this.locationManager == null ? (LocationManager)this.getContext().getSystemService(Context.LOCATION_SERVICE) : locationManager;

        boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        String provider = gps_enabled ? LocationManager.GPS_PROVIDER : network_enabled ? LocationManager.NETWORK_PROVIDER : "";

        if (provider.isEmpty()) {
            DialogFactory.createMessageInfo(this.getContext(), R.string.gps_title_lbl, R.string.gps_no_provider_available_error).show();
            return;
        }

        this.gpsLocationResult = null;

        showLoadingDialog(R.string.gps_loading_lbl, true);
        locationManager.requestLocationUpdates(provider, 5, 0, this);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            showCancelGpsDetection();
        }, 20000);
    }

    private void showCancelGpsDetection() {
        this.loadingDialog.showCancelButton();
    }

    private void onCancelGpsDetection() {
        try {
            locationManager.removeUpdates(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void refreshModelToUI() {
        clearGpsResultTexts();

        showResults();
    }

    @Override
    public void refreshInteractionState() {
        this.getGpsLayout.setVisibility(columnModel.isReadOnly() ? GONE : VISIBLE);
        this.txtColumnRequired.setVisibility(columnModel.isRequired() ? VISIBLE : GONE);
        this.btGetGps.setEnabled(!columnModel.isReadOnly());
    }

    @Override
    public void setValue(String value) {
        if (StringUtil.isBlank(value)) return;

        Double[] values = GpsFormatter.getValuesFrom(value);
        if (values != null) {
            this.gpsLocationResult = new Location("fake");

            this.gpsLocationResult.setLatitude(values[0]);
            this.gpsLocationResult.setLongitude(values[1]);
            this.gpsLocationResult.setAltitude(values[2]);
            this.gpsLocationResult.setAccuracy((float) (values[3]*1F));

            refreshModelToUI();
        }

        this.columnModel.setValue(value);
        this.columnModel.setGpsValues(getValues());
    }

    public void setValues(Map<String,Double> gpsValues) {
        if (gpsValues == null) return;

        this.gpsLocationResult = getLocation(gpsValues);

        this.setValue(GpsFormatter.format(this.gpsLocationResult)); //must save in format: "\d+, \d+, Alt: \d+, Acc: \d+"

        refreshModelToUI();
    }

    private Location getLocation(Map<String,Double> gpsValues) {
        Location location = new Location("fake");

        Double lat = gpsValues.get(column.getName()+"Lat");
        Double lon = gpsValues.get(column.getName()+"Lon");
        Double alt = gpsValues.get(column.getName()+"Alt");
        Double acc = gpsValues.get(column.getName()+"Acc");

        if (lat != null) location.setLatitude(lat);
        if (lon != null) location.setLongitude(lon);
        if (alt != null) location.setAltitude(alt);
        if (acc != null) location.setAccuracy((float) (acc*1F));

        return location;
    }

    @Override
    public String getValue() {
        return this.columnModel.getValue();
    }

    public Map<String, Double> getValues(){
        Map<String, Double> map = new LinkedHashMap<>();

        if (gpsLocationResult != null) {
            String name = this.column.getName();

            map.put(name+"Lat", gpsLocationResult.getLatitude());
            map.put(name+"Lon", gpsLocationResult.getLongitude());
            map.put(name+"Alt", gpsLocationResult.getAltitude());
            map.put(name+"Acc", gpsLocationResult.getAccuracy()*1d);
        }

        return map;
    }

    @Override
    public String getValueAsXml() {
        String value = getValue();
        String name = this.column.getName();

        return value==null ? "<"+ name + " />" : "<"+name+">"+value+"</ "+name+">";
    }

    @Override
    public void onLocationChanged(Location location) {
        this.gpsLocationResult = location;
        this.setValue(GpsFormatter.format(location)); //must save in format: "\d+, \d+, Alt: \d+, Acc: \d+"

        showResults();

        showLoadingDialog(null, false);

        afterUserInput();
        onCancelGpsDetection();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        showLoadingDialog(null, false);
    }

    @Override
    public void onProviderDisabled(String provider) {

        showLoadingDialog(null, false);
    }

}
