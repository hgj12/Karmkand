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
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

@SuppressLint("CustomSplashScreen")
public class KarmKandSplash extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500;
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

        // ✅ FIX 5 — safe area insets handling
        // Ensures bottom headline & status bar/notch are never clipped across any device
        View splashRoot = findViewById(R.id.splashRoot);
        if (splashRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(splashRoot, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                );
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return windowInsets;
            });
        }

        // ✅ FIX 6 — full screen / light system bars
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
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (controller != null) {
                controller.setAppearanceLightStatusBars(true);
                controller.setAppearanceLightNavigationBars(true);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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