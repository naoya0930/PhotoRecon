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

import androidx.recyclerview.widget.RecyclerView;

import com.app.nao.photorecon.R;
import com.app.nao.photorecon.model.entity.SegmentedPhoto;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class ThumbnailViewComponentAdapter extends RecyclerView.Adapter<ThumbnailViewComponentAdapter.ThumbnailViewHolder> {

    private Context context;
    // private Thumbnail thumbnail;
    private List<SegmentedPhoto> mSegmentedPhoto;
    private String mSegmentedImageUri;
//    protected ThumbnailViewComponentAdapter(Context context, Thumbnail thumbnail){
//        this.context = context;
//        this.thumbnail = thumbnail;
//    }
    public ThumbnailViewComponentAdapter(Context context, List<SegmentedPhoto> segmentedPhoto,String segmentedImageUri){
        this.context =context;
        this.mSegmentedPhoto = segmentedPhoto;
        this.mSegmentedImageUri = segmentedImageUri;
    }


    @Override
    public ThumbnailViewComponentAdapter.ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_thumnaillist, parent, false);
        return new ThumbnailViewComponentAdapter.ThumbnailViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(ThumbnailViewComponentAdapter.ThumbnailViewHolder holder, int position) {
//        String segmentImageUri =thumbnail.thumbnailImageUris.get(position);
//        String segmentClassName = thumbnail.thumbnailClassnameLists.get(position);
//        holder.setObjects(segmentImageUri,segmentClassName);
//    }
    @Override
    public void onBindViewHolder(ThumbnailViewComponentAdapter.ThumbnailViewHolder holder, int position) {
        //String segmentImageUri =thumbnail.thumbnailImageUris.get(position);
        //TODO: ここをメソッド切り分けするか検討
        String segmentedImageUri = mSegmentedImageUri + "/" + position +".JPEG";
        String segmentClassName = mSegmentedPhoto.get(position).getCategorization_name();
        holder.setObjects(segmentedImageUri,segmentClassName);
    }

    @Override
    public int getItemCount() {
        //TODO:ユニークでない書き方しているの修正．クラス側にメソッドいれても良い．
        return mSegmentedPhoto.size();
    }

    public class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        private TextView objectTextView;
        private ImageView objectImageView;

        public ThumbnailViewHolder(View itemView) {
            super(itemView);
            objectTextView = itemView.findViewById(R.id.objectTextView);
            objectImageView = itemView.findViewById(R.id.objectImageView);
        }
        public void setObjects(String imageUri,String className) {
            File imageFile = new File(context.getFilesDir(), imageUri);
            if (imageFile.getAbsoluteFile().exists()) {
                Uri thumbnailUri = Uri.fromFile(imageFile);
                // objectImageView.setImageURI(thumbnailUri);
                Glide.with(context)
                        .load(thumbnailUri)
                        .into(objectImageView);
            } else {
                // TODO:errorハンドリング
                Log.w("TEST","missing thumbnail Image!");
            }
            objectTextView.setText(className);
        }
    }
}