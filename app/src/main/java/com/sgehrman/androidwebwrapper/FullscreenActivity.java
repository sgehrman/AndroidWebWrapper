package com.sgehrman.androidwebwrapper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sgehrman.androidwebwrapper.util.SystemUiHider;

public class FullscreenActivity extends Activity {
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

      delayedHide(AUTO_HIDE_DELAY_MILLIS);

      return false;
    }
  };

  private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
  Handler mHideHandler = new Handler();
  Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
      mSystemUiHider.hide();
    }
  };
  private SystemUiHider mSystemUiHider;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_fullscreen);

    final View controlsView = findViewById(R.id.fullscreen_content_controls);
    final View contentView = findViewById(R.id.fullscreen_content);

    final WebView webView = (WebView) findViewById(R.id.fullscreen_content);
    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setUseWideViewPort(true);
    settings.setLoadWithOverviewMode(true);
    webView.setWebViewClient(new MyWebViewClient());

//      webView.loadUrl("http://learn.code.org/hoc/1");
      webView.loadUrl("http://www.slatercenter.com/products/screen");

    // Set up an instance of SystemUiHider to control the system UI for
    // this activity.
    mSystemUiHider = new SystemUiHider(this, contentView, HIDER_FLAGS);
    mSystemUiHider.setup();
    mSystemUiHider
        .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
          // Cached values.
          int mControlsHeight;
          int mShortAnimTime;

          @Override
          public void onVisibilityChange(boolean visible) {
            if (mControlsHeight == 0) {
              mControlsHeight = controlsView.getHeight();
            }
            if (mShortAnimTime == 0) {
              mShortAnimTime = getResources().getInteger(
                  android.R.integer.config_shortAnimTime);
            }
            controlsView.animate()
                .translationY(visible ? 0 : mControlsHeight)
                .setDuration(mShortAnimTime);

            if (visible) {
              delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
          }
        });

    findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    delayedHide(100);
  }

  private void delayedHide(int delayMillis) {
    mHideHandler.removeCallbacks(mHideRunnable);
    mHideHandler.postDelayed(mHideRunnable, delayMillis);
  }

  private class MyWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (Uri.parse(url).getHost().equals("learn.code.org")) {
        // This is my web site, so do not override; let my WebView load the page
        return false;
      }
      // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      startActivity(intent);
      return true;
    }
  }

}
