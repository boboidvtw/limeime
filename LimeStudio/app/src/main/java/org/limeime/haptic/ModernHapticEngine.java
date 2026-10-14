/*
 *
 *  *    Copyright 2026, The LimeIME Open Source Project
 *  *
 *  *    Project Url: http://github.com/lime-ime/limeime/
 *  *
 *  *    This program is free software: you can redistribute it and/or modify
 *  *    it under the terms of the GNU General Public License as published by
 *  *    the Free Software Foundation, either version 3 of the License, or
 *  *    (at your option) any later version.
 *  *
 *  *    This program is distributed in the hope that it will be useful,
 *  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *    GNU General Public License for more details.
 *
 */

package org.limeime.haptic;

import android.content.Context;
import android.os.Build;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;

import org.limeime.KeypressHapticPolicy;

/**
 * ModernHapticEngine: Delivers crisp, tactile, multi-level haptic feedback
 * across Android 8.0 (API 26) through Android 15+ (API 35+).
 *
 * Supports:
 * - Linear Resonant Actuator (LRA) amplitude scaling (levels 1-5).
 * - Predefined tactile effects (EFFECT_CLICK, EFFECT_TICK, EFFECT_HEAVY_CLICK) on API 29+.
 * - Android 13+ USAGE_TOUCH vibration attributes.
 * - System KEYBOARD_TAP fallback on supported hardware.
 */
public final class ModernHapticEngine {

    private static final String TAG = "ModernHapticEngine";
    private static volatile ModernHapticEngine sInstance;

    private final Context mContext;
    private Vibrator mVibrator;

    // Amplitude mapping for level 1 to 5 (0 = off)
    private static final int[] LEVEL_AMPLITUDES = {0, 35, 75, 130, 190, 255};
    // Duration in milliseconds for level 1 to 5
    private static final long[] LEVEL_DURATIONS = {0, 8, 11, 14, 18, 22};

    private ModernHapticEngine(Context context) {
        mContext = context.getApplicationContext();
        initVibrator();
    }

    public static ModernHapticEngine getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ModernHapticEngine.class) {
                if (sInstance == null) {
                    sInstance = new ModernHapticEngine(context);
                }
            }
        }
        return sInstance;
    }

    private void initVibrator() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) mContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) {
                    mVibrator = vm.getDefaultVibrator();
                }
            }
            if (mVibrator == null) {
                mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Vibrator: " + e.getMessage());
        }
    }

    public Vibrator getVibrator() {
        if (mVibrator == null) {
            initVibrator();
        }
        return mVibrator;
    }

    /**
     * Triggers keypress haptic feedback according to device capability and user level.
     *
     * @param inputView     The active input view, can be null.
     * @param vibrateLevel  1 (extra weak) to 5 (extra strong), or <= 0 for disabled.
     * @param legacyDuration Milliseconds duration fallback.
     */
    @SuppressWarnings("deprecation")
    public void performKeypressHaptic(View inputView, int vibrateLevel, long legacyDuration) {
        if (vibrateLevel <= 0 && legacyDuration <= 0) {
            return;
        }

        // 1. Try system keyboard tap if policy recommends and view is available
        if (KeypressHapticPolicy.shouldUseSystemKeyboardTapHaptic() && inputView != null) {
            try {
                int flags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    flags |= HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
                }
                boolean handled = inputView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, flags);
                if (handled) {
                    return;
                }
            } catch (Exception e) {
                Log.w(TAG, "performHapticFeedback failed: " + e.getMessage());
            }
        }

        Vibrator vibrator = getVibrator();
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        int clampedLevel = Math.max(1, Math.min(5, vibrateLevel > 0 ? vibrateLevel : 3));
        long duration = legacyDuration > 0 ? legacyDuration : LEVEL_DURATIONS[clampedLevel];
        int amplitude = LEVEL_AMPLITUDES[clampedLevel];

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                performApi33Haptic(vibrator, clampedLevel, duration, amplitude);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                performApi29Haptic(vibrator, clampedLevel, duration, amplitude);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                performApi26Haptic(vibrator, duration, amplitude);
            } else {
                vibrator.vibrate(duration);
            }
        } catch (Exception e) {
            Log.e(TAG, "Vibration execution failed: " + e.getMessage());
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private void performApi33Haptic(Vibrator vibrator, int level, long duration, int amplitude) {
        VibrationAttributes attributes = new VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_TOUCH)
                .build();

        VibrationEffect effect = createScaledEffect(vibrator, level, duration, amplitude);
        vibrator.vibrate(effect, attributes);
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
    private void performApi29Haptic(Vibrator vibrator, int level, long duration, int amplitude) {
        VibrationEffect effect = createScaledEffect(vibrator, level, duration, amplitude);
        vibrator.vibrate(effect);
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    private void performApi26Haptic(Vibrator vibrator, long duration, int amplitude) {
        int amp = vibrator.hasAmplitudeControl() ? amplitude : VibrationEffect.DEFAULT_AMPLITUDE;
        VibrationEffect effect = VibrationEffect.createOneShot(duration, amp);
        vibrator.vibrate(effect);
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
    private VibrationEffect createScaledEffect(Vibrator vibrator, int level, long duration, int amplitude) {
        if (vibrator.hasAmplitudeControl()) {
            return VibrationEffect.createOneShot(duration, amplitude);
        }
        // Fallback to predefined click/tick if amplitude control is not supported
        if (level <= 1) {
            return VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK);
        } else if (level >= 4) {
            return VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK);
        } else {
            return VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
        }
    }
}
