package com.abubakar.connectify.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashtagUtil {

    private static final Pattern HASHTAG_PATTERN =
            Pattern.compile("#(\\w+)");

    public static Set<String> extractHashtags(String caption) {

        Set<String> hashtags = new HashSet<>();

        if (caption == null || caption.isBlank()) {
            return hashtags;
        }

        Matcher matcher = HASHTAG_PATTERN.matcher(caption);

        while (matcher.find()) {
            hashtags.add(
                    matcher.group(1).toLowerCase()
            );
        }

        return hashtags;
    }

}
