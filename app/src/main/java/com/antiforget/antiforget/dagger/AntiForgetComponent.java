package com.antiforget.antiforget.dagger;

import com.antiforget.antiforget.BLESearchService;
import com.antiforget.antiforget.MainActivity;

import dagger.Component;

@Component(modules = BLEModule.class)
@AppScope
public interface AntiForgetComponent {
    void inject(MainActivity activity);

    void inject(BLESearchService service);
}
