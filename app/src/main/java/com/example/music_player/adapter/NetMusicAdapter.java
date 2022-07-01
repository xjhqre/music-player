package com.example.music_player.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.example.music_player.R;
import com.example.music_player.application.MyApplication;
import com.example.music_player.model.MusicBean;
import com.example.music_player.utils.ToastUtils;

import java.util.List;

public class NetMusicAdapter extends RecyclerView.Adapter<NetMusicAdapter.MusicViewHolder> {

    Context context;
    List<MusicBean> mDatas;

    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClick(View view, int position);
    }

    public NetMusicAdapter(Context context, List<MusicBean> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_net_music, parent, false);
        MusicViewHolder holder = new MusicViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if (mDatas == null) return;
        MusicBean musicBean = mDatas.get(position);
        holder.songTv.setText(musicBean.getTitle());
        holder.singerTv.setText(musicBean.getArtist());
        holder.albumTv.setText(musicBean.getAlbum());
        holder.itemView.setOnClickListener(v -> onItemClickListener.OnItemClick(v, position));
        long id = MyApplication.getInstance().getUser().getId();

        if (MyApplication.getInstance().getDbOpenHelper().searchNetMusicExistById(String.valueOf(id), musicBean.getSongId())) {
            musicBean.setLike(true);
            holder.heart.setBackgroundResource(R.drawable.ic_heart_fill);
        }

        holder.heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicBean.isLike()) { // 取消喜欢
                    musicBean.setLike(false);
                    MyApplication.getInstance().getDbOpenHelper().unlikeNetMusic(String.valueOf(MyApplication.getInstance().getUser().getId()), musicBean.getSongId());
                    holder.heart.setBackgroundResource(R.drawable.ic_heart);
                    ToastUtils.show("取消收藏");
                    return;
                }
                long id = MyApplication.getInstance().getUser().getId();
                if (MyApplication.getInstance().getDbOpenHelper().searchNetMusicExistById(String.valueOf(id), musicBean.getSongId())) {  // 已存在数据库里musicBean.setLike(true);
                    MyApplication.getInstance().getDbOpenHelper().relikeNetMusic(String.valueOf(MyApplication.getInstance().getUser().getId()), musicBean.getSongId());
                    holder.heart.setBackgroundResource(R.drawable.ic_heart_fill);
                } else { // 数据库里不存在，新加入

                    // 网络音乐设置文件缓存
                    HttpProxyCacheServer proxy = MyApplication.getProxy();
                    String proxyUrl = proxy.getProxyUrl(musicBean.getPath());
                    if (proxyUrl != null) {
                        musicBean.setProxyUrl(proxyUrl);
                    }
                    MyApplication.getInstance().getDbOpenHelper().addNetMusic(String.valueOf(MyApplication.getInstance().getUser().getId()), musicBean);
                    holder.heart.setBackgroundResource(R.drawable.ic_heart_fill);
                    ToastUtils.show("已添加到我喜欢的音乐");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mDatas == null) return 0;
        return mDatas.size();
    }

    class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView songTv, singerTv, albumTv;
        ImageButton heart;

        public MusicViewHolder(View itemView) {
            super(itemView);
            songTv = itemView.findViewById(R.id.item_net_music_song);
            singerTv = itemView.findViewById(R.id.item_net_music_singer);
            albumTv = itemView.findViewById(R.id.item_net_music_album);
            heart = itemView.findViewById(R.id.item_net_music_heart);
        }
    }
}

