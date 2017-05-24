package com.antiforget.antiforget;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_LOCATION_PERMISSION = 1000;
    public static final int REQUEST_ENABLE_BT = 2000;

    public static final String ENABLE_BT = "enable_bt";

    public static final String MAC_ADDRESS = "5C:F8:21:DD:CE:41";
    public static final String START_BLE_SEARCH = "start_search";
    public static final String STOP_BLE_SEARCH = "stop_search";
    public static final String FORGOT_REASON = "forgot_reason";

    @Inject
    BluetoothAdapter mBluetoothAdapter;
    private Intent mStartIntent;
    private Intent mStopIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((AntiForgetApplication) getApplication()).getAppComponent().inject(this);

        mStartIntent = new Intent(this, BLESearchService.class);
        mStartIntent.setAction(START_BLE_SEARCH);
        mStartIntent.putExtra(START_BLE_SEARCH, MAC_ADDRESS);

        mStopIntent = new Intent(this, BLESearchService.class);
        mStopIntent.setAction(STOP_BLE_SEARCH);
        mStopIntent.putExtra(STOP_BLE_SEARCH, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermissions();
        }

        if(getIntent().hasExtra(FORGOT_REASON)){
            ((TextView)findViewById(R.id.message)).setText(getIntent().getStringExtra(FORGOT_REASON));
        }
    }

    private void checkBLE() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT);
        }
        else {
            BLEAlarm.scheduleOnAlarm(this, mStartIntent);
            BLEAlarm.scheduleOffAlarm(this, mStopIntent);

//            startService(mStartIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode
                && REQUEST_ENABLE_BT == requestCode) {
            startService(mStartIntent);
        }
        else {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkBLE();
                } else {
                    finish();
                }
            }
            break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkLocationPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            checkBLE();
        }
    }
}
