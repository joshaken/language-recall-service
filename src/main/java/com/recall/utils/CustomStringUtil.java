package com.recall.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CustomStringUtil {
    public static boolean containsChinese(String str) {
        if (str == null) return false;
        return str.matches(".*[一-\u9fff]+.*");
    }

    public static boolean containsJapanese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // 平假名
            if (c >= '\u3040' && c <= '\u309F') return true;

            // 片假名
            if (c >= '\u30A0' && c <= '\u30FF') return true;
        }
        return false;
    }

    public static List<String> splitByLengthStream(String str, int chunkSize) {
        if (str == null || str.isEmpty() || chunkSize <= 0) {
            return List.of();
        }

        return IntStream.iterate(0, i -> i < str.length(), i -> i + chunkSize)
                .mapToObj(i -> str.substring(i, Math.min(i + chunkSize, str.length())))
                .collect(Collectors.toList());
    }
}
