package com.xlzhen.aikidsstory.utils;

import com.xlzhen.mvvm.storage.StorageUtils;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.xlzhen.edgetts.EdgeTTS;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class GeneratorTTS {
    public static void handleTTS(Context context, boolean man, String language, String text, String existingPath, Consumer<String> pathSetter, @StringRes int errorRes) {
        if (text.isEmpty()) {
            Toast.makeText(context, errorRes, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!existingPath.isEmpty()) return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String path = StorageUtils.generateVoiceFilePath(context);
                path = EdgeTTS.textToMp3(text, path, language, man);

                if (new File(path).exists()) {
                    String finalPath = path;
                    ((Activity) context).runOnUiThread(() -> {
                        pathSetter.accept(finalPath);
                        // 3. 可在此添加数据更新通知
                    });
                }
            } catch (Exception e) {
                Log.e("TTS", "Generate voice failed", e);
            }
        });
    }
}
