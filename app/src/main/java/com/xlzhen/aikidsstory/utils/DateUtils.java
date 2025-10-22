package com.xlzhen.aikidsstory.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    // 默认的日期时间格式
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将毫秒时间戳转换为指定格式的日期时间字符串。
     *
     * @param timestamp 毫秒时间戳 (例如 System.currentTimeMillis())
     * @param format 目标日期时间的格式字符串 (例如 "yyyy-MM-dd HH:mm:ss")
     * @return 格式化后的日期时间字符串
     */
    public static String convertMillisToDateTime(long timestamp, String format) {
        // 1. 创建 SimpleDateFormat 对象，指定格式和地区
        // 注意：在 Android 中，强烈建议传入 Locale，避免在不同设备上的格式化问题。
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

        // 2. 将毫秒时间戳转换为 Date 对象
        Date date = new Date(timestamp);

        // 3. 格式化并返回字符串
        return sdf.format(date);
    }

    /**
     * 将毫秒时间戳转换为默认格式 "yyyy-MM-dd HH:mm:ss" 的字符串。
     *
     * @param timestamp 毫秒时间戳
     * @return 格式化后的日期时间字符串
     */
    public static String convertMillisToDateTime(long timestamp) {
        return convertMillisToDateTime(timestamp, DEFAULT_FORMAT);
    }
}

// ------------------- 使用示例 -------------------
// 在 Activity/Fragment 或其他类中调用：
/*
public class Example {
    public void showTime() {
        long currentTimeMillis = System.currentTimeMillis();

        // 使用默认格式
        String formattedTime = DateUtils.convertMillisToDateTime(currentTimeMillis);
        System.out.println("当前时间: " + formattedTime); 
        // 输出示例: 当前时间: 2025-10-22 14:46:29

        // 使用自定义格式
        String customFormat = DateUtils.convertMillisToDateTime(currentTimeMillis, "MM/dd HH:mm");
        System.out.println("自定义格式: " + customFormat);
        // 输出示例: 自定义格式: 10/22 14:46
    }
}
*/