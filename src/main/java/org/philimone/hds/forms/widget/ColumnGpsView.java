package org.philimone.hds.forms.widget;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.philimone.hds.forms.R;
import org.philimone.hds.forms.main.FormFragment;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.widget.dialog.DialogFactory;
import org.philimone.hds.forms.widget.dialog.LoadingDialog;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ColumnGpsView extends ColumnView implements LocationListener {

    private TextView txtName;
    private Button btGetGps;
    private TextView txtGpsLatitude;
    private TextView txtGpsLongitude;
    private TextView txtGpsAltitude;
    private TextView txtGpsAccuracy;
    private Location gpsLocationResult;

    private LocationManager locationManager;
    private LoadingDialog loadingDialog;

    private ActivityResultLauncher<String[]> requestPermissions;

    public ColumnGpsView(ColumnGroupView view, @Nullable AttributeSet attrs, @NonNull Column column) {
        super(view, R.layout.column_gps_item, attrs, column);

        initialize();        ;
    }

    public ColumnGpsView(ColumnGroupView view, @NonNull Column column) {
        this(view, null, column);
    }

    private void initialize(){
        createView();
        initPermissions();
    }

    private void initPermissions() {
        this.requestPermissions = this.getActivity().registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionResults -> {
            boolean granted = !permissionResults.values().contains(false);

            if (granted) {
                detectGpsLocation();
            } else {
                DialogFactory.createMessageInfo(this.getContext(), R.string.gps_title_lbl, R.string.gps_permissions_error).show();
            }
        });
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btGetGps = findViewById(R.id.btGetGps);
        this.txtGpsLatitude = findViewById(R.id.txtGpsLatitude);
        this.txtGpsLongitude = findViewById(R.id.txtGpsLongitude);
        this.txtGpsAltitude = findViewById(R.id.txtGpsAltitude);
        this.txtGpsAccuracy = findViewById(R.id.txtGpsAccuracy);

        this.loadingDialog = new LoadingDialog(this.getContext());

        this.btGetGps.setOnClickListener(v -> onGetGpsClicked());

        updateValues();
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

        if (denied) { //without access
            requestPermissions.launch(permissions);
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
            //No provider available
            DialogFactory.createMessageInfo(this.getContext(), R.string.gps_title_lbl, R.string.gps_no_provider_available_error).show();
            return;
        }

        this.gpsLocationResult = null;

        locationManager.requestLocationUpdates(provider, 5, 0, this);
        showLoadingDialog(R.string.gps_loading_lbl, true);
    }

    @Override
    public void updateValues() {
        clearGpsResultTexts();

        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        txtName.setText(column.getLabel());

        showResults();
    }

    @Override
    public void setValue(String value) { //do nothing
        //this.column.setValue(value);
        //updateValues();
    }

    public void setValues(Map<String,Double> gpsValues) {
        if (gpsValues == null) return;

        this.gpsLocationResult = new Location("fake");

        Double lat = gpsValues.get(column.getName()+"Lat");
        Double lon = gpsValues.get(column.getName()+"Lon");
        Double alt = gpsValues.get(column.getName()+"Alt");
        Double acc = gpsValues.get(column.getName()+"Acc");

        if (lat != null) this.gpsLocationResult.setLatitude(lat);
        if (lon != null) this.gpsLocationResult.setLongitude(lon);
        if (alt != null) this.gpsLocationResult.setAltitude(alt);
        if (acc != null) this.gpsLocationResult.setAccuracy((float) (acc*1F));

        updateValues();
    }

    @Override
    public String getValue() {
        Location gps = this.gpsLocationResult;
        if (gps != null){
            return gps.getLatitude()+";"+gps.getLongitude()+";"+gps.getAltitude()+";"+gps.getAccuracy();
        }

        return null;
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
        showLoadingDialog(null, false);

        this.gpsLocationResult = location;

        showResults();

        showLoadingDialog(null, false);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("megps", ""+provider+", status="+status);
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
