package com.antiforget.antiforget;

public interface ISharedPrefsHelper {

    void setNoDeviceState(boolean flag);

    boolean isNoDevice();

    void clearAllMocks();

    void setSearchInterval(int interval);

    long getSearchInterval();
}
