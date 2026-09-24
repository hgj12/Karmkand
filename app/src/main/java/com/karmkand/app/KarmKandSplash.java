package com.karmkand.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

@SuppressLint("CustomSplashScreen")
public class KarmKandSplash extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;
    private Handler handler;
    private Runnable navigateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ✅ FIX 1 — installSplashScreen() BEFORE super.onCreate()
        // This is the correct order — calling it after super causes 1 frame flash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

            // ✅ FIX 2 — always return false immediately
            // Returning false tells system "dismiss right now"
            splashScreen.setKeepOnScreenCondition(() -> false);

            // ✅ FIX 3 — remove exit animation so no visual overlap
            splashScreen.setOnExitAnimationListener(
                    splashScreenView -> splashScreenView.remove()
            );
        }

        super.onCreate(savedInstanceState); // ← super AFTER installSplashScreen

        // ✅ FIX 4 — make window background match your splash
        // Prevents any white/blank frame between system splash and your layout
        getWindow().setBackgroundDrawableResource(R.drawable.bg_spiritual_gradient);

        setContentView(R.layout.splash);

        // ✅ FIX 5 — full screen flags
        makeFullScreen();

        // Navigate after delay
        handler = new Handler(Looper.getMainLooper());
        navigateRunnable = () -> {
            if (!isFinishing() && !isDestroyed()) {
                startActivity(new Intent(KarmKandSplash.this, KarmKandMenu.class));
                overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );
                finish();
            }
        };
        handler.postDelayed(navigateRunnable, SPLASH_DELAY);
    }

    private void makeFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            // Android below 11
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ✅ Prevent memory leak
        if (handler != null && navigateRunnable != null) {
            handler.removeCallbacks(navigateRunnable);
        }
    }

}