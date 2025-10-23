package com.xlzhen.aikidsstory;

import static android.animation.ValueAnimator.REVERSE;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;

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

    // --- 常量定义 ---
    private static final int ANIM_DURATION_TRANSLATION = 500;
    private static final int ANIM_DURATION_STORY_ALPHA = 1000;
    private static final int ANIM_DURATION_ROTATE_PLAY = 10000; // 播放按钮旋转速度
    private static final int TRANSLATION_Y_GENERATOR_HIDDEN = -500;
    private static final int TRANSLATION_Y_GENERATOR_VISIBLE = -100;
    private static final int TRANSLATION_Y_PLAY_HIDDEN = -700;
    private static final int TRANSLATION_Y_PLAY_VISIBLE = 0;


    // --- 状态和资源变量 ---
    private SettingsConfig settingsConfig;
    private MediaPlayer mediaPlayer; // 更改命名: mp -> mediaPlayer
    private boolean isPausedByManual = false; // 更改命名: playPause -> isPausedByManual (用于区分手动暂停和停止)
    private StoryGeneratorClient storyGeneratorClient; // 更改命名: client -> storyGeneratorClient

    // 动画对象
    private AnimatorSet generatorIconAnimatorSet; // 统一管理生成按钮动画
    private ObjectAnimator playAnimator;

    // --- 临时位移状态 (屏幕旋转后不保留) ---
    private float lastTranslationY = 0;
    private float lastPlayTranslationY = 0;


    @Override
    protected int getVariableId() {
        return BR.main;
    }

    @Override
    protected MainActivityViewModel bindingModel() {
        // 使用 ApplicationContext 初始化，避免 Activity 引用
        return new MainActivityViewModel(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 确保在 onCreate 或 initData 中初始化客户端和播放器
        storyGeneratorClient = new StoryGeneratorClient();
        initMediaPlayer();
    }

    @Override
    protected void initData() {
        // initData 主要用于初始化一次性 UI 和监听器

        // 恢复 ViewModel 中的状态
        if (model.isStoryGenerating.getValue()) {
            startGeneratorAnimation();
            translationYImageView(TRANSLATION_Y_GENERATOR_HIDDEN, true);
            translationYPlayImageView(TRANSLATION_Y_PLAY_HIDDEN, true);
        } else {
            // 初始状态或恢复后的状态
            translationYImageView(TRANSLATION_Y_GENERATOR_VISIBLE, true);
            translationYPlayImageView(TRANSLATION_Y_PLAY_HIDDEN, true);

            ObjectAnimator.ofFloat(binding.storyScrollView, "alpha", 1, 0f)
                    .setDuration(ANIM_DURATION_STORY_ALPHA).start();
        }

        // --- 监听器设置 ---
        binding.generatorButtonTextView.setOnClickListener(v -> handleGenerateButtonClick());
        binding.playButtonImageView.setOnClickListener(v -> audioPlayer(model.audioPath.getValue()));
        binding.settingsImageView.setOnClickListener(this::handleSettingsClick);
        binding.historyImageView.setOnClickListener(this::handleHistoryClick);
    }

    @Override
    protected ActivityMainBinding bindingInflate() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    // --- MediaPlayer 初始化 ---
    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(mp -> {
                // 播放完成后的处理
                mediaPlayer.stop();
                mediaPlayer.reset();
                binding.playButtonImageView.setImageResource(R.drawable.play_icon);
                isPausedByManual = false;
                if (playAnimator != null) {
                    playAnimator.cancel();
                }
            });
        }
    }

    // --- 事件处理方法 ---

    private void handleGenerateButtonClick() {
        if (model.isStoryGenerating.getValue()) {
            return;
        }

        // 停止并重置当前播放
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            binding.playButtonImageView.setImageResource(R.drawable.play_icon);
        }

        // UI 变化和状态更新
        ObjectAnimator.ofFloat(binding.storyScrollView, "alpha", binding.storyScrollView.getAlpha(), 0f)
                .setDuration(ANIM_DURATION_STORY_ALPHA).start();

        model.isStoryGenerating.setValue(true); // 更新 ViewModel 状态

        translationYImageView(TRANSLATION_Y_GENERATOR_HIDDEN, false);
        translationYPlayImageView(TRANSLATION_Y_PLAY_HIDDEN, false);

        startGeneratorAnimation();
        requestStory();
    }

    private void handleSettingsClick(View view) {
        ObjectAnimator.ofFloat(view, "rotation", 0, 360)
                .setDuration(ANIM_DURATION_TRANSLATION).start();
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    private void handleHistoryClick(View view) {
        ObjectAnimator.ofFloat(view, "rotation", 0, 360)
                .setDuration(ANIM_DURATION_TRANSLATION).start();
        startActivity(new Intent(MainActivity.this, HistoryActivity.class));
    }


    private void requestStory() {
        // 调用异步请求方法
        RequestStoryModel requestModel = new RequestStoryModel("", Locale.getDefault().toLanguageTag());
        storyGeneratorClient.generateStoryAsync(requestModel, new StoryGeneratorClient.StoryCallback() {
            @Override
            public void onSuccess(String storyContent, String themeUsed) {
                // UI 线程操作
                String display = getString(R.string.title) + themeUsed + "\n\n" + getString(R.string.story) + storyContent;
                ObjectAnimator.ofFloat(binding.storyScrollView, "alpha", 0f, 1f)
                        .setDuration(ANIM_DURATION_STORY_ALPHA).start();
                binding.storyTextView.setText(display);

                // 开始 TTS 生成
                GeneratorTTS.handleTTS(MainActivity.this, settingsConfig.isMan(),
                        Locale.getDefault().toLanguageTag(), display, "", path -> {
                            // TTS 成功，进行后续处理
                            handleStoryGenerationSuccess(storyContent, themeUsed, path);
                        }, R.string.generator_story);
            }

            @Override
            public void onFailure(String errorMessage) {
                // UI 线程操作
                handleStoryGenerationFailure(errorMessage);
            }
        });
    }

    /**
     * TTS 和故事生成成功后的逻辑
     */
    private void handleStoryGenerationSuccess(String storyContent, String themeUsed, String audioPath) {
        // 取消动画，恢复按钮
        cancelGeneratorAnimation();

        // 更新 ViewModel 和 Activity 状态
        model.isStoryGenerating.setValue(false);
        model.audioPath.setValue(audioPath);

        // 1. 数据模型创建和更新
        StoryModel storyModel = new StoryModel();
        storyModel.setContent(storyContent);
        storyModel.setId(System.currentTimeMillis());
        storyModel.setTitle(themeUsed);
        storyModel.setAudioPath(audioPath);
        storyModel.setTime(DateUtils.convertMillisToDateTime(storyModel.getId()));

        // 2. 历史记录列表加载和保存
        StoryModelList storyModelList = StorageUtils.getData(this, "history", StoryModelList.class);
        if (storyModelList == null) {
            storyModelList = new StoryModelList(new ArrayList<>());
        }
        storyModelList.getStoryModels().add(0, storyModel); // 新的故事放在最前面
        StorageUtils.saveData(this, "history", storyModelList);
    }

    /**
     * 故事生成失败后的逻辑
     */
    private void handleStoryGenerationFailure(String errorMessage) {
        cancelGeneratorAnimation();
        model.isStoryGenerating.setValue(false);
        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }


    // --- 动画封装方法 ---

    private void startTranslationYAnimator(Object target, float fromY, float toY, boolean immediate) {
        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationY", fromY, toY);
        animator.setDuration(immediate ? 0 : ANIM_DURATION_TRANSLATION);
        animator.start();
    }

    private void translationYImageView(int translationY, boolean immediate) {
        // 获取当前位置作为 fromY，以确保动画平滑衔接
        float currentY = binding.generatorButtonBgImageView.getTranslationY();

        startTranslationYAnimator(binding.generatorButtonBgImageView, currentY, translationY, immediate);
        startTranslationYAnimator(binding.generatorButtonImageView, currentY, translationY, immediate);
        startTranslationYAnimator(binding.generatorButtonTextView, currentY, translationY, immediate);

        lastTranslationY = translationY;
    }

    private void translationYPlayImageView(int playTranslationY, boolean immediate) {
        float currentY = binding.playButtonBgImageView.getTranslationY();

        startTranslationYAnimator(binding.playButtonBgImageView, currentY, playTranslationY, immediate);
        startTranslationYAnimator(binding.playButtonImageView, currentY, playTranslationY, immediate);

        // 历史和设置按钮的逻辑
        float targetY = (playTranslationY == TRANSLATION_Y_PLAY_HIDDEN) ? playTranslationY : TRANSLATION_Y_PLAY_VISIBLE;

        startTranslationYAnimator(binding.historyImageView, binding.historyImageView.getTranslationY(), targetY, immediate);
        startTranslationYAnimator(binding.settingsImageView, binding.settingsImageView.getTranslationY(), targetY, immediate);

        lastPlayTranslationY = playTranslationY;
    }

    /**
     * 启动生成按钮的旋转和闪烁动画
     */
    private void startGeneratorAnimation() {
        // 清理旧动画
        if (generatorIconAnimatorSet != null) generatorIconAnimatorSet.cancel();

        ObjectAnimator alphaText = ObjectAnimator.ofFloat(binding.generatorButtonTextView, "alpha", 1, 0.3f);
        ObjectAnimator rotateText = ObjectAnimator.ofFloat(binding.generatorButtonTextView, "rotation", 0, 360);
        ObjectAnimator alphaButton = ObjectAnimator.ofFloat(binding.generatorButtonImageView, "alpha", 1, 0.3f);
        ObjectAnimator rotateButton = ObjectAnimator.ofFloat(binding.generatorButtonImageView, "rotation", 0, 360);

        // 统一设置重复模式和次数
        for (ObjectAnimator animator : new ObjectAnimator[]{alphaText, rotateText, alphaButton, rotateButton}) {
            animator.setDuration(1000);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(REVERSE);
        }

        generatorIconAnimatorSet = new AnimatorSet();
        generatorIconAnimatorSet.playTogether(alphaText, rotateText, alphaButton, rotateButton);
        generatorIconAnimatorSet.start();
    }

    /**
     * 取消生成按钮动画并重置 UI 状态
     */
    private void cancelGeneratorAnimation() {
        if (generatorIconAnimatorSet != null) {
            generatorIconAnimatorSet.cancel();
        }

        // 直接设置最终状态（无需 100ms 动画）
        binding.generatorButtonTextView.setAlpha(1f);
        binding.generatorButtonTextView.setRotation(0f);
        binding.generatorButtonImageView.setAlpha(1f);
        binding.generatorButtonImageView.setRotation(0f);

        // 恢复按钮位置
        translationYImageView(TRANSLATION_Y_GENERATOR_VISIBLE, false);
        translationYPlayImageView(TRANSLATION_Y_PLAY_VISIBLE, false);
    }

    /**
     * 启动播放按钮的旋转动画
     */
    private void startPlayButtonAnimation() {
        if (playAnimator != null) {
            playAnimator.cancel();
        }
        playAnimator = ObjectAnimator.ofFloat(binding.playButtonBgImageView, "rotation", 0, 360)
                .setDuration(ANIM_DURATION_ROTATE_PLAY); // 10秒一圈，更舒缓
        playAnimator.setRepeatMode(ValueAnimator.RESTART);
        playAnimator.setRepeatCount(ValueAnimator.INFINITE);
        playAnimator.start();
    }

    // --- MediaPlayer 播放逻辑 ---
    public void audioPlayer(String path) {
        if (mediaPlayer == null || path == null || path.isEmpty()) return;

        if (mediaPlayer.isPlaying()) {
            // 1. 当前正在播放 -> 暂停
            if (playAnimator != null) playAnimator.cancel();
            isPausedByManual = true; // 标记为手动暂停
            mediaPlayer.pause();
            binding.playButtonImageView.setImageResource(R.drawable.play_icon);
        } else if (isPausedByManual) {
            // 2. 处于手动暂停状态 -> 恢复播放
            startPlayButtonAnimation();
            isPausedByManual = false;

            // --- 优化点：恢复播放时设置速度 ---
            try {
                // 此时处于 Paused 状态，可以安全设置速度
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(settingsConfig.getSpeed()));
                mediaPlayer.start();
            } catch (IllegalStateException e) {
                System.err.println("Error setting speed during resume: " + e.getMessage());
                mediaPlayer.start(); // 至少开始播放
            }
            // ----------------------------------

            binding.playButtonImageView.setImageResource(R.drawable.pause_icon);
        } else {
            // 3. 停止或未播放状态 -> 加载新音频并播放
            try {
                startPlayButtonAnimation();
                binding.playButtonImageView.setImageResource(R.drawable.pause_icon);

                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();

                // --- 优化点：在 prepare() 之后，start() 之前设置速度 ---
                // 此时处于 Prepared 状态，可以安全设置速度
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(settingsConfig.getSpeed()));
                // --------------------------------------------------------

                mediaPlayer.start();
            } catch (Exception e) {
                System.err.println("MediaPlayer Error: " + e.getMessage());
                Toast.makeText(this, "播放音频失败", Toast.LENGTH_SHORT).show();
                // 失败后重置 UI 和状态
                binding.playButtonImageView.setImageResource(R.drawable.play_icon);
                if (playAnimator != null) playAnimator.cancel();
            }
        }
    }

    // --- 生命周期管理 ---

    @Override
    protected void onResume() {
        super.onResume();
        // 集中在 onResume 中加载/更新配置
        settingsConfig = StorageUtils.getData(this, "config", SettingsConfig.class);
        if (settingsConfig == null) {
            settingsConfig = new SettingsConfig();
        }

        // 如果是正在播放的状态恢复（例如从锁屏或设置页面返回），继续动画
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            startPlayButtonAnimation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 1. 取消所有动画，防止内存泄漏
        if (generatorIconAnimatorSet != null) generatorIconAnimatorSet.cancel();
        if (playAnimator != null) playAnimator.cancel();

        // 2. 释放 MediaPlayer 资源
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release(); // 释放原生资源
            mediaPlayer = null;
        }
    }
}