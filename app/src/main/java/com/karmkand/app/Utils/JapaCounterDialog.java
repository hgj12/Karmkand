package com.karmkand.app.Utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;

import com.karmkand.app.R;

public class JapaCounterDialog {

    private final Context context;
    private final String mantraTitle;
    private int currentCount = 0;
    private int roundsCompleted = 0;
    private static final int TARGET_MALA = 108;

    public JapaCounterDialog(Context context, String mantraTitle) {
        this.context = context;
        this.mantraTitle = mantraTitle;
    }

    public void show() {
        Dialog dialog = new Dialog(context, R.style.SpiritualDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_japa_counter, null);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        AppCompatTextView txtJapaTitle = view.findViewById(R.id.txtJapaTitle);
        AppCompatTextView txtJapaTarget = view.findViewById(R.id.txtJapaTarget);
        AppCompatTextView txtJapaCount = view.findViewById(R.id.txtJapaCount);
        AppCompatTextView txtJapaProgress = view.findViewById(R.id.txtJapaProgress);
        AppCompatTextView txtJapaRounds = view.findViewById(R.id.txtJapaRounds);
        View btnJapaTap = view.findViewById(R.id.btnJapaTap);
        View btnJapaReset = view.findViewById(R.id.btnJapaReset);
        View btnJapaClose = view.findViewById(R.id.btnJapaClose);

        if (mantraTitle != null && !mantraTitle.isEmpty()) {
            txtJapaTitle.setText(mantraTitle);
        }

        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        btnJapaTap.setOnClickListener(v -> {
            currentCount++;
            vibrateTap(vibrator, 35);

            if (currentCount >= TARGET_MALA) {
                roundsCompleted++;
                currentCount = 0;
                vibrateComplete(vibrator);
                Toast.makeText(context, LocaleStringHelper.getString(context, R.string.japa_completed_msg), Toast.LENGTH_SHORT).show();
            }

            txtJapaCount.setText(toDevanagari(currentCount));
            txtJapaProgress.setText(String.format("१०८ में से %s जप", toDevanagari(currentCount)));
            txtJapaRounds.setText(String.format("माला चक्र: %s", toDevanagari(roundsCompleted)));
        });

        btnJapaReset.setOnClickListener(v -> {
            currentCount = 0;
            roundsCompleted = 0;
            txtJapaCount.setText("०");
            txtJapaProgress.setText("१०८ में से ० जप");
            txtJapaRounds.setText("माला चक्र: ०");
        });

        txtJapaCount.setText("०");
        btnJapaClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String toDevanagari(int number) {
        String numStr = String.valueOf(number);
        StringBuilder sb = new StringBuilder();
        for (char c : numStr.toCharArray()) {
            if (c >= '0' && c <= '9') {
                sb.append((char) ('\u0966' + (c - '0')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void vibrateTap(Vibrator vibrator, int ms) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        } catch (Exception ignored) {}
    }

    private void vibrateComplete(Vibrator vibrator) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        try {
            long[] pattern = {0, 100, 80, 150};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        } catch (Exception ignored) {}
    }
}
