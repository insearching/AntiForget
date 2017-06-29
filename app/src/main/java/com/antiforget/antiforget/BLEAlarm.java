package com.antiforget.antiforget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import timber.log.Timber;

import static android.content.Context.ALARM_SERVICE;
import static com.antiforget.antiforget.BLESearchService.STARTED_FROM_ALARM;

public class BLEAlarm {

    private static BLEAlarm instance;

    static final String ACTION_ALARM_RECEIVER = "alarm_receiver_action";

    private BLEAlarm() {
    }

    public static BLEAlarm getInstance() {
        if (instance == null)
            instance = new BLEAlarm();
        return instance;
    }

    public void scheduleAlarm(Context context, Intent intent,
                              Calendar time, MainActivity.Alarm alarmType) {
        intent.putExtra(STARTED_FROM_ALARM, true);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(context, alarmType.getCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                time.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        Timber.i(alarmType.toString() + " alarm scheduled for " + time.getTime().toString());
        isAlarmSet(context, intent, alarmType.getCode());
    }

//
//    public void scheduleOnAlarm(Context context, Intent intent, Calendar time) {
//        intent.putExtra(STARTED_FROM_ALARM, true);
//        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
//        PendingIntent alarmIntent = PendingIntent.getService(context, START_ALARM_CODE, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
//                time.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
//
//        Timber.i("Start alarm scheduled for " + time.getTime().toString());
//        isAlarmSet(context, intent, START_ALARM_CODE);
//    }
//
//    public void scheduleOffAlarm(Context context, Intent intent, Calendar time) {
//        intent.putExtra(STOPPED_FROM_ALARM, true);
//        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
//        PendingIntent alarmIntent = PendingIntent.getService(context, STOP_ALARM_CODE, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
//                time.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
//
//        Timber.i("Stop alarm scheduled for " + time.getTime().toString());
//        isAlarmSet(context, intent, STOP_ALARM_CODE);
//    }

    private boolean isAlarmSet(Context context, Intent intent, int requestCode) {
        boolean isWorking = (PendingIntent.getService(context,
                requestCode, intent, PendingIntent.FLAG_NO_CREATE) != null);
        Timber.i("Alarm is " + (isWorking ? "" : "not ") + "working...");
        return isWorking;
    }
}
