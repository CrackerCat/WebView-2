/*
 * MIT License
 *
 * Copyright (c) 2022 Ramzan Elmurzaev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elmurzaev.webview;

import android.content.Context;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

public final class SearchEngines {

    @NonNull
    public static String getSearchUrlFor(@NonNull Context context,
                                         @NonNull String searchEngine,
                                         @NonNull String query) {
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