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

package org.limeime.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * LIMEClipboardManager: Autonomous manager for clipboard history,
 * clip observation and fast-paste injection into InputConnection.
 */
public final class LIMEClipboardManager {

    private static final String TAG = "LIMEClipboardManager";
    private static final int MAX_HISTORY_SIZE = 20;
    private static final int MAX_CLIP_TEXT_LENGTH = 4000;

    private static volatile LIMEClipboardManager sInstance;

    private final Context mContext;
    private ClipboardManager mSystemClipboard;
    private final LinkedList<String> mClipHistory = new LinkedList<>();
    private final List<OnClipboardUpdateListener> mListeners = new ArrayList<>();

    public interface OnClipboardUpdateListener {
        void onClipboardHistoryChanged(List<String> history);
    }

    private final ClipboardManager.OnPrimaryClipChangedListener mClipListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    captureCurrentPrimaryClip();
                }
            };

    private LIMEClipboardManager(Context context) {
        mContext = context.getApplicationContext();
        initSystemClipboard();
    }

    public static LIMEClipboardManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LIMEClipboardManager.class) {
                if (sInstance == null) {
                    sInstance = new LIMEClipboardManager(context);
                }
            }
        }
        return sInstance;
    }

    private void initSystemClipboard() {
        try {
            mSystemClipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (mSystemClipboard != null) {
                mSystemClipboard.addPrimaryClipChangedListener(mClipListener);
                captureCurrentPrimaryClip();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to initialize ClipboardManager: " + e.getMessage());
        }
    }

    public synchronized void captureCurrentPrimaryClip() {
        if (mSystemClipboard == null) return;
        try {
            if (!mSystemClipboard.hasPrimaryClip()) return;
            ClipData clip = mSystemClipboard.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) return;

            CharSequence text = clip.getItemAt(0).coerceToText(mContext);
            if (text == null) return;

            String str = text.toString().trim();
            if (str.isEmpty() || str.length() > MAX_CLIP_TEXT_LENGTH) return;

            // Remove if already present (move to top)
            mClipHistory.remove(str);
            mClipHistory.addFirst(str);

            while (mClipHistory.size() > MAX_HISTORY_SIZE) {
                mClipHistory.removeLast();
            }

            notifyListeners();
        } catch (Exception e) {
            Log.w(TAG, "Error capturing primary clip: " + e.getMessage());
        }
    }

    public synchronized List<String> getClipHistory() {
        return Collections.unmodifiableList(new ArrayList<>(mClipHistory));
    }

    public synchronized void clearHistory() {
        mClipHistory.clear();
        notifyListeners();
    }

    public synchronized void addListener(OnClipboardUpdateListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
            listener.onClipboardHistoryChanged(getClipHistory());
        }
    }

    public synchronized void removeListener(OnClipboardUpdateListener listener) {
        mListeners.remove(listener);
    }

    private void notifyListeners() {
        List<String> snapshot = getClipHistory();
        for (OnClipboardUpdateListener listener : mListeners) {
            try {
                listener.onClipboardHistoryChanged(snapshot);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying clipboard listener: " + e.getMessage());
            }
        }
    }

    /**
     * Pastes item by index directly to the target input connection.
     */
    public boolean pasteItem(InputConnection ic, int index) {
        if (ic == null) return false;
        String textToPaste = null;
        synchronized (this) {
            if (index >= 0 && index < mClipHistory.size()) {
                textToPaste = mClipHistory.get(index);
            }
        }
        if (textToPaste != null && !textToPaste.isEmpty()) {
            return ic.commitText(textToPaste, 1);
        }
        return false;
    }
}
