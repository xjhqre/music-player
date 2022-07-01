package com.example.music_player.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_player.R;
import com.example.music_player.application.MyApplication;
import com.example.music_player.model.MusicBean;
import com.example.music_player.utils.ToastUtils;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder>{

    Context context;
    List<MusicBean> mDatas;

    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void OnItemClick(View view,int position);
    }

    public MusicAdapter(Context context, List<MusicBean> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music,parent,false);
        MusicViewHolder holder = new MusicViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        MusicBean musicBean = mDatas.get(position);
        holder.idTv.setText((position + 1) + "");
        holder.songTv.setText(musicBean.getTitle());
        holder.singerTv.setText(musicBean.getArtist());
        holder.albumTv.setText(musicBean.getAlbum());
        holder.itemView.setOnClickListener(v -> onItemClickListener.OnItemClick(v,position));
        holder.heartIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO 通知
                if (musicBean.isLike()) { // 取消喜欢
                    musicBean.setLike(false);
                    MyApplication.getInstance().getDbOpenHelper().unlikeNetMusic(String.valueOf(MyApplication.getInstance().getUser().getId()), musicBean.getSongId());
                    holder.heartIb.setBackgroundResource(R.drawable.ic_heart);
                    ToastUtils.show("取消收藏");
                } else { // 重新喜欢
                    musicBean.setLike(true);
                    MyApplication.getInstance().getDbOpenHelper().relikeNetMusic(String.valueOf(MyApplication.getInstance().getUser().getId()), musicBean.getSongId());
                    holder.heartIb.setBackgroundResource(R.drawable.ic_heart_fill);
                    ToastUtils.show("重新收藏");
                }
            }
        });
        if (musicBean.getType() == 0) { // 本地音乐，隐藏喜欢按钮
            holder.heartIb.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class MusicViewHolder extends RecyclerView.ViewHolder{
        TextView idTv,songTv,singerTv,albumTv;
        ImageButton heartIb;
        public MusicViewHolder(View itemView) {
            super(itemView);
            idTv = itemView.findViewById(R.id.item_local_music_num);
            songTv = itemView.findViewById(R.id.item_local_music_song);
            singerTv = itemView.findViewById(R.id.item_local_music_singer);
            albumTv = itemView.findViewById(R.id.item_local_music_album);
            heartIb = itemView.findViewById(R.id.item_music_heart);
        }
    }
}

