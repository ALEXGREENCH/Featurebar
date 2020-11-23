/*
 * The Spreadtrum Communication Inc. 2016
 */

package com.sprd.android.support.featurebar;

import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyCharacterMap.KeyData;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import com.android.internal.app.ActionBarImpl;

import com.android.internal.widget.ActionBarContextView;
import com.android.internal.widget.ActionBarView;

public class FeatureBarHelper {
    private static final String TAG = "FeatureBarHelper";
    private static final boolean DEBUG = true;

    ViewGroup mBar;
    View mOptionsKey;
    View mCenterKey;
    View mBackKey;

    Window mWindow;

    private static final int FLAG_POTENTIAL_ERROR_SET_CONTENT = 0x0010;
    int mPotentialErrorFlag = 0;

    public FeatureBarHelper(Activity activity) {
        if (activity == null) {
            Log.e(TAG, "Pass activity is null");
            return;
        }

        mWindow = activity.getWindow();
        if (mWindow == null) {
            Log.e(TAG, "Window of activity is null");
            return;
        }
        wrapDecor(activity);
    }

    public void wrapDecor(final Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        if (decorView == null) {
            Log.e(TAG, "DecorView is null");
            return;
        }

        ViewGroup contentView = (ViewGroup) decorView.findViewById(android.R.id.content);
        if (contentView == null) {
            Log.e(TAG, "Decor view is null, have you called wrapDecor after Activity#super.onCreate?");
            return;
        }

        final int childCount = contentView.getChildCount();
        if (childCount == 0) {
            // Maybe called before Activity#setContentView
            mPotentialErrorFlag |= FLAG_POTENTIAL_ERROR_SET_CONTENT;
        }
        View[] children = new View[childCount];
        for (int i = 0; i < childCount; i++) {
            children[i] = contentView.getChildAt(i);
        }

        //change for listActivity, remove to avoid crash
        contentView.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(activity);
        View wrapper = inflater.inflate(R.layout.decor_layout, null);

        ViewGroup rawContentView = (ViewGroup) wrapper.findViewById(R.id.content);
        if (childCount > 0) {
            for (View child : children) {
                rawContentView.addView(child);
            }
        }
        //change for listActivity, add view first then setContenView
        activity.setContentView(wrapper);

        mBar = (ViewGroup) wrapper.findViewById(R.id.feature_bar);
        mBackKey = wrapper.findViewById(R.id.feature_bar_back);
        mCenterKey = wrapper.findViewById(R.id.feature_bar_center);
        mOptionsKey = wrapper.findViewById(R.id.feature_bar_options);

        mBackKey.setClickable(true);
        mBackKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDownAndUpKeyEvents(activity, KeyEvent.KEYCODE_BACK);
            }
        });
        mOptionsKey.setClickable(true);
        mOptionsKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDownAndUpKeyEvents(activity, KeyEvent.KEYCODE_MENU);
            }
        });
        mCenterKey.setClickable(true);
        mCenterKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDownAndUpKeyEvents(activity, KeyEvent.KEYCODE_DPAD_CENTER);
            }
        });

        if(activity.getActionBar() != null) {
            activity.getActionBar().addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
                    @Override
                    public void onMenuVisibilityChanged(boolean isVisible) {
                        if (isVisible) {
                            ((TextView) mOptionsKey).setText("");
                        } else {
                            ((TextView) mOptionsKey).setText(R.string.default_feature_bar_options);
                        }
                 }
            });
        }
        ActionBarView actionBarView = (ActionBarView) decorView.findViewById(
                com.android.internal.R.id.action_bar);
        if (actionBarView != null) {
            actionBarView.setOverrideOverflowButton(mOptionsKey);
        } else {
            Log.d(TAG, "actionBarView is null");
            if (mWindow != null) {
                if(DEBUG) Log.d(TAG, "Attempt to invoke setShouldOverrideResources access PhoneWindow");
                mWindow.setShouldOverrideResources(true);
            } else {
                if(DEBUG) Log.d(TAG, "mWindow is empty, pls check it");
            }
        }
    }

    public View getOptionsKeyView() {
        checkError(FLAG_POTENTIAL_ERROR_SET_CONTENT, mOptionsKey);
        return mOptionsKey;
    }

    public View getCenterKeyView() {
        checkError(FLAG_POTENTIAL_ERROR_SET_CONTENT, mCenterKey);
        return mCenterKey;
    }

    public View getBackKeyView() {
        checkError(FLAG_POTENTIAL_ERROR_SET_CONTENT, mBackKey);
        return mBackKey;
    }

    public ViewGroup getFeatureBar() {
        checkError(FLAG_POTENTIAL_ERROR_SET_CONTENT, mBar);
        return mBar;
    }

    private void checkError(int flag, Object obj) {
        switch (flag) {
            case FLAG_POTENTIAL_ERROR_SET_CONTENT:
                // if ((mPotentialErrorFlag & FLAG_POTENTIAL_ERROR_SET_CONTENT) != 0) {
                    if (obj == null) {
                        throw new IllegalStateException("Can not find views, have you called wrapDecor before setContentView ?");
                    }
                // }
                break;
        }

    }

    public static void sendDownAndUpKeyEvents(Activity activity, int keyCode) {
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                // Inject down.
                final long downTime = SystemClock.uptimeMillis();
                KeyEvent down = KeyEvent.obtain(downTime, downTime, KeyEvent.ACTION_DOWN, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_FROM_SYSTEM,
                        InputDevice.SOURCE_KEYBOARD, null);
                window.injectInputEvent(down);
                    // Inject up.
                final long upTime = SystemClock.uptimeMillis();
                KeyEvent up = KeyEvent.obtain(downTime, upTime, KeyEvent.ACTION_UP, keyCode, 0, 0,
                        KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_FROM_SYSTEM,
                        InputDevice.SOURCE_KEYBOARD, null);
                window.injectInputEvent(up);
            }
        }
    }

    public void setActionMode(Activity activity){
        View decorView = activity.getWindow().getDecorView();
        if (decorView == null) {
            Log.e(TAG, "DecorView is null");
            return;
        }

        ActionBarContextView actionBarContextView = (ActionBarContextView) decorView.findViewById(
                com.android.internal.R.id.action_context_bar);
        Log.d(TAG, "find  ActionBarContextView ");
        if(actionBarContextView != null){
            Log.d(TAG, "  ActionBarContextView  is "+actionBarContextView);
            actionBarContextView.setOverrideOverflowButton(mOptionsKey);
        }else {
            Log.d(TAG, "ActionBarContextView is null");
        }
    }
}

