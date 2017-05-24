package com.antiforget.antiforget.dagger;

import com.antiforget.antiforget.BLESearchService;
import com.antiforget.antiforget.MainActivity;

import dagger.Component;

/**
 * Created by insearching on 5/18/17.
 */

@Component(modules = {BLEModule.class})
public interface AntiForgetComponent {
    void inject (MainActivity activity);
    void inject (BLESearchService service);
}
