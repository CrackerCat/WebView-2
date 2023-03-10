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

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to hold hosts that should be blocked by {@link WebView}.
 * */
public class ContentBlocker {

    private final Set<String> blockedList = new HashSet<>();
    private final Set<String> whiteList = new HashSet<>();

    /**
     * @param stream used to prepopulate block list.
     * */
    public ContentBlocker(@NonNull InputStream stream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                blockedList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add {@code host} to whitelist. It will not be
     * blocked if it's in this list, even if it's
     * currently in the blocklist.
     * */
    public void whiteList(String... host) {
        whiteList.addAll(Arrays.asList(host));
    }

    /**
     * Remove {@code host} from whitelist if it was previously
     * added to it by {@link #whiteList(String...)}
     * */
    public void removeFromWhiteList(@NonNull String... host) {
        for (String s : host) {
            whiteList.remove(s);
        }
    }

    /**
     * @return true if {@code host} is blocked and not
     * included in the whitelist.
     * */
    public boolean isBlocked(String host) {
        return blockedList.contains(host) && !whiteList.contains(host);
    }

}