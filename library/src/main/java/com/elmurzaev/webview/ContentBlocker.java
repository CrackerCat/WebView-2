package com.elmurzaev.webview;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ContentBlocker {

    private final Set<String> blockedList = new HashSet<>();
    private final Set<String> whiteList = new HashSet<>();

    public ContentBlocker() {

    }

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

    public void addToWhiteList(String... host) {
        whiteList.addAll(Arrays.asList(host));
    }

    public void removeFromWhiteList(@NonNull String... host) {
        for (String s : host) {
            whiteList.remove(s);
        }
    }

    public boolean isBlocked(String host) {
        return blockedList.contains(host) && !whiteList.contains(host);
    }

}