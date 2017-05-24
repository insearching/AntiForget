package com.antiforget.antiforget.dagger;

import android.content.Context;

import com.antiforget.antiforget.AntiForgetApplication;

import dagger.Module;
import dagger.Provides;


@Module
public class AppModule {

    AntiForgetApplication application;
    public AppModule(AntiForgetApplication application) {
        this.application = application;
    }

    @Provides
    Context providesContext(){
        return application;
    }
}
