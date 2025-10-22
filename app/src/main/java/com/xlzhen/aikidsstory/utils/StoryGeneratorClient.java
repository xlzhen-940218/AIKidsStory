package com.xlzhen.aikidsstory.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alibaba.fastjson2.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.xlzhen.aikidsstory.config.WebConfig;
import com.xlzhen.aikidsstory.models.RequestStoryModel;
import com.xlzhen.aikidsstory.models.StoryResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class StoryGeneratorClient {

    // 替换为你的服务器地址和端口
    // 注意：如果是本地测试，请使用你电脑的局域网 IP 地址，而不是 127.0.0.1 或 localhost。

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // 用于切换回主线程更新 UI
    private static final int TIMEOUT_SECONDS = 120; // 将超时时间设置为 45 秒

    public StoryGeneratorClient() {
        // 使用 Builder 模式配置 OkHttpClient，设置更长的超时时间
        this.client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS) // 连接超时
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)    // 读取超时 (等待服务器返回数据)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)   // 写入超时 (发送请求体数据)
                .build();
    }

    /**
     * 定义一个回调接口，用于处理请求结果
     */
    public interface StoryCallback {
        void onSuccess(String storyContent, String themeUsed);

        void onFailure(String errorMessage);
    }

    /**
     * 异步发送请求并生成儿童故事
     * * @param userRequest 用户的具体故事要求，如果为空字符串("")，服务器将自动生成随机主题。
     *
     * @param callback 结果回调接口
     */
    public void generateStoryAsync(RequestStoryModel requestStoryModel, StoryCallback callback) {

        // 1. 构造 JSON 请求体
        String jsonPayload = com.alibaba.fastjson2.JSON.toJSONString(requestStoryModel);

        RequestBody body = RequestBody.create(jsonPayload, JSON);

        // 2. 构造 HTTP POST 请求
        Request request = new Request.Builder()
                .url(WebConfig.SERVER_URL)
                .post(body)
                .build();

        // 3. 异步执行请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 网络连接失败或请求被取消
                Log.e("StoryClient", "请求失败: " + e.getMessage());
                // 切换到主线程执行失败回调
                mainHandler.post(() -> {
                    callback.onFailure("网络连接错误: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody == null) {
                        mainHandler.post(() -> callback.onFailure("服务器返回空响应体。"));
                        return;
                    }

                    String responseData = responseBody.string();

                    if (!response.isSuccessful()) {
                        Log.e("StoryClient", "服务器错误: " + response.code() + ", Body: " + responseData);
                        mainHandler.post(() -> {
                            callback.onFailure("生成故事失败: HTTP " + response.code() + ". 详情: " + responseData);
                        });
                        return;
                    }

                    // =================================================================
                    // 健壮的 JSON 解析逻辑 (使用 Gson)
                    // =================================================================
                    StoryResponse storyResponse = null;
                    try {
                        // 使用 Gson 将 JSON 字符串自动转换为 StoryResponse 对象
                        storyResponse = new Gson().fromJson(responseData, StoryResponse.class);
                    } catch (JsonSyntaxException e) {
                        Log.e("StoryClient", "JSON 格式错误或解析失败: " + e.getMessage() + "\nData: " + responseData);
                        mainHandler.post(() -> callback.onFailure("JSON 响应格式错误。"));
                        return;
                    }

                    if (storyResponse == null || !storyResponse.isSuccess()) {
                        mainHandler.post(() -> callback.onFailure("故事生成失败，响应结构无效或 success=false。"));
                        return;
                    }

                    // 成功后提取数据
                    String finalStory = storyResponse.getStory();
                    String finalTheme = storyResponse.getThemeUsed();

                    // 切换回主线程执行成功回调
                    mainHandler.post(() -> {
                        callback.onSuccess(finalStory, finalTheme);
                    });

                }
            }
        });
    }
}