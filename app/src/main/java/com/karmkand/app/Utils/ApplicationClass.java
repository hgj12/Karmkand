package com.karmkand.app.Utils;

import android.app.Activity;
import android.app.Application;

public class ApplicationClass extends Application {

    private static final String TAG = "ApplicationClass";

    private static ApplicationClass sInstance;

    private static Utility utility = null;

    @Override
    public void onCreate() {
        super.onCreate();

        LocaleManager.applySavedLanguage(this);

        sInstance = this;
        utility = Utility.getInstance();
        utility.init(this);
        LocaleStringHelper.init(this);
    }

    /**
     * @return ApplicationClass singleton instance
     */
    public synchronized static ApplicationClass getInstance() {
        return sInstance;
    }
}
