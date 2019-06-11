package com.quickblox.sample.chat.java.utils;

import android.util.Patterns;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkUtils {

    /**
     * Returns a list with all links contained in the input
     */
    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<>();
        Pattern pattern = Patterns.WEB_URL;
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
        }
        return containedUrls;
    }

    public static String getLinkForHostIcon(String link) {
        String iconLink;
        try {
            URL url = new URL(getLinkWithProtocol(link));
            iconLink = String.format("%s://%s/favicon.ico", url.getProtocol(), url.getHost());
        } catch (MalformedURLException e) {
            iconLink = null;
        }
        return iconLink;
    }

    public static String getHostFromLink(String link) {
        String host;
        try {
            URL url = new URL(getLinkWithProtocol(link));
            host = url.getHost();
        } catch (MalformedURLException e) {
            host = null;
        }
        return host;
    }

    public static String getLinkWithProtocol(String link) {
        if (!link.startsWith("http")) {
            link = "https://" + link;
        }
        return link;
    }

    public static String prepareCorrectLink(String link) {
        if (link.startsWith("http") && !link.contains("://")) {
            link = link.replace(":/", "://");
        } else if (link.startsWith("//")) {
            link = "https://" + link.substring(2);
        } else if (link.startsWith("/")) {
            link = "https://" + link.substring(1);
        }
        return link;
    }
}