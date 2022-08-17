package com.app.lsnhdsteth.hdsteth;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.androidnetworking.widget.ANImageView;
import com.app.lsnhdsteth.R;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends PagerAdapter {

    Context context;
    List<String> images_path;
    LayoutInflater mLayoutInflater;
    String selected_report_path;
    MediaPlayer player;
    ImageView play_iv;

    ImageAdapter(Context context,ArrayList<String> images_path,String selected_report_path){
        this.context=context;
        this.images_path=images_path;
        this.selected_report_path=selected_report_path;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void changeScroll(){
        if(player != null && player.isPlaying()){
            player.stop();
        }
        if(play_iv!=null){
            play_iv.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    public int getCount() {
        return images_path.size()+1;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_image, container, false);
        ANImageView imageView = itemView.findViewById(R.id.iv_pager_image);
        play_iv = itemView.findViewById(R.id.iv_play);
        TextView play_tv = itemView.findViewById(R.id.tv_play);
        if(position == images_path.size()){
            play_iv.setVisibility(View.VISIBLE);
            play_tv.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
        } else {
            play_iv.setVisibility(View.GONE);
            play_tv.setVisibility(View.GONE);
            imageView.setImageUrl(images_path.get(position));
        }
        play_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(player != null && player.isPlaying()){
                    player.stop();
                    play_iv.setImageResource(R.drawable.ic_play);
                }else{
                    play_iv.setImageResource(R.drawable.ic_pause);
                    playAudio(play_iv);
                }

            }
        });
        container.addView(itemView);
        return itemView;
    }

    public void playAudio(ImageView play_iv){
        try {
            Log.d("TAG", "playAudio: "+selected_report_path+"wav.wav");
            Uri uri = Uri.parse(selected_report_path+"wav.wav");
//            Uri uri = Uri.parse("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(context, uri);
            player.prepare();
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    play_iv.setImageResource(R.drawable.ic_play);
                }
            });
        } catch(Exception e) {
            System.out.println(e.toString());
        }
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)object);
    }

}
