package com.antiforget.antiforget;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.jraska.console.Console;

import java.util.Calendar;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.antiforget.antiforget.AntiForgetApplication.BLE_MAC;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_LOCATION_PERMISSION = 1000;

    public static final int REQUEST_ENABLE_BT = 2000;

    public static final String ENABLE_BT = "enable_bt";

    public static final String MAC_ADDRESS_KEY = "mac_address";

    private static final String START_BLE_SEARCH = "Start search";

    private static final String STOP_BLE_SEARCH = "Stop search";

    @Inject
    @Nullable
    private BluetoothAdapter mBluetoothAdapter;

    @Inject
    private ISharedPrefsHelper mPrefsHelper;

    enum Alarm {
        START(START_BLE_SEARCH, 1000),
        STOP(STOP_BLE_SEARCH, 2000);

        private final String text;
        private final int requestCode;

        Alarm(final String text, int requestCode) {
            this.text = text;
            this.requestCode = requestCode;
        }

        @Override
        public String toString() {
            return text;
        }

        public int getCode() {
            return requestCode;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((AntiForgetApplication) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.start_search)
    public void onStartClick() {
        startCheck();
    }

    @OnClick(R.id.stop_search)
    public void onStopClick() {
        startService(getBLEIntent(Alarm.STOP));
    }

    @OnClick(R.id.start_alarm)
    public void setStartAlarm() {
        setTime(Alarm.START);
    }

    @OnClick(R.id.stop_alarm)
    public void onCancelAlarm() {
        setTime(Alarm.STOP);
    }

    @OnClick(R.id.no_device_mock)
    public void onNoDeviceMock() {
        mPrefsHelper.setNoDeviceState(true);
        Toast.makeText(this, "No device mock set", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.clear_mock)
    public void onClearMock() {
        mPrefsHelper.clearAllMocks();
        Console.clear();
        Toast.makeText(this, "Cleared all mocks", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.search_interval)
    public void onSearchInterval() {
        final View view = getLayoutInflater().inflate(R.layout.dialog_interval_input, null);
        final EditText input = (EditText) view.findViewById(R.id.interval_field);
        new AlertDialog.Builder(this)
                .setView(view)
                .setTitle(R.string.enter_search_interval)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String str = input.getText().toString();
                    if (str == null) return;

                    try {
                        int interval = Integer.parseInt(str);
                        mPrefsHelper.setSearchInterval(interval);
                    } catch (NumberFormatException ex) {
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @OnClick(R.id.clear_interval)
    public void clearSearchInterval() {
        mPrefsHelper.setSearchInterval(0);
        Toast.makeText(this, "Interval set back to default", Toast.LENGTH_LONG).show();
    }

    private void startCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermissions();
        } else {
            checkBLE();
        }
    }

    private void setTime(final Alarm alarmType) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
            Calendar selectedTime = Calendar.getInstance();
            selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour);
            selectedTime.set(Calendar.MINUTE, selectedMinute);
            selectedTime.set(Calendar.SECOND, 0);
            if (selectedTime.before(now)) {
                selectedTime.add(Calendar.DAY_OF_MONTH, 1);
            }

            BLEAlarm.getInstance().scheduleAlarm(this, getBLEIntent(alarmType), selectedTime, alarmType);

        }, hour, minute, true);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private void checkBLE() {
        if (mBluetoothAdapter == null) {
            Timber.i("Bluetooth is disabled!");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT);
        } else {
            startService(getBLEIntent(Alarm.START));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode
                && REQUEST_ENABLE_BT == requestCode) {
            startService(getBLEIntent(Alarm.START));
        } else {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkBLE();
                } else {
                    checkLocationPermissions();
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

    private Intent getBLEIntent(final Alarm action) {
        Intent intent = new Intent(this, BLESearchService.class);
        intent.setAction(action.toString());
        if (Alarm.START.equals(action)) {
            intent.putExtra(MAC_ADDRESS_KEY, BLE_MAC);
        }
        return intent;
    }
}
