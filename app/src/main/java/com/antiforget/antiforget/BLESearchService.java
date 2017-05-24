package com.antiforget.antiforget;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;


public class BLESearchService extends IntentService {


    private static final long SEARCH_DELAY = 60 * 1000;
    private static final long NO_DEVICE_DELAY = 40 * 1000;

    @Inject
    BluetoothAdapter mBluetoothAdapter;

    private String macAddress;

    PublishSubject<Long> subject;

    public BLESearchService() {
        super("BLESearchService");
    }

    public BLESearchService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((AntiForgetApplication) getApplication()).getAppComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Timber.i("onHandleIntent");
        if (intent.hasExtra(MainActivity.START_BLE_SEARCH)) {
            macAddress = intent.getStringExtra(MainActivity.START_BLE_SEARCH);
            checkBLE();
        } else if (intent.hasExtra(MainActivity.STOP_BLE_SEARCH)) {
            if (subject != null && !subject.hasComplete())
                subject.onComplete();

            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            timer.dispose();
            stopSelf();
        }

        if (intent.hasExtra("ALARM"))
            Timber.i("Started from alarm!");
    }


    private void checkBLE() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(this, MainActivity.class);
            enableBtIntent.putExtra(MainActivity.ENABLE_BT, true);
            pushNotification(enableBtIntent, getString(R.string.enable_bt), true);
        } else {
            startBLEScan();
        }
    }

    private void pushNotification(Intent intent, String message, boolean vibrate) {
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setVibrate(vibrate ? new long[]{0, 700, 50, 500, 50, 500, 50, 500, 50, 1000} : null)
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
                    Timber.i("Time - " + timePast * NO_DEVICE_DELAY / 1000  + " secs.");
                    remind(Long.valueOf(timePast).intValue());
                },
                error -> {
                    Timber.e(error);
                    postNewScan(50l);
                },
                () -> {
                    Timber.i("Device found!");
                    postNewScan(SEARCH_DELAY);
                    timerDisposable.dispose();
                    lastTick = new AtomicLong(1L);
                });
    }


    private void remind(int periods) {
        boolean vibrate = periods >= 3;
        String message = getString(R.string.no_device_message, periods * NO_DEVICE_DELAY / 1000);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.FORGOT_REASON, message);
        pushNotification(intent, message, vibrate);
    }


    Disposable timerDisposable;

    private AtomicLong lastTick = new AtomicLong(1L);


    private void startBLEScan() {
        Timber.i("Start scanning...");
        subscribeObserver();
        mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);

        timerDisposable = Observable.interval(NO_DEVICE_DELAY, TimeUnit.MILLISECONDS)
                .timeInterval()
                .map(longTimed -> lastTick.getAndIncrement())
                .take(5)
                .subscribe(period -> {
                    Timber.i("Time past period " + period);
                    subject.onNext(period);
                });
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            Timber.i("MAC:" + result.getDevice().getAddress());
            if (result.getDevice().getAddress().equals(macAddress)) {
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

    Disposable timer;
    private void postNewScan(long delay) {
        Timber.i("Stopping BLE search");
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        Timber.i("Restarting in " + delay / 1000 + " seconds.");
        timer = Observable.empty()
                .delay(delay, TimeUnit.MILLISECONDS)
                .doOnComplete(() -> checkBLE())
                .subscribe();
    }
}
