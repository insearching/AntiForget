package com.antiforget.antiforget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.antiforget.antiforget.MainActivity.MAC_ADDRESS;
import static com.antiforget.antiforget.MainActivity.START_BLE_SEARCH;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent mServiceIntent = new Intent(context, BLESearchService.class);
            mServiceIntent.putExtra(START_BLE_SEARCH, MAC_ADDRESS);

//            BLEAlarm.scheduleOnAlarm(context, mServiceIntent);
        }
    }
}
