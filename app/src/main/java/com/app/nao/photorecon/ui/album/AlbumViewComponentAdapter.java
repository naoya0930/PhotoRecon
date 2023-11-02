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
import com.app.nao.photorecon.ui.util.ScreenInfo;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class AlbumViewComponentAdapter extends RecyclerView.Adapter<AlbumViewComponentAdapter.ObjectViewHolder> {

    private Context context;
    private AssetManager assetManager;
    private List<String> originalImageUris; // 1 つの画像 URI の文字列形式
    private List<String> mCreateAt;
    private List<Thumbnail> mThumbnails;
    private int screenWidth;

    protected AlbumViewComponentAdapter(Context context, List<String> originalImageUris, List<Thumbnail> thumbnails,List<String> createDate) {
        this.context = context;
        this.assetManager = context.getAssets();
        this.originalImageUris = originalImageUris;
        this.mThumbnails =thumbnails;
        this.mCreateAt = createDate;
    }

    @Override
    public AlbumViewComponentAdapter.ObjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_listview, parent, false);
        screenWidth = ScreenInfo.getViewSize(view).x;
        return new AlbumViewComponentAdapter.ObjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumViewComponentAdapter.ObjectViewHolder holder, int position) {
        Thumbnail thumbnail = mThumbnails.get(position);
        String originalImageUri = originalImageUris.get(position);
        String reconDate = mCreateAt.get(position);
        holder.setObjects(originalImageUri, reconDate);
        holder.setThumbnailRecyclerView(thumbnail);
    }

    @Override
    public int getItemCount() {
        return originalImageUris.size();
    }

    public class ObjectViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView mDateTextView;
        private RecyclerView thumbnailViewComponentAdapter;

        public ObjectViewHolder(View itemView) {
            super(itemView);
            mDateTextView = itemView.findViewById(R.id.dateText);
            imageView = itemView.findViewById(R.id.imageView);
        }

        public void setObjects(String originalImageUri,String reconDate) {
            // Assetから画像をロードしてImageViewに設定
            mDateTextView.setText(reconDate);
            File imageDirectory = new File(context.getFilesDir(), originalImageUri);
            File imageFile = imageDirectory.listFiles()[0];
            if (imageFile.exists()) {
                Uri imageUri = Uri.fromFile(imageFile);
                // 画像ファイルが存在する場合
                //不具合があるならbitmapに落とす．
                // imageView.setImageURI(imageUri);
                // Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                // imageView.setImageBitmap(bitmap);
                Glide.with(context)
                        .load(imageUri)
                        .override(screenWidth)
                        .into(imageView);
            } else {
                //TODO: 画像が見つからないときの対応
                Log.w("TEST", "missing original lImage!");
            }
        }
        public void setThumbnailRecyclerView(Thumbnail thumbnail) {
            // findbyIDは毎回呼び出す必要がある
            thumbnailViewComponentAdapter = itemView.findViewById(R.id.ContainerThumbnailRecycleView);
            thumbnailViewComponentAdapter
                    .setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            ThumbnailViewComponentAdapter adapter = new ThumbnailViewComponentAdapter(context, thumbnail);
            thumbnailViewComponentAdapter.setAdapter(adapter);
        }
    }
}
