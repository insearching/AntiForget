package com.antiforget.antiforget;

import android.app.Application;

import com.antiforget.antiforget.dagger.AntiForgetComponent;
import com.antiforget.antiforget.dagger.AppModule;
import com.antiforget.antiforget.dagger.DaggerAntiForgetComponent;

import timber.log.Timber;


public class AntiForgetApplication extends Application {

    AntiForgetComponent component;
    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAntiForgetComponent.builder()
                .appModule(new AppModule(this))
                .build();

        Timber.plant(new Timber.DebugTree());
    }

    protected AntiForgetComponent getAppComponent() {
        return component;
    }
}
