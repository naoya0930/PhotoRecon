package com.app.nao.photorecon.ui.album;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.nao.photorecon.R;
import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.ui.util.ScreenInfo;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class AlbumViewComponentAdapter extends RecyclerView.Adapter<AlbumViewComponentAdapter.ObjectViewHolder> {

    private Context context;
    private List<Photo> mPhotoList;
    private int screenWidth;

    protected AlbumViewComponentAdapter(Context context, List<Photo> photoList){
        this.context = context;
        this.mPhotoList =photoList;
    }

    @Override
    public AlbumViewComponentAdapter.ObjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_listview, parent, false);
        screenWidth = ScreenInfo.getViewSize(view).x;
        return new AlbumViewComponentAdapter.ObjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumViewComponentAdapter.ObjectViewHolder holder, int position) {
        Photo photo = mPhotoList.get(position);
        holder.setOriginalImage(photo);
        holder.setThumbnailRecyclerView(photo);
    }

    @Override
    // public int getItemCount() {return originalImageUris.size();}
    public int getItemCount() { return mPhotoList.size();}
    public class ObjectViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView mDateTextView;
        private RecyclerView thumbnailViewComponentAdapter;
        private BoxPaintView mBoxPaintView;

        public ObjectViewHolder(View itemView) {
            super(itemView);
            mDateTextView = itemView.findViewById(R.id.dateText);
            imageView = itemView.findViewById(R.id.imageView);
            mBoxPaintView = itemView.findViewById(R.id.boxPaintView);
        }

        public void setThumbnailRecyclerView(Photo photo){
            thumbnailViewComponentAdapter = itemView.findViewById(R.id.ContainerThumbnailRecycleView);
            thumbnailViewComponentAdapter.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            ThumbnailViewComponentAdapter adapter =
                    new ThumbnailViewComponentAdapter(context, photo.getRecon_list(),photo.getRecon_list_uri(),mBoxPaintView);
            // タップ時のviewへの参照させるために，カスタムViewをここで渡しておく．

            thumbnailViewComponentAdapter.setAdapter(adapter);
        }
        protected void setOriginalImage(Photo photo){
            mDateTextView.setText(photo.getSaved_at());
            //クリックした際の挙動をセット
            AlbumImageTapHandler albumImageTapHandler = new AlbumImageTapHandler(context,photo.getId());
            imageView.setOnLongClickListener(albumImageTapHandler);
            // イメージのセット．汎用化できそう
            File imageDirectory = new File(context.getFilesDir(), photo.getSourceOriginalUri());
            File imageFile = imageDirectory.listFiles()[0];
            if (imageFile.exists()) {
                Uri imageUri = Uri.fromFile(imageFile);
                Glide.with(context)
                        .load(imageUri)
                        .override(screenWidth)
                        .into(imageView);
            } else {
                //TODO: 画像が見つからないときの対応
                Log.w("TEST", "missing original lImage!");
            }
        }
    }
}
