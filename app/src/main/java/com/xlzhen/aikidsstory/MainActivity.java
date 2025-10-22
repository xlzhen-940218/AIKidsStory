package com.xlzhen.aikidsstory;

import static android.animation.ValueAnimator.REVERSE;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.xlzhen.aikidsstory.config.SettingsConfig;
import com.xlzhen.aikidsstory.models.RequestStoryModel;
import com.xlzhen.aikidsstory.models.StoryModel;
import com.xlzhen.aikidsstory.models.StoryModelList;
import com.xlzhen.aikidsstory.utils.DateUtils;
import com.xlzhen.aikidsstory.utils.GeneratorTTS;
import com.xlzhen.aikidsstory.utils.StorageUtils;
import com.xlzhen.aikidsstory.utils.StoryGeneratorClient;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private SettingsConfig settingsConfig;
    private float lastTranslationY = 0;
    private float lastPlayTranslationY = 0;
    private AppCompatImageView generatorButtonImageView;
    private AppCompatImageView generatorButtonBackgroundImageView;
    private AppCompatTextView generatorButtonTextView;
    private AppCompatTextView storyTextView;

    private AppCompatImageView playButtonImageView;
    private AppCompatImageView playButtonBackgroundImageView;

    private AppCompatImageView historyImageView;
    private AppCompatImageView settingsImageView;

    private ScrollView scrollView;

    private ObjectAnimator alphaTextObjAnimator;
    private ObjectAnimator rotateButtonObjAnimator;
    private ObjectAnimator alphaButtonObjAnimator;
    private ObjectAnimator playAnimator;
    private StoryGeneratorClient client;
    private String audioPath;

    private boolean generatorStory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        settingsConfig = StorageUtils.getData(this, "config", SettingsConfig.class);
        if (settingsConfig == null) {
            settingsConfig = new SettingsConfig();
        }

        initViews();

        // 初始化客户端
        client = new StoryGeneratorClient();
    }

    private void initViews() {
        generatorButtonImageView = findViewById(R.id.generator_button_image_view);
        generatorButtonBackgroundImageView = findViewById(R.id.generator_button_bg_image_view);
        generatorButtonTextView = findViewById(R.id.generator_button_text_view);
        playButtonImageView = findViewById(R.id.play_button_image_view);
        playButtonBackgroundImageView = findViewById(R.id.play_button_bg_image_view);
        storyTextView = findViewById(R.id.story_text_view);

        historyImageView = findViewById(R.id.history_image_view);
        settingsImageView = findViewById(R.id.settings_image_view);
        scrollView = findViewById(R.id.story_scroll_view);


        translationYImageView(-100);
        translationYPlayImageView(-500);
        ObjectAnimator.ofFloat(scrollView, "alpha", 1, 0f)
                .setDuration(1000).start();
        generatorButtonTextView.setOnClickListener(v -> {
            if (generatorStory) {
                return;
            }
            if (mp != null) {
                mp.stop();
                playButtonImageView.setImageResource(R.drawable.play_icon);
            }
            ObjectAnimator.ofFloat(scrollView, "alpha", scrollView.getAlpha(), 0f)
                    .setDuration(1000).start();
            generatorStory = true;
            translationYImageView(-500);
            translationYPlayImageView(-500);
            alphaImageView();
            requestStory();
        });

        playButtonImageView.setOnClickListener(v -> {

            audioPlayer(audioPath);
        });

        settingsImageView.setOnClickListener(view -> {
            ObjectAnimator.ofFloat(settingsImageView, "rotation", 0, 360)
                    .setDuration(1000).start();
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
        historyImageView.setOnClickListener(view -> {
            ObjectAnimator.ofFloat(historyImageView, "rotation", 0, 360)
                    .setDuration(1000).start();
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        });
    }

    private void requestStory() {

        // 调用异步请求方法
        client.generateStoryAsync(new RequestStoryModel("", Locale.getDefault().toLanguageTag()), new StoryGeneratorClient.StoryCallback() {
            @Override
            public void onSuccess(String storyContent, String themeUsed) {
                String display = getString(R.string.title) + themeUsed + "\n\n" + getString(R.string.story) + storyContent;
                ObjectAnimator.ofFloat(scrollView, "alpha", 0f, 1f)
                        .setDuration(1000).start();
                storyTextView.setText(display);
                //tvStory.setText(display);
                GeneratorTTS.handleTTS(MainActivity.this, settingsConfig.isMan(), Locale.getDefault().toLanguageTag(), display, "", path -> {
                            // 故事成功生成，更新 UI
                            cancelAnimation();
                            audioPath = path;
                            StoryModelList storyModelList = StorageUtils.getData(MainActivity.this, "history", StoryModelList.class);
                            if (storyModelList == null) {
                                storyModelList = new StoryModelList(new ArrayList<>());
                            }
                            StoryModel storyModel = new StoryModel();
                            storyModel.setContent(storyContent);
                            storyModel.setId(System.currentTimeMillis());
                            storyModel.setTitle(themeUsed);
                            storyModel.setAudioPath(audioPath);
                            storyModel.setTime(DateUtils.convertMillisToDateTime(storyModel.getId()));
                            storyModelList.getStoryModels().add(storyModel);
                            StorageUtils.saveData(MainActivity.this, "history", storyModelList);
                        }, R.string.generator_story
                );
            }

            @Override
            public void onFailure(String errorMessage) {
                // 故事生成失败，提示用户
                cancelAnimation();

                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void translationYImageView(int translationY) {
        ObjectAnimator.ofFloat(generatorButtonBackgroundImageView, "translationY", lastTranslationY, translationY)
                .setDuration(1000).start();
        ObjectAnimator.ofFloat(generatorButtonImageView, "translationY", lastTranslationY, translationY)
                .setDuration(1000).start();
        ObjectAnimator.ofFloat(generatorButtonTextView, "translationY", lastTranslationY, translationY)
                .setDuration(1000).start();


        lastTranslationY = translationY;
    }

    private void translationYPlayImageView(int playTranslationY) {
        ObjectAnimator.ofFloat(playButtonBackgroundImageView, "translationY", lastPlayTranslationY, playTranslationY)
                .setDuration(1000).start();
        ObjectAnimator.ofFloat(playButtonImageView, "translationY", lastPlayTranslationY, playTranslationY)
                .setDuration(1000).start();
        if (generatorStory) {
            ObjectAnimator.ofFloat(historyImageView, "translationY", historyImageView.getTranslationY(), playTranslationY)
                    .setDuration(1000).start();
            ObjectAnimator.ofFloat(settingsImageView, "translationY", settingsImageView.getTranslationY(), playTranslationY)
                    .setDuration(1000).start();
        } else {
            ObjectAnimator.ofFloat(historyImageView, "translationY", historyImageView.getTranslationY(), 0)
                    .setDuration(1000).start();
            ObjectAnimator.ofFloat(settingsImageView, "translationY", settingsImageView.getTranslationY(), 0)
                    .setDuration(1000).start();
        }
        lastPlayTranslationY = playTranslationY;
    }

    private void alphaImageView() {
        alphaTextObjAnimator = ObjectAnimator.ofFloat(generatorButtonTextView, "alpha", 1, 0f)
                .setDuration(1000);
        alphaTextObjAnimator.setRepeatCount(100);
        alphaTextObjAnimator.setRepeatMode(REVERSE);
        alphaTextObjAnimator.start();

        alphaButtonObjAnimator = ObjectAnimator.ofFloat(generatorButtonImageView, "alpha", 1, 0f)
                .setDuration(1000);
        alphaButtonObjAnimator.setRepeatCount(100);
        alphaButtonObjAnimator.setRepeatMode(REVERSE);
        alphaButtonObjAnimator.start();

        rotateButtonObjAnimator = ObjectAnimator.ofFloat(generatorButtonImageView, "rotation", 0, 360)
                .setDuration(1000);
        rotateButtonObjAnimator.setRepeatCount(100);
        rotateButtonObjAnimator.setRepeatMode(REVERSE);
        rotateButtonObjAnimator.start();
    }

    private void cancelAnimation() {
        generatorStory = false;
        alphaTextObjAnimator.cancel();
        alphaButtonObjAnimator.cancel();
        rotateButtonObjAnimator.cancel();
        ObjectAnimator.ofFloat(generatorButtonTextView, "alpha", 0f, 1f).setDuration(100).start();
        ObjectAnimator.ofFloat(generatorButtonImageView, "alpha", 0f, 1f).setDuration(100).start();
        ObjectAnimator.ofFloat(generatorButtonImageView, "rotation", 0).setDuration(100).start();

        translationYImageView(-100);

        translationYPlayImageView(0);
    }

    private MediaPlayer mp;

    private boolean playPause;

    public void audioPlayer(String path) {

        if (mp == null) {
            mp = new MediaPlayer();
            mp.setOnCompletionListener(mediaPlayer -> {
                mp.stop();
                playButtonImageView.setImageResource(R.drawable.play_icon);
                playPause = false;

            });
        }
        if (mp.isPlaying()) {
            playAnimator.cancel();
            playPause = true;
            mp.pause();
            playButtonImageView.setImageResource(R.drawable.play_icon);
            return;
        } else if (playPause) {
            if (playAnimator != null) {
                playAnimator.cancel();
            }
            playAnimator = ObjectAnimator.ofFloat(playButtonBackgroundImageView, "rotation", 0, 360)
                    .setDuration(1000);
            playAnimator.setRepeatMode(ValueAnimator.RESTART);
            playAnimator.setRepeatCount(1000);
            playAnimator.start();

            playPause = false;
            mp.start();
            playButtonImageView.setImageResource(R.drawable.pause_icon);
            return;
        }
        try {
            if (playAnimator != null) {
                playAnimator.cancel();
            }
            playAnimator = ObjectAnimator.ofFloat(playButtonBackgroundImageView, "rotation", 0, 360)
                    .setDuration(10000);
            playAnimator.setRepeatMode(ValueAnimator.RESTART);
            playAnimator.setRepeatCount(1000);
            playAnimator.start();
            playButtonImageView.setImageResource(R.drawable.pause_icon);
            mp.reset();
            mp.setDataSource(path);
            mp.prepare();
            mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(settingsConfig.getSpeed()));
            mp.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        settingsConfig = StorageUtils.getData(this, "config", SettingsConfig.class);
        if (settingsConfig == null) {
            settingsConfig = new SettingsConfig();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp = null;
        }
    }
}