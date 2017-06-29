package com.antiforget.antiforget;

import android.app.Application;

import com.antiforget.antiforget.dagger.AntiForgetComponent;
import com.antiforget.antiforget.dagger.AppModule;
import com.antiforget.antiforget.dagger.DaggerAntiForgetComponent;

import timber.log.Timber;


public class AntiForgetApplication extends Application {

    AntiForgetComponent component;

    public static final String BLE_MAC = "5C:F8:21:DD:CE:41";

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAntiForgetComponent.builder()
                .appModule(new AppModule(this))
                .build();

        Timber.plant(new Timber.DebugTree());

        Timber.plant(new LogTree());
    }
    protected AntiForgetComponent getAppComponent() {
        return component;
    }
}
