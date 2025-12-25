package com.recall.utils;

public class CustomStringUtil {
    public static boolean containsChinese(String str) {
        if (str == null) return false;
        return str.matches(".*[\u4e00-\u9fff]+.*");
    }
}
