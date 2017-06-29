package com.antiforget.antiforget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import static com.antiforget.antiforget.AntiForgetApplication.BLE_MAC;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent mServiceIntent = new Intent(context, BLESearchService.class);
            mServiceIntent.putExtra(MainActivity.Alarm.START.toString(), BLE_MAC);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if (Calendar.getInstance().after(calendar)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            BLEAlarm.getInstance().scheduleAlarm(context, mServiceIntent,
                    calendar, MainActivity.Alarm.START);
        }
    }
}
