package com.banda.android.musicplayer;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.banda.android.musicplayer.Models.AlbumInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumHolder> {

    private final Activity host;
    private final LayoutInflater inflater;
    private List<AlbumInfo> albumInfoList = new ArrayList<>();

    public AlbumAdapter(Activity activity, List<AlbumInfo> albumInfo) {
        host = activity;
        this.albumInfoList = albumInfo;
        inflater = LayoutInflater.from(host);
    }

    @Override
    public AlbumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlbumHolder(inflater.inflate(R.layout.album_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final AlbumHolder holder, final int position) {

        if (albumInfoList.get(position).getAlbumCover() != null) {
            Picasso.
                    with(host).
                    load("file://" + Uri.parse(albumInfoList.get(position).getAlbumCover())).noFade().resize(220, 220).placeholder(R.drawable.loading).error(R.drawable.album)
                    .into(holder.avatar);
        } else {
            Picasso.with(host).load(R.drawable.album).placeholder(R.drawable.loading).into(holder.avatar);
        }
    }
    interface AlbumAdapterOnClickHandler {
    }

    class AlbumHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.avatar)
        ImageView avatar;

        public AlbumHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            AlbumInfo albumInfo = albumInfoList.get(adapterPosition);
            Intent intent = new Intent(host, MainActivity.class);
            intent.putExtra("album", albumInfo);
            host.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
                    host, avatar, avatar.getTransitionName()).toBundle());
        }
    }


    @Override
    public int getItemCount() {
        return albumInfoList.size();
    }
}

