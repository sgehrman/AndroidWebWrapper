package com.sgehrman.androidwebwrapper.util;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

public class SystemUiHiderBase {

  /**
   * When this flag is set, {@link #show()} and {@link #hide()} will toggle
   * the visibility of the status bar. If there is a navigation bar, show and
   * hide will toggle low profile mode.
   */
  public static final int FLAG_FULLSCREEN = 0x2;

  /**
   * When this flag is set, {@link #show()} and {@link #hide()} will toggle
   * the visibility of the navigation bar, if it's present on the device and
   * the device allows hiding it. In cases where the navigation bar is present
   * but cannot be hidden, show and hide will toggle low profile mode.
   */
  public static final int FLAG_HIDE_NAVIGATION = FLAG_FULLSCREEN | 0x4;

  /**
   * The activity associated with this UI hider object.
   */
  protected Activity mActivity;

  /**
   * The view on which {@link View#setSystemUiVisibility(int)} will be called.
   */
  protected View mAnchorView;

  /**
   * The current UI hider flags.
   *
   * @see #FLAG_FULLSCREEN
   * @see #FLAG_HIDE_NAVIGATION
   */
  protected int mFlags;

  /**
   * The current visibility callback.
   */
  protected OnVisibilityChangeListener mOnVisibilityChangeListener = sDummyListener;
  /**
   * Flags for {@link View#setSystemUiVisibility(int)} to use when showing the
   * system UI.
   */
  private int mShowFlags;

  /**
   * Flags for {@link View#setSystemUiVisibility(int)} to use when hiding the
   * system UI.
   */
  private int mHideFlags;

  /**
   * Flags to test against the first parameter in
   * {@link android.view.View.OnSystemUiVisibilityChangeListener#onSystemUiVisibilityChange(int)}
   * to determine the system UI visibility state.
   */
  private int mTestFlags;

  /**
   * Whether or not the system UI is currently visible. This is cached from
   * {@link android.view.View.OnSystemUiVisibilityChangeListener}.
   */
  private boolean mVisible = true;

  public SystemUiHiderBase(Activity activity, View anchorView, int flags) {
    mActivity = activity;
    mAnchorView = anchorView;
    mFlags = flags;

    mShowFlags = View.SYSTEM_UI_FLAG_VISIBLE;
    mHideFlags = View.SYSTEM_UI_FLAG_LOW_PROFILE;
    mTestFlags = View.SYSTEM_UI_FLAG_LOW_PROFILE;

    if ((mFlags & FLAG_FULLSCREEN) != 0) {
      // If the client requested fullscreen, add flags relevant to hiding
      // the status bar. Note that some of these constants are new as of
      // API 16 (Jelly Bean). It is safe to use them, as they are inlined
      // at compile-time and do nothing on pre-Jelly Bean devices.
      mShowFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
      mHideFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_FULLSCREEN;
    }

    if ((mFlags & FLAG_HIDE_NAVIGATION) != 0) {
      // If the client requested hiding navigation, add relevant flags.
      mShowFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
      mHideFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
      mTestFlags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setup() {
    mAnchorView.setOnSystemUiVisibilityChangeListener(mSystemUiVisibilityChangeListener);
  }

  /**
   * {@inheritDoc}
   */
  public void hide() {
    mAnchorView.setSystemUiVisibility(mHideFlags);
  }

  /**
   * {@inheritDoc}
   */
  public void show() {
    mAnchorView.setSystemUiVisibility(mShowFlags);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isVisible() {
    return mVisible;
  }

  private View.OnSystemUiVisibilityChangeListener mSystemUiVisibilityChangeListener
      = new View.OnSystemUiVisibilityChangeListener() {
    @Override
    public void onSystemUiVisibilityChange(int vis) {
      // Test against mTestFlags to see if the system UI is visible.
      if ((vis & mTestFlags) != 0) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
          // Pre-Jelly Bean, we must manually hide the action bar
          // and use the old window flags API.
          mActivity.getActionBar().hide();
          mActivity.getWindow().setFlags(
              WindowManager.LayoutParams.FLAG_FULLSCREEN,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // Trigger the registered listener and cache the visibility
        // state.
        mOnVisibilityChangeListener.onVisibilityChange(false);
        mVisible = false;

      } else {
        mAnchorView.setSystemUiVisibility(mShowFlags);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
          // Pre-Jelly Bean, we must manually show the action bar
          // and use the old window flags API.
          mActivity.getActionBar().show();
          mActivity.getWindow().setFlags(
              0,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // Trigger the registered listener and cache the visibility
        // state.
        mOnVisibilityChangeListener.onVisibilityChange(true);
        mVisible = true;
      }
    }
  };

  /**
   * Toggle the visibility of the system UI.
   */
  public void toggle() {
    if (isVisible()) {
      hide();
    } else {
      show();
    }
  }

  /**
   * Registers a callback, to be triggered when the system UI visibility
   * changes.
   */
  public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) {
    if (listener == null) {
      listener = sDummyListener;
    }

    mOnVisibilityChangeListener = listener;
  }

  /**
   * A dummy no-op callback for use when there is no other listener set.
   */
  private static OnVisibilityChangeListener sDummyListener = new OnVisibilityChangeListener() {
    @Override
    public void onVisibilityChange(boolean visible) {
    }
  };

  /**
   * A callback interface used to listen for system UI visibility changes.
   */
  public interface OnVisibilityChangeListener {
    /**
     * Called when the system UI visibility has changed.
     *
     * @param visible True if the system UI is visible.
     */
    public void onVisibilityChange(boolean visible);
  }

}
