package com.antiforget.antiforget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.joda.time.DateTime;

import java.util.Calendar;

import timber.log.Timber;

public class BLEAlarm {

    private static final int START_ALARM_CODE = 1000;
    private static final int STOP_ALARM_CODE = 2000;

    public static void scheduleOnAlarm(Context context, Intent intent) {
        boolean isSet = (PendingIntent.getBroadcast(context, START_ALARM_CODE,
                intent, PendingIntent.FLAG_NO_CREATE) != null);
        if (isSet) return;

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(context, START_ALARM_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);

        if(DateTime.now().isAfter(calendar.getTimeInMillis()))
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        Timber.i("Start alarm scheduled for " + calendar.getTime().toString());
    }

    public static void scheduleOffAlarm(Context context, Intent intent) {
        boolean isSet = (PendingIntent.getBroadcast(context, START_ALARM_CODE,
                intent, PendingIntent.FLAG_NO_CREATE) != null);
        if (isSet) return;

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(context, STOP_ALARM_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if(DateTime.now().isAfter(calendar.getTimeInMillis()))
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        Timber.i("Stop alarm scheduled for " + calendar.getTime().toString());
    }
}
