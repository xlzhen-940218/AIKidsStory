package com.xlzhen.aikidsstory.models;

/**
 * 用于 Gson 解析服务器返回的 JSON 响应的 Java 对象 (POJO)。
 * 字段名必须与服务器返回的 JSON 键名一致。
 */
public class StoryResponse {
    
    // Gson 会自动将 JSON 字符串中的 "story" 字段映射到这个变量
    private String story; 
    
    // Gson 会自动将 JSON 字符串中的 "theme_used" 字段映射到这个变量
    private String theme_used;

    // 其他字段 (可选，但推荐保留以匹配结构)
    private String theme_source;
    private String model;
    private boolean success;

    // 必须提供 getter 方法以便访问解析后的数据
    public String getStory() {
        return story;
    }

    public String getThemeUsed() {
        return theme_used;
    }

    public boolean isSuccess() {
        return success;
    }
    
    // (可以省略其他字段的 getter/setter，如果应用不需要用到它们)
}