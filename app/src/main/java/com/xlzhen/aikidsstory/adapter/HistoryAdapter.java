package com.xlzhen.aikidsstory.adapter;

import android.content.Context;
import android.media.MediaPlayer;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.xlzhen.aikidsstory.R;
import com.xlzhen.aikidsstory.adapter.base.GenericUniversalAdapter;
import com.xlzhen.aikidsstory.models.StoryModel;

import java.util.List;

public class HistoryAdapter extends GenericUniversalAdapter<StoryModel> {

    private MediaPlayer mp;

    private boolean playPause;
    private long playAudioId;
    private final float speed;

    /**
     * 构造函数
     *
     * @param context     Context
     * @param dataList    要显示的数据列表
     * @param layoutResId 列表项布局的资源ID (R.layout.xxx)
     */
    public HistoryAdapter(Context context, List<StoryModel> dataList, int layoutResId, float speed) {
        super(context, dataList, layoutResId);
        this.speed = speed;
    }

    private StoryModel getItemById(long id) {
        for (StoryModel model : dataList) {
            if (model.getId() == id) {
                return model;
            }
        }
        return null;
    }

    public void audioPlayer(StoryModel item) {
        if (mp == null) {
            mp = new MediaPlayer();
            mp.setOnCompletionListener(mediaPlayer -> {
                mp.stop();
                //playButtonImageView.setImageResource(R.drawable.play_icon);
                StoryModel model = getItemById(playAudioId);
                if (model != null) {
                    model.setPlaying(false);
                    notifyDataSetChanged();
                }
                playPause = false;

            });
        }
        if (playAudioId == item.getId()) {
            if (mp.isPlaying()) {
                playPause = true;
                mp.pause();
                item.setPlaying(false);
                //playButtonImageView.setImageResource(R.drawable.play_icon);
                return;
            } else if (playPause) {
                playPause = false;
                mp.start();
                item.setPlaying(true);
                //playButtonImageView.setImageResource(R.drawable.pause_icon);
                return;
            }
        } else {
            if (mp.isPlaying()) {
                mp.stop();
            }
        }

        playAudioId = item.getId();
        try {
            item.setPlaying(true);
            //playButtonImageView.setImageResource(R.drawable.pause_icon);
            mp.reset();
            mp.setDataSource(item.getAudioPath());
            mp.prepare();
            mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(speed));
            mp.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    @Override
    protected void bindView(ViewHolder viewHolder, StoryModel item, int position) {
        AppCompatTextView storyTitleView = viewHolder.findViewById(R.id.story_title_view);
        storyTitleView.setText(item.getTitle());
        AppCompatTextView timeView = viewHolder.findViewById(R.id.story_time_view);
        timeView.setText(item.getTime());
        AppCompatImageView playButtonImageView = viewHolder.findViewById(R.id.play_button_image_view);
        playButtonImageView.setImageResource(item.isPlaying() ? R.drawable.pause_icon : R.drawable.play_icon);
        playButtonImageView.setOnClickListener(v -> {
            audioPlayer(item);
            notifyDataSetChanged();
        });
    }

    public void stopPlay() {
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp = null;
        }
    }
}
