package com.recall.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for string operations, particularly for handling different languages.
 */
public class CustomStringUtil {

    /**
     * Checks if the string contains Chinese characters.
     * @param str The string to check
     * @return true if Chinese characters are found, false otherwise
     */
    public static boolean containsChinese(String str) {
        if (str == null) return false;
        return str.matches(".*[一-\u9fff]+.*");
    }

    /**
     * Checks if the string contains Japanese characters (Hiragana or Katakana).
     * @param text The string to check
     * @return true if Japanese characters are found, false otherwise
     */
    public static boolean containsJapanese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Hiragana range
            if (c >= '\u3040' && c <= '\u309F') return true;

            // Katakana range
            if (c >= '\u30A0' && c <= '\u30FF') return true;
        }
        return false;
    }

    /**
     * Splits a string into chunks of a specified length.
     * @param str The string to split
     * @param chunkSize The size of each chunk
     * @return A list of string chunks
     */
    public static List<String> splitByLengthStream(String str, int chunkSize) {
        if (str == null || str.isEmpty() || chunkSize <= 0) {
            return List.of();
        }

        return IntStream.iterate(0, i -> i < str.length(), i -> i + chunkSize)
                .mapToObj(i -> str.substring(i, Math.min(i + chunkSize, str.length())))
                .collect(Collectors.toList());
    }
}
