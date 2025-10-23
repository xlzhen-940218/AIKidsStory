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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.xlzhen.aikidsstory.config.SettingsConfig;
import com.xlzhen.aikidsstory.databinding.ActivityMainBinding;
import com.xlzhen.aikidsstory.models.RequestStoryModel;
import com.xlzhen.aikidsstory.models.StoryModel;
import com.xlzhen.aikidsstory.models.StoryModelList;
import com.xlzhen.aikidsstory.utils.DateUtils;
import com.xlzhen.aikidsstory.utils.GeneratorTTS;
import com.xlzhen.aikidsstory.utils.StoryGeneratorClient;
import com.xlzhen.aikidsstory.viewmodel.MainActivityViewModel;
import com.xlzhen.mvvm.activity.BaseActivity;
import com.xlzhen.mvvm.storage.StorageUtils;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends BaseActivity<ActivityMainBinding, MainActivityViewModel> {
    private SettingsConfig settingsConfig;
    private float lastTranslationY = 0;
    private float lastPlayTranslationY = 0;
//    private AppCompatImageView generatorButtonImageView;
//    private AppCompatImageView generatorButtonBackgroundImageView;
//    private AppCompatTextView generatorButtonTextView;
//    private AppCompatTextView storyTextView;
//
//    private AppCompatImageView playButtonImageView;
//    private AppCompatImageView playButtonBackgroundImageView;
//
//    private AppCompatImageView historyImageView;
//    private AppCompatImageView settingsImageView;
//
//    private ScrollView scrollView;

    private ObjectAnimator alphaTextObjAnimator;
    private ObjectAnimator rotateButtonObjAnimator;
    private ObjectAnimator alphaButtonObjAnimator;
    private ObjectAnimator rotateTextObjAnimator;
    private ObjectAnimator playAnimator;
    private StoryGeneratorClient client;
    private String audioPath;

    private boolean generatorStory = false;

    @Override
    protected int getVariableId() {
        return BR.main;
    }

    @Override
    protected MainActivityViewModel bindingModel() {
        return new MainActivityViewModel(this);
    }

    @Override
    protected void initData() {

        settingsConfig = StorageUtils.getData(this, "config", SettingsConfig.class);
        if (settingsConfig == null) {
            settingsConfig = new SettingsConfig();
        }

        // 初始化客户端
        client = new StoryGeneratorClient();
        translationYImageView(-100);
        translationYPlayImageView(-600);
        ObjectAnimator.ofFloat(binding.storyScrollView, "alpha", 1, 0f)
                .setDuration(1000).start();
        binding.generatorButtonTextView.setOnClickListener(v -> {
            if (generatorStory) {
                return;
            }
            if (mp != null) {
                mp.stop();
                binding.playButtonImageView.setImageResource(R.drawable.play_icon);
            }
            ObjectAnimator.ofFloat(binding.storyScrollView, "alpha", binding.storyScrollView.getAlpha(), 0f)
                    .setDuration(1000).start();
            generatorStory = true;
            translationYImageView(-500);
            translationYPlayImageView(-600);
            alphaImageView();
            requestStory();
        });

        binding.playButtonImageView.setOnClickListener(v -> {

            audioPlayer(audioPath);
        });

        binding.settingsImageView.setOnClickListener(view -> {
            ObjectAnimator.ofFloat(binding.settingsImageView, "rotation", 0, 360)
                    .setDuration(1000).start();
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
        binding.historyImageView.setOnClickListener(view -> {
            ObjectAnimator.ofFloat(binding.historyImageView, "rotation", 0, 360)
                    .setDuration(1000).start();
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        });
    }

    @Override
    protected ActivityMainBinding bindingInflate() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }


    private void requestStory() {

        // 调用异步请求方法
        client.generateStoryAsync(new RequestStoryModel("", Locale.getDefault().toLanguageTag()), new StoryGeneratorClient.StoryCallback() {
            @Override
            public void onSuccess(String storyContent, String themeUsed) {
                String display = getString(R.string.title) + themeUsed + "\n\n" + getString(R.string.story) + storyContent;
                ObjectAnimator.ofFloat(binding.storyScrollView, "alpha", 0f, 1f)
                        .setDuration(1000).start();
                binding.storyTextView.setText(display);
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
        ObjectAnimator.ofFloat(binding.generatorButtonBgImageView, "translationY", lastTranslationY, translationY)
                .setDuration(1000).start();
        ObjectAnimator.ofFloat(binding.generatorButtonImageView, "translationY", lastTranslationY, translationY)
                .setDuration(1000).start();
        ObjectAnimator.ofFloat(binding.generatorButtonTextView, "translationY", lastTranslationY, translationY)
                .setDuration(1000).start();


        lastTranslationY = translationY;
    }

    private void translationYPlayImageView(int playTranslationY) {
        ObjectAnimator.ofFloat(binding.playButtonBgImageView, "translationY", lastPlayTranslationY, playTranslationY)
                .setDuration(1000).start();
        ObjectAnimator.ofFloat(binding.playButtonImageView, "translationY", lastPlayTranslationY, playTranslationY)
                .setDuration(1000).start();
        if (generatorStory) {
            ObjectAnimator.ofFloat(binding.historyImageView, "translationY", binding.historyImageView.getTranslationY(), playTranslationY)
                    .setDuration(1000).start();
            ObjectAnimator.ofFloat(binding.settingsImageView, "translationY", binding.settingsImageView.getTranslationY(), playTranslationY)
                    .setDuration(1000).start();
        } else {
            ObjectAnimator.ofFloat(binding.historyImageView, "translationY", binding.historyImageView.getTranslationY(), 0)
                    .setDuration(1000).start();
            ObjectAnimator.ofFloat(binding.settingsImageView, "translationY", binding.settingsImageView.getTranslationY(), 0)
                    .setDuration(1000).start();
        }
        lastPlayTranslationY = playTranslationY;
    }

    private void alphaImageView() {
        alphaTextObjAnimator = ObjectAnimator.ofFloat(binding.generatorButtonTextView, "alpha", 1, 0f)
                .setDuration(1000);
        alphaTextObjAnimator.setRepeatCount(100);
        alphaTextObjAnimator.setRepeatMode(REVERSE);
        alphaTextObjAnimator.start();

        rotateTextObjAnimator = ObjectAnimator.ofFloat(binding.generatorButtonTextView, "rotation", 0, 360)
                .setDuration(1000);
        rotateTextObjAnimator.setRepeatCount(100);
        rotateTextObjAnimator.setRepeatMode(REVERSE);
        rotateTextObjAnimator.start();

        alphaButtonObjAnimator = ObjectAnimator.ofFloat(binding.generatorButtonImageView, "alpha", 1, 0f)
                .setDuration(1000);
        alphaButtonObjAnimator.setRepeatCount(100);
        alphaButtonObjAnimator.setRepeatMode(REVERSE);
        alphaButtonObjAnimator.start();

        rotateButtonObjAnimator = ObjectAnimator.ofFloat(binding.generatorButtonImageView, "rotation", 0, 360)
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
        rotateTextObjAnimator.cancel();
        ObjectAnimator.ofFloat(binding.generatorButtonTextView, "alpha", 0f, 1f).setDuration(100).start();
        ObjectAnimator.ofFloat(binding.generatorButtonTextView, "rotation", 0).setDuration(100).start();

        ObjectAnimator.ofFloat(binding.generatorButtonImageView, "alpha", 0f, 1f).setDuration(100).start();
        ObjectAnimator.ofFloat(binding.generatorButtonImageView, "rotation", 0).setDuration(100).start();

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
                binding.playButtonImageView.setImageResource(R.drawable.play_icon);
                playPause = false;

            });
        }
        if (mp.isPlaying()) {
            playAnimator.cancel();
            playPause = true;
            mp.pause();
            binding.playButtonImageView.setImageResource(R.drawable.play_icon);
            return;
        } else if (playPause) {
            if (playAnimator != null) {
                playAnimator.cancel();
            }
            playAnimator = ObjectAnimator.ofFloat(binding.playButtonBgImageView, "rotation", 0, 360)
                    .setDuration(1000);
            playAnimator.setRepeatMode(ValueAnimator.RESTART);
            playAnimator.setRepeatCount(1000);
            playAnimator.start();

            playPause = false;
            mp.start();
            binding.playButtonImageView.setImageResource(R.drawable.pause_icon);
            return;
        }
        try {
            if (playAnimator != null) {
                playAnimator.cancel();
            }
            playAnimator = ObjectAnimator.ofFloat(binding.playButtonBgImageView, "rotation", 0, 360)
                    .setDuration(10000);
            playAnimator.setRepeatMode(ValueAnimator.RESTART);
            playAnimator.setRepeatCount(1000);
            playAnimator.start();
            binding.playButtonImageView.setImageResource(R.drawable.pause_icon);
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
    public void onDestroy() {
        super.onDestroy();
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp = null;
        }
    }

}