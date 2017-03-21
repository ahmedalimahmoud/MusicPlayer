package com.banda.android.musicplayer;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.banda.android.musicplayer.Models.SongInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicHolder> {
    private static final String ACTION_DATA_UPDATED =
            "com.banda.android.musicplayer.ACTION_DATA_UPDATED";
    private final Activity host;
    private final LayoutInflater inflater;
    public static List<SongInfo> songInfoList = new ArrayList<>();

    public MusicAdapter(Activity activity, List<SongInfo> songInfo) {
        host = activity;
        songInfoList = songInfo;
        inflater = LayoutInflater.from(host);
        updateWidgets();
    }


    String getIDPosition(int position) {
        return String.valueOf(songInfoList.get(position).getId());
    }

    @Override
    public MusicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MusicHolder(inflater.inflate(R.layout.music_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final MusicHolder holder, final int position) {

        holder.title.setText(songInfoList.get(position).getTitle());
        holder.subtitle.setText(songInfoList.get(position).getArtist());
        if (songInfoList.get(position).getCover() != null) {
            Picasso.
                    with(host).
                    load("file://" + Uri.parse(songInfoList.get(position).getCover())).noFade().resize(220, 220).placeholder(R.drawable.loading).error(R.drawable.music1)
                    .into(holder.avatar);
        } else {
            Picasso.with(host).load(R.drawable.music1).placeholder(R.drawable.loading).into(holder.avatar);
        }
    }
    interface MusicAdapterOnClickHandler {
    }

    class MusicHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.avatar)
        ImageView avatar;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.subtitle)
        TextView subtitle;

        public MusicHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(host, DetailsActivity.class);
            intent.putExtra(DetailsActivity.EXTRA_SONGS, getAdapterPosition());
            host.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
                    host, avatar, avatar.getTransitionName()).toBundle());

        }
    }
    @Override
    public int getItemCount() {
        return songInfoList.size();
    }

    private void updateWidgets() {
        Context context = host.getApplicationContext();
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}


