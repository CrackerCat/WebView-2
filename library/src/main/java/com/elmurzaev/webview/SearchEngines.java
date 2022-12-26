package com.elmurzaev.webview;

import android.content.Context;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

public final class SearchEngines {

    @NonNull
    public static String getSearchUrlFor(@NonNull Context context, @NonNull String searchEngine, @NonNull String query) {
        String url = null;

        String[] engineNames = context.getResources().getStringArray(R.array.search_engine_names);
        String[] engineUrls = context.getResources().getStringArray(R.array.search_engine_urls);

        for (int i = 0; i < engineNames.length; i++) {
            if (searchEngine.equalsIgnoreCase(engineNames[i])) {
                url = engineUrls[i];
            }
        }
        if (url == null) {
            url = engineUrls[0];
        }
        return URLUtil.composeSearchUrl(query, url, "%s");
    }

}