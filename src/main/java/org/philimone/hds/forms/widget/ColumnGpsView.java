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
import org.philimone.hds.forms.listeners.GpsPermissionListener;
import org.philimone.hds.forms.main.FormActivity;
import org.philimone.hds.forms.model.Column;
import org.philimone.hds.forms.widget.dialog.DialogFactory;
import org.philimone.hds.forms.widget.dialog.LoadingDialog;

import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ColumnGpsView extends ColumnView implements LocationListener, GpsPermissionListener {

    private TextView txtName;
    private Button btGetGps;
    private TextView txtGpsLatitude;
    private TextView txtGpsLongitude;
    private TextView txtGpsAltitude;
    private TextView txtGpsAccuracy;
    private Location gpsLocationResult;

    private LocationManager locationManager;
    private LoadingDialog loadingDialog;


    public ColumnGpsView(Context context, @Nullable AttributeSet attrs, @NonNull Column column) {
        super(context, R.layout.column_gps_item, attrs, column);

        initialize();        ;
    }

    public ColumnGpsView(Context context, @NonNull Column column) {
        this(context, null, column);
    }

    private void initialize(){

        if (this.mContext instanceof FormActivity) {
            ((FormActivity) this.mContext).setPermissionListener(this);
        }

        createView();
    }

    private void createView() {

        this.txtColumnRequired = findViewById(R.id.txtColumnRequired);
        this.txtName = findViewById(R.id.txtColumnName);
        this.btGetGps = findViewById(R.id.btGetGps);
        this.txtGpsLatitude = findViewById(R.id.txtGpsLatitude);
        this.txtGpsLongitude = findViewById(R.id.txtGpsLongitude);
        this.txtGpsAltitude = findViewById(R.id.txtGpsAltitude);
        this.txtGpsAccuracy = findViewById(R.id.txtGpsAccuracy);

        this.loadingDialog = new LoadingDialog(this.mContext);

        txtColumnRequired.setVisibility(this.column.isRequired() ? VISIBLE : GONE);
        txtName.setText(column.getLabel());

        clearGpsResultTexts();

        this.btGetGps.setOnClickListener(v -> onGetGpsClicked());

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
        showLoadingDialog(this.mContext.getString(msgResId), show);
    }

    private void showLoadingDialog(String msg, boolean show) {
        if (show) {
            this.loadingDialog.setMessage(msg);
            this.loadingDialog.show();
        } else {
            this.loadingDialog.hide();
        }
    }

    private boolean ensurePermissionsGranted(final String... permissions) {

        boolean denied = false;
        for (String permission : permissions) {
            denied = denied || ContextCompat.checkSelfPermission(this.getContext(), permission) == PackageManager.PERMISSION_DENIED;
        }

        if (denied) { //without access
            //request permissions
            ActivityCompat.requestPermissions(this.getActivity(), permissions, FormActivity.REQUEST_GPS_PERMISSION);
        } else {
            detectGpsLocation();
        }

        return !denied;
    }

    private void onGetGpsClicked() {
        ensurePermissionsGranted(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void detectGpsLocation() {

        if (ActivityCompat.checkSelfPermission(this.mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            DialogFactory.createMessageInfo(this.mContext, R.string.gps_title_lbl, R.string.gps_permissions_error).show();
            return;
        }

        this.locationManager = this.locationManager == null ? (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE) : locationManager;

        boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        String provider = gps_enabled ? LocationManager.GPS_PROVIDER : network_enabled ? LocationManager.NETWORK_PROVIDER : "";

        if (provider.isEmpty()) {
            //No provider available
            DialogFactory.createMessageInfo(this.mContext, R.string.gps_title_lbl, R.string.gps_no_provider_available_error).show();
            return;
        }

        this.gpsLocationResult = null;

        locationManager.requestLocationUpdates(provider, 5, 0, this);
        showLoadingDialog(R.string.gps_loading_lbl, true);
    }

    @Override
    public String getValue() {
        return this.gpsLocationResult.toString();
    }

    public Map<String, Double> getValues(){
        Map<String, Double> map = new LinkedHashMap<>();

        if (gpsLocationResult != null) {
            String name = this.column.getName();

            map.put(name+"_lat", gpsLocationResult.getLatitude());
            map.put(name+"_lon", gpsLocationResult.getLongitude());
            map.put(name+"_alt", gpsLocationResult.getAltitude());
            map.put(name+"_acc", gpsLocationResult.getAccuracy()*1d);
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

    @Override
    public void onGpsPermissionGranted() {
        detectGpsLocation();
    }

    @Override
    public void onGpsPermissionDenied() {
        DialogFactory.createMessageInfo(this.mContext, R.string.gps_title_lbl, R.string.gps_permissions_error).show();
    }
}
