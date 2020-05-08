package com.mtramin.rxfingerprint;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

/**
 * Created by Gbenga Oladipupo on 07/05/2020.
 */


public class UIThreadExecutor implements Executor {
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static UIThreadExecutor get(){
        return new UIThreadExecutor();
    }

    @Override
    public void execute(@NonNull Runnable runnable) {
        handler.post(runnable);
    }
}
