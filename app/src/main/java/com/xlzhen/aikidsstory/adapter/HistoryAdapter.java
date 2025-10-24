package com.xlzhen.aikidsstory.adapter;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.xlzhen.aikidsstory.R;
import com.xlzhen.aikidsstory.adapter.base.GenericUniversalAdapter;
import com.xlzhen.aikidsstory.models.StoryModel;
import com.xlzhen.aikidsstory.models.StoryModelList;
import com.xlzhen.mvvm.storage.StorageUtils;

import java.io.File;
import java.util.List;

public class HistoryAdapter extends GenericUniversalAdapter<StoryModel> {
    private static final int ANIMATOR_TAG = 20102;

    @Override
    public long getItemId(int i) {
        return dataList.get(i).getId();
    }

    // 使用接口回调，将播放和删除逻辑上提到 Activity/Fragment
    public interface OnItemActionListener {
        void onDelete(StoryModel item);

        void onPlayPause(StoryModel item);
    }

    private final float speed;
    private OnItemActionListener itemActionListener; // 新增回调接口

    // 媒体播放器相关：作为 Adapter 的成员变量，便于集中管理播放状态
    private MediaPlayer mp;
    private long playingAudioId = -1; // 当前正在播放/暂停的音频ID
    private boolean isPausedByMe = false; // 是否处于用户暂停状态

    /**
     * 构造函数
     */
    public HistoryAdapter(Context context, List<StoryModel> dataList, int layoutResId, float speed) {
        super(context, dataList, layoutResId);
        this.speed = speed;
        initMediaPlayer();
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.itemActionListener = listener;
    }

    // 初始化 MediaPlayer
    private void initMediaPlayer() {
        mp = new MediaPlayer();
        mp.setOnCompletionListener(mediaPlayer -> {
            // 播放完成
            mp.stop();
            isPausedByMe = false;
            // 通知数据更新
            StoryModel completedModel = getItemById(playingAudioId);
            if (completedModel != null) {
                completedModel.setPlaying(false);
            }
            playingAudioId = -1; // 重置
            notifyDataSetChanged();
        });
    }

    /**
     * 根据 ID 查找 StoryModel
     * 优化：这是一个 O(N) 操作，如果数据量大，可以考虑使用 HashMap 优化，
     * 但对于 Adapter 而言，直接遍历也还可接受。
     */
    private StoryModel getItemById(long id) {
        for (StoryModel model : dataList) {
            if (model.getId() == id) {
                return model;
            }
        }
        return null;
    }

    /**
     * 媒体播放/暂停/切换逻辑
     */
    public void audioPlayer(StoryModel item) {
        if (item.getId() == playingAudioId) {
            // 1. 同一音频：暂停/继续
            if (mp.isPlaying()) {
                mp.pause();
                isPausedByMe = true;
                item.setPlaying(false);
            } else if (isPausedByMe) {
                mp.start();
                isPausedByMe = false;
                item.setPlaying(true);
            }
        } else {
            // 2. 切换音频：停止旧的，播放新的
            if (mp.isPlaying() || isPausedByMe) {
                // 停止旧的播放，并更新旧数据状态
                mp.stop();
                StoryModel oldModel = getItemById(playingAudioId);
                if (oldModel != null) {
                    oldModel.setPlaying(false);
                }
            }

            // 播放新的音频
            playingAudioId = item.getId();
            isPausedByMe = false;

            try {
                item.setPlaying(true);
                mp.reset();
                mp.setDataSource(item.getAudioPath());
                mp.prepare();
                // 播放速度设置
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(speed));
                }
                mp.start();
            } catch (Exception e) {
                System.err.println("播放音频错误: " + e.getMessage());
                item.setPlaying(false); // 播放失败，设置状态为非播放
                playingAudioId = -1;
            }
        }
        // 统一通知刷新
        notifyDataSetChanged();
    }

    @Override
    protected void bindView(CommonViewHolder viewHolder, StoryModel item, int position) {

        // 1. 视图绑定
        AppCompatTextView storyTitleView = viewHolder.findViewById(R.id.story_title_view);
        storyTitleView.setText(item.getTitle());
        AppCompatTextView timeView = viewHolder.findViewById(R.id.story_time_view);
        timeView.setText(item.getTime());
        AppCompatImageView playButtonImageView = viewHolder.findViewById(R.id.play_button_image_view);
        AppCompatImageView playButtonBgImageView = viewHolder.findViewById(R.id.play_button_bg_image_view);
        AppCompatImageView deleteButtonImageView = viewHolder.findViewById(R.id.delete_button_view);

        // 2. 状态更新
        boolean isPlaying = (item.getId() == playingAudioId) && item.isPlaying();
        playButtonImageView.setImageResource(isPlaying ? R.drawable.pause_icon : R.drawable.play_icon);

        // 3. 动画处理 (关键优化)
        RotateAnimationHelper helper = (RotateAnimationHelper) viewHolder.getConvertView().getTag(R.id.main);
        if (helper == null) {
            // 为每个 item view 关联一个动画管理辅助类
            helper = new RotateAnimationHelper(playButtonBgImageView);
            viewHolder.getConvertView().setTag(R.id.main, helper);
        }

        if (isPlaying) {
            helper.start();
        } else {
            helper.stop();
        }

        // 4. 事件监听器

        // 播放按钮
        playButtonImageView.setOnClickListener(v -> {
            // 使用回调接口，避免 Adapter 逻辑过于复杂
            if (itemActionListener != null) {
                itemActionListener.onPlayPause(item);
            } else {
                // 如果没有设置回调，执行默认逻辑
                audioPlayer(item);
            }
        });

        // 删除按钮
        deleteButtonImageView.setOnClickListener(v -> {
            new AlertDialog.Builder(context).setMessage(R.string.delete_story)
                    .setNegativeButton(R.string.confirm, (dialogInterface, i) -> {
                        // 1. 处理播放状态
                        if (item.getId() == playingAudioId) {
                            stopAndReleasePlayer(); // 如果删除的是当前正在播放的，停止播放器
                        }

                        // 2. 删除文件和数据
                        File audioFile = new File(item.getAudioPath());
                        if (audioFile.exists()) {
                            audioFile.delete();
                        }
                        dataList.remove(item);

                        // 3. 持久化数据
                        StoryModelList storyModelList = new StoryModelList(dataList);
                        StorageUtils.saveData(context, "history", storyModelList);

                        // 4. 通知刷新
                        notifyDataSetChanged();

                        // 5. 调用回调接口 (可选，用于 Activity/Fragment 接收删除事件)
                        if (itemActionListener != null) {
                            itemActionListener.onDelete(item);
                        }

                        dialogInterface.dismiss();
                    })
                    .setPositiveButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        });
    }

    /**
     * 停止播放并重置播放器状态。
     */
    public void stopPlay() {
        if (mp != null && (mp.isPlaying() || isPausedByMe)) {
            mp.stop();
            isPausedByMe = false;
            // 更新当前播放项的状态
            StoryModel model = getItemById(playingAudioId);
            if (model != null) {
                model.setPlaying(false);
            }
            playingAudioId = -1;
            notifyDataSetChanged();
        }
    }

    /**
     * 停止并释放 MediaPlayer 资源 (在 Activity/Fragment 销毁时调用)。
     */
    public void stopAndReleasePlayer() {
        stopPlay(); // 先停止
        if (mp != null) {
            mp.release(); // 释放资源
            mp = null;
        }
    }

    /**
     * 辅助类：管理旋转动画
     * 将动画逻辑独立出来，并与 View 绑定，避免 Item 复用时动画错乱。
     */
    private static class RotateAnimationHelper {
        private final View targetView;
        private ObjectAnimator animator;

        public RotateAnimationHelper(View targetView) {
            this.targetView = targetView;
        }

        public void start() {
            if (animator == null) {
                animator = ObjectAnimator.ofFloat(targetView, "rotation", 0, 360)
                        .setDuration(10000);
                animator.setRepeatMode(ValueAnimator.RESTART);
                animator.setRepeatCount(ValueAnimator.INFINITE); // 使用常量
            }
            if (!animator.isRunning()) {
                animator.start();
            }
        }

        public void stop() {
            if (animator != null) {
                animator.cancel();
            }
            // 停止时，将 View 旋转角度重置，避免下次开始时突变
            targetView.setRotation(0);
        }
    }
}