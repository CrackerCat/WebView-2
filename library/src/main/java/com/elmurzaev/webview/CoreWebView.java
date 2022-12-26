package com.elmurzaev.webview;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class CoreWebView extends WebView implements DefaultLifecycleObserver, DownloadListener {

    private static final int RC_DOWNLOAD_FILE = 10;
    private static final int RC_GEO_PERMISSION = 11;
    private static final int RC_FILE_CHOOSER = 12;
    private static final int RC_WEB_PERMISSIONS = 13;

    @NonNull
    private String userAgentDesktop = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:107.0) Gecko/20100101 Firefox/107.0";
    @NonNull
    private String searchEngine = "https://www.google.com/search?q=%s";
    @Nullable
    private DownloadManager.Request pendingDownloadRequest;
    @Nullable
    private ValueCallback<Boolean> geoPermissionCallback;
    @Nullable
    private ValueCallback<Uri[]> fileChooserCallback;
    @Nullable
    private ProgressBar progressBar;
    @Nullable
    private PermissionRequest permissionRequest;
    private ContentBlocker contentBlocker = new ContentBlocker();

    private boolean isDesignMode;
    private boolean isContentBlockerEnabled;

    public CoreWebView(@NonNull Context context) {
        this(context, null);
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        );
    }

    public CoreWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        setWebViewClient(new WebClient());
        setWebChromeClient(new ChromeClient());
        setDownloadListener(this);

        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);
        getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);

        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);

        if (isDarkMode()) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(getSettings(), WebSettingsCompat.FORCE_DARK_ON);
            }
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                WebSettingsCompat.setForceDarkStrategy(getSettings(),
                        WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING);
            }
        }

        if (context instanceof LifecycleOwner) {
            Lifecycle lifecycle = ((LifecycleOwner) context).getLifecycle();
            lifecycle.addObserver(this);
        }
    }

    public boolean isContentBlockerEnabled() {
        return isContentBlockerEnabled;
    }

    public void setContentBlockerEnabled(boolean contentBlockerEnabled) {
        isContentBlockerEnabled = contentBlockerEnabled;
    }

    public static boolean isLocalHost(@NonNull String s) {
        return Pattern.matches("(?:https?://)?localhost(?::\\d+)?(?![^/])", s);
    }

    public void setContentBlocker(@NonNull ContentBlocker blocker) {
        contentBlocker = blocker;
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        onPause();
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        onResume();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        destroy();
        owner.getLifecycle().removeObserver(this);
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        if (URLUtil.isNetworkUrl(url) && !Objects.equals(mimetype, "text/html")) {
            String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                    .addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url))
                    .setMimeType(mimetype)
                    .setTitle(fileName)
                    .setDescription("Downloading file")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.allowScanningByMediaScanner();

            if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startDownload(request);
            } else {
                pendingDownloadRequest = request;
                ActivityCompat.requestPermissions((Activity) getContext(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_DOWNLOAD_FILE);
            }
        }
    }

    public void startDownload(@NonNull DownloadManager.Request request) {
        DownloadManager dm = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        dm.enqueue(request);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case RC_FILE_CHOOSER:
                Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                if (fileChooserCallback != null) {
                    fileChooserCallback.onReceiveValue(result);
                }
                break;
        }
    }

    @NonNull
    public String getDesktopUserAgent() {
        return userAgentDesktop;
    }

    public void setDesktopUserAgent(@NonNull String desktopUserAgent) {
        userAgentDesktop = desktopUserAgent;
    }

    @NonNull
    public String getSearchEngine() {
        return searchEngine;
    }

    /**
     * @param engine search engine name. For example: Google
     */
    public void setSearchEngine(@NonNull String engine) {
        this.searchEngine = engine;
    }

    public String getUserAgent() {
        return getSettings().getUserAgentString();
    }

    public void setUserAgent(@Nullable String userAgent) {
        getSettings().setUserAgentString(userAgent);
    }

    public boolean isDesignMode() {
        return isDesignMode;
    }

    public void setDesignMode(boolean designMode) {
        if (designMode) {
            execJs("document.designMode='on'");
        } else {
            execJs("document.designMode='off'");
        }
        this.isDesignMode = designMode;
    }

    /**
     * @return true if current theme is using night mode.
     */
    public boolean isDarkMode() {
        int mask = getContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return mask == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * @param callback listener indicating the status of the operation.
     */
    public synchronized void clearAllData(@Nullable ValueCallback<Boolean> callback) {
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(cleared -> {
            if (cleared) {
                CookieManager.getInstance().flush();
            }
        });
        clearCache(true);
        clearFormData();
        clearHistory();
        clearSslPreferences();

        if (callback != null) {
            callback.onReceiveValue(true);
        }
    }

    /**
     * Shorter version of {@link #evaluateJavascript(String, ValueCallback)}
     */
    public void execJs(@NonNull String js) {
        evaluateJavascript(js, null);
    }

    public boolean isDesktopMode() {
        return !getSettings().getUserAgentString().contains("Mobile")
                && getSettings().getLoadWithOverviewMode();
    }

    /**
     * If {@code desktopMode} is true, web sites will be opened using User-Agent from
     * {@link #getDesktopUserAgent()} and some other customizations. Calling this method
     * will cause page reload;
     */
    public void setDesktopMode(boolean desktopMode) {
        getSettings().setUserAgentString(desktopMode ? userAgentDesktop : null);
        getSettings().setLoadWithOverviewMode(desktopMode);
        getSettings().setUseWideViewPort(!desktopMode);
        setInitialScale(100);
        reload();
    }

    /**
     * @param url can be exact url with or without protocol prefix or may also be a search query,
     *            in which case a url from {@link #getSearchEngine()} will be opened with {@code url}
     *            placed in it.
     */
    @Override
    public void loadUrl(@NonNull String url) {
        if (URLUtil.isValidUrl(url)) {
            super.loadUrl(url);
        } else if (Patterns.WEB_URL.matcher(url).matches() || isLocalHost(url)) {
            super.loadUrl("http://" + url);
        } else {
            super.loadUrl(SearchEngines.getSearchUrlFor(getContext(), searchEngine, url));
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case RC_GEO_PERMISSION:
                if (geoPermissionCallback != null) {
                    geoPermissionCallback.onReceiveValue(grantResults[0]== PackageManager.PERMISSION_GRANTED);
                }
                break;
            case RC_DOWNLOAD_FILE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && pendingDownloadRequest != null) {
                    startDownload(pendingDownloadRequest);
                }
                break;
            case RC_WEB_PERMISSIONS:
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        if (permissionRequest != null) {
                            permissionRequest.deny();
                        }
                        break;
                    }
                }
                if (permissionRequest != null) {
                    permissionRequest.grant(permissionRequest.getResources());
                }
        }
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public class ChromeClient extends WebChromeClient {

        private final Activity activity;

        public ChromeClient() {
            if (getContext() instanceof Activity) {
                activity = (Activity) getContext();
            } else {
                Context context = ((ContextThemeWrapper) getContext()).getBaseContext();
                activity = (Activity) context;
            }
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (progressBar != null) {
                progressBar.setProgress(newProgress);
            }
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            decorView.addView(view);
            view.bringToFront();
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            decorView.removeViewAt(decorView.getChildCount() - 1);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            new AlertDialog.Builder(getContext())
                    .setMessage(origin + " wants to access your location.")
                    .setPositiveButton("Grant", (dialog, which) -> {
                        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            callback.invoke(origin, true, true);
                        } else {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION},
                                    RC_GEO_PERMISSION);
                            geoPermissionCallback = grant -> callback.invoke(origin, grant, grant);
                        }
                    })
                    .setNegativeButton("Discard", (dialog, which) -> {
                        callback.invoke(origin, false, false);
                    })
                    .setCancelable(false)
                    .show();
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt();
            geoPermissionCallback = null;
            Toast.makeText(activity, "Geo permission denied!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                         @NonNull FileChooserParams fileChooserParams) {
            fileChooserCallback = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            activity.startActivityForResult(intent, RC_FILE_CHOOSER);

            return true;
        }

        @Override
        public void onPermissionRequest(@NonNull PermissionRequest request) {
            Set<String> permissions = new ArraySet<>();
            for (String resource : request.getResources()) {
                switch (resource) {
                    case PermissionRequest.RESOURCE_AUDIO_CAPTURE:
                        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
                            permissions.add(Manifest.permission.RECORD_AUDIO);
                        }
                        break;
                    case PermissionRequest.RESOURCE_VIDEO_CAPTURE:
                        if (!hasPermission(Manifest.permission.CAMERA)) {
                            permissions.add(Manifest.permission.CAMERA);
                        }
                }
            }
            if (!permissions.isEmpty()) {
                permissionRequest = request;
                ActivityCompat.requestPermissions(activity,
                        permissions.toArray(new String[]{}), RC_WEB_PERMISSIONS);
            } else {
                request.grant(request.getResources());
            }
        }
    }

    public class WebClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
            if (!URLUtil.isNetworkUrl(request.getUrl().toString())) {
                try {
                    Intent intent = new Intent(Intent.parseUri(request.getUrl().toString(), 0));
                    view.getContext().startActivity(Intent.createChooser(intent, null));
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (url != null && isContentBlockerEnabled
                    && contentBlocker.isBlocked(Uri.parse(url).getHost())) {
                return new WebResourceResponse("text/html", null, null);
            }
            return super.shouldInterceptRequest(view, url);
        }
    }

    public boolean hasPermission(@NonNull String permission) {
        return ContextCompat.checkSelfPermission(getContext(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

}
