package com.cuongnd.sigmasolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

import com.cuongnd.sigmasolution.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {
    private static final String TAG = "MainActivity";
    CountDownTimer timerGPS;
    CountDownTimer timerBattery;
    private ActivityMainBinding activityMainBinding;
    LocationManager locationManager;
    String provider;
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        activityMainBinding.startBtn.setOnClickListener(this);
        activityMainBinding.stopBtn.setOnClickListener(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }

        timerGPS = new CountDownTimer(10000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "onTick : " + millisUntilFinished + "" + timerGPS.hashCode());
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "GPS : "+address);
                timerGPS.start();
            }
        }.start();

        timerBattery = new CountDownTimer(6000,3000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Log.d(TAG,"Battery : "+getBatteryPercentage(getApplicationContext()));
                timerBattery.start();
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!checkPermission()){
            requestPermission();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == activityMainBinding.stopBtn.getId()) {
            timerBattery.cancel();
            timerGPS.cancel();
        } else if (v.getId() == activityMainBinding.startBtn.getId()) {
            timerGPS.cancel();
            timerBattery.cancel();

            timerBattery.start();
            timerGPS.start();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        address = location.getLatitude() + " - " + location.getLongitude();
        Log.d(TAG, location.getLatitude() + " - " + location.getLongitude());
    }

    boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public static int getBatteryPercentage(Context context) {

        if (Build.VERSION.SDK_INT >= 21) {

            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        } else {

            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);

            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

            double batteryPct = level / (double) scale;

            return (int) (batteryPct * 100);
        }
    }
}
