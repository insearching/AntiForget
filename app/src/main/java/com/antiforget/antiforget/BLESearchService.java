package com.antiforget.antiforget;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;


public class BLESearchService extends Service {

    private static final long SEARCH_DELAY = 60 * 1000;

    private static final long NO_DEVICE_DELAY = 40 * 1000;

    private static final int IMPORTANT_PERIOD = 2;

    public static final String STARTED_FROM_ALARM = "started_from_alarm";

    public static final String STOPPED_FROM_ALARM = "stopped_from_alarm";

    private static final long[] VIBRATE_RATE
            = new long[]{0, 700, 50, 500, 50, 500, 50, 500, 50, 1000};

    @Inject
    @Nullable
    BluetoothAdapter mBluetoothAdapter;

    @Inject
    ISharedPrefsHelper mPrefsHelper;

    String macAddress;

    PublishSubject<Long> subject;

    Disposable noDeviceTimer;

    Disposable searchTimer;

    AtomicLong lastTick = new AtomicLong(1L);

    @Override
    public void onCreate() {
        ((AntiForgetApplication) getApplication()).getAppComponent().inject(this);

        super.onCreate();

        Timber.i("onCreate");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Timber.i("onStartCommand");

        if (intent != null)
            handIntent(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Timber.i("onDestroy");

        mPrefsHelper.clearAllMocks();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handIntent(Intent intent) {
        if (intent.getAction().equals(MainActivity.Alarm.START.toString())) {
            macAddress = intent.getStringExtra(MainActivity.MAC_ADDRESS_KEY);
            performBleCheckAndStartScanning(intent.hasExtra(STARTED_FROM_ALARM));
        } else if (intent.getAction().equals(MainActivity.Alarm.STOP.toString())) {
            if (subject != null && !subject.hasComplete())
                subject.onComplete();

            if (mBluetoothAdapter != null)
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);

            if (searchTimer != null && !searchTimer.isDisposed())
                searchTimer.dispose();

            stopSelf();
            Timber.i("Scanning stopped.");

            if (intent.hasExtra(STOPPED_FROM_ALARM))
                pushNotification(intent, getString(R.string.stopped_from_alarm), false);
        }
    }

    private void performBleCheckAndStartScanning() {
        performBleCheckAndStartScanning(false);
    }

    private void performBleCheckAndStartScanning(boolean startedFromAlarm) {
        if (mBluetoothAdapter == null) {
            Timber.i("Bluetooth adapter is missing!");
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(this, MainActivity.class);
            enableBtIntent.putExtra(MainActivity.ENABLE_BT, true);
            pushNotification(enableBtIntent, getString(R.string.enable_bt), true);
        } else {
            startBLEScan(startedFromAlarm);
        }
    }

    private void pushNotification(Intent intent, String message, boolean vibrate) {
        Timber.i("Pushing notification with message: " + message);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setVibrate(vibrate ? VIBRATE_RATE : null)
                .setLights(Color.RED, 3000, 3000)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }

    private void subscribeObserver() {
        subject = PublishSubject.create();
        subject.subscribe(
                timePast -> {
                    Timber.i("Time - " + timePast * NO_DEVICE_DELAY / 1000 + " secs.");
                    showNoDeviceMessage(Long.valueOf(timePast).intValue());
                },
                error -> {
                    Timber.e(error);
                    postNewScan(50l);
                },
                () -> {
                    postNewScan(getSearchDelay());
                    noDeviceTimer.dispose();
                    lastTick = new AtomicLong(1L);
                });
    }

    private void showNoDeviceMessage(int periods) {
        restartBLESearch();
        if (periods > IMPORTANT_PERIOD) {
            String message = getString(R.string.no_device_message, periods * NO_DEVICE_DELAY / 1000);

            Intent intent = new Intent(this, MainActivity.class);
            pushNotification(intent, message, true);
        }
    }

    private void restartBLESearch() {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
    }

    private void showStartSearchFromAlarmMessage() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(STARTED_FROM_ALARM);
        pushNotification(intent, getString(R.string.started_from_alarm), false);
    }

    private void showStopSearchMessage() {
        Intent intent = new Intent(this, MainActivity.class);
        pushNotification(intent, getString(R.string.search_stopped), false);
    }

    private long getSearchDelay() {
        long interval = mPrefsHelper.getSearchInterval();
        if (interval == 0) {
            return SEARCH_DELAY;
        } else {
            return interval;
        }
    }

    private void startBLEScan(boolean startedFromAlarm) {
        Timber.i("Start scanning...");

        if (startedFromAlarm)
            showStartSearchFromAlarmMessage();

        subscribeObserver();

        restartBLESearch();

        noDeviceTimer = Observable.interval(NO_DEVICE_DELAY, TimeUnit.MILLISECONDS)
                .timeInterval()
                .map(longTimed -> lastTick.getAndIncrement())
                .take(5)
                .doOnComplete(() -> showStopSearchMessage())
                .subscribe(period -> subject.onNext(period));
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            Timber.i("Device found: " + result.getDevice().getAddress());
            if (result.getDevice().getAddress().equals(macAddress) && !mPrefsHelper.isNoDevice()) {
                subject.onComplete();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            subject.onError(new Exception("Search failed with error code " + errorCode));
        }
    };

    private void postNewScan(long delay) {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        Timber.i("Restarting in " + delay / 1000 + " seconds.");
        searchTimer = Observable.empty()
                .delay(delay, TimeUnit.MILLISECONDS, Schedulers.io())
                .doOnComplete(() -> performBleCheckAndStartScanning())
                .subscribe();
    }
}