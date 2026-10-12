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

package org.limeime.keyboard;

import android.util.Log;

import org.limeime.LIMEService;
import org.limeime.global.LIMEPreferenceManager;

/**
 * KeyboardLayoutController: Manages dynamic switching between standard,
 * one-handed (left/right) and split phone keyboard modes at runtime.
 */
public final class KeyboardLayoutController {

    private static final String TAG = "KeyboardLayoutCtrl";

    private KeyboardLayoutController() {}

    /**
     * Cycles one-hand mode:
     * PORTRAIT_STANDARD -> PORTRAIT_ONE_HAND_RIGHT -> PORTRAIT_ONE_HAND_LEFT -> PORTRAIT_STANDARD
     *
     * @param service Active LIMEService instance.
     * @return Newly applied PhoneKeyboardModePolicy mode constant.
     */
    public static int cycleOneHandMode(LIMEService service) {
        if (service == null) {
            return PhoneKeyboardModePolicy.PORTRAIT_STANDARD;
        }

        try {
            LIMEPreferenceManager pref = new LIMEPreferenceManager(service);
            int currentMode = pref.getPhonePortraitKeyboardMode();
            int nextMode;

            switch (currentMode) {
                case PhoneKeyboardModePolicy.PORTRAIT_STANDARD:
                    nextMode = PhoneKeyboardModePolicy.PORTRAIT_ONE_HAND_RIGHT;
                    break;
                case PhoneKeyboardModePolicy.PORTRAIT_ONE_HAND_RIGHT:
                    nextMode = PhoneKeyboardModePolicy.PORTRAIT_ONE_HAND_LEFT;
                    break;
                case PhoneKeyboardModePolicy.PORTRAIT_ONE_HAND_LEFT:
                default:
                    nextMode = PhoneKeyboardModePolicy.PORTRAIT_STANDARD;
                    break;
            }

            pref.setPhonePortraitKeyboardMode(nextMode);
            service.reloadKeyboardLayout();
            return nextMode;
        } catch (Exception e) {
            Log.e(TAG, "cycleOneHandMode failed: " + e.getMessage());
            return PhoneKeyboardModePolicy.PORTRAIT_STANDARD;
        }
    }

    /**
     * Directly switches portrait keyboard layout mode and applies it immediately.
     */
    public static void setPortraitMode(LIMEService service, int mode) {
        if (service == null) return;
        try {
            LIMEPreferenceManager pref = new LIMEPreferenceManager(service);
            pref.setPhonePortraitKeyboardMode(mode);
            service.reloadKeyboardLayout();
        } catch (Exception e) {
            Log.e(TAG, "setPortraitMode failed: " + e.getMessage());
        }
    }
}
