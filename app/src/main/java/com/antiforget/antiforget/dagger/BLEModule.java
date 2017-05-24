package com.antiforget.antiforget.dagger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module(includes = {AppModule.class})
public class BLEModule {

    @Provides
    BluetoothAdapter providesAdapter(Context context){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
         return bluetoothManager.getAdapter();
    }
}
