package com.antiforget.antiforget.dagger;

import android.content.Context;

import com.antiforget.antiforget.AntiForgetApplication;
import com.antiforget.antiforget.ISharedPrefsHelper;
import com.antiforget.antiforget.SharedPrefsHelper;

import dagger.Module;
import dagger.Provides;


@Module
public class AppModule {

    AntiForgetApplication application;

    public AppModule(AntiForgetApplication application) {
        this.application = application;
    }

    @Provides
    @AppScope
    Context providesContext() {
        return application;
    }

    @Provides
    @AppScope
    ISharedPrefsHelper providesSharedHelper(Context context) {
        return new SharedPrefsHelper(context);
    }
}
