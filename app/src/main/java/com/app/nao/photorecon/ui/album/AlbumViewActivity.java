package com.app.nao.photorecon.ui.album;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.nao.photorecon.R;
import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.model.entity.SegmentedPhoto;
import com.app.nao.photorecon.model.usecase.LoadAllPhotoResult;
import com.app.nao.photorecon.ui.util.ScreenInfo;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class AlbumViewActivity extends AppCompatActivity {

    private RecyclerView mAlbumRecyclerView;
    private List<Photo> mPhotoList;

    private ArrayList<String> mSourceImageUris;
    private ArrayList<String> mRecognitionDate;
    private ArrayList<Thumbnail> mThumbnails;

    // private int mScreenWidth;
    private LoadAllPhotoResult mLoadAllPhotoResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);


        //イメージのサイズを取得
        // mScreenWidth = ScreenInfo.getDisplaySize(this).x;

        mLoadAllPhotoResult = new LoadAllPhotoResult();
        mPhotoList = mLoadAllPhotoResult.getAllPhotoResult();
        mSourceImageUris = new ArrayList<>();
        mRecognitionDate =new ArrayList<>();
        mThumbnails = new ArrayList<>();
        // ファイル走査してセット．
        // TODO:UIのスレッドでやっているけど問題なし？
        setFiles();

        mAlbumRecyclerView = findViewById(R.id.ContainerRecycleView);
        mAlbumRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAlbumRecyclerView.setAdapter((new AlbumViewComponentAdapter(this,mSourceImageUris, mThumbnails, mRecognitionDate)));
    }

    private void setFiles(){
        for(Photo photo: mPhotoList){

            ArrayList<String> _reconImagesUris = new ArrayList<>();
            ArrayList<String> _segmentedClassStr = new ArrayList<>();

            mSourceImageUris.add(photo.getSourceOriginalUri());
            mRecognitionDate.add(photo.getSaved_at());
            List<SegmentedPhoto> segmentedPhoto = photo.getRecon_list();
            File reconImageDirectory=new File(getFilesDir(),photo.getRecon_list_uri());
            // ＊このfilesは絶対パスになるので直接使わないで
            File[] files = reconImageDirectory.listFiles();
            for(int x =0;x<files.length;x++){
                _reconImagesUris.add(photo.getRecon_list_uri()+"/"+x+".JPEG");
                _segmentedClassStr.add(segmentedPhoto.get(x).getCategorization_name());
            }
            mThumbnails.add(new Thumbnail(_reconImagesUris,_segmentedClassStr));
        }
    }
/*
    protected class ObjectListAdapter extends RecyclerView.Adapter<ObjectListAdapter.ObjectViewHolder> {

        private Context context;
        private AssetManager assetManager;
        private List<String> originalImageUris; // 1 つの画像 URI の文字列形式
        private List<Thumbnail> mThumbnails;
        protected ObjectListAdapter(Context context, List<String> originalImageUris,List<Thumbnail> thumbnails) {
            this.context = context;
            this.assetManager = context.getAssets();
            this.originalImageUris = originalImageUris;
            this.mThumbnails =thumbnails;
        }

        @Override
        public ObjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_listview, parent, false);

            return new ObjectViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ObjectViewHolder holder, int position) {
            Thumbnail thumbnail = mThumbnails.get(position);
            String originalImageUri = originalImageUris.get(position);
            holder.setObjects(thumbnail,originalImageUri);
        }

        @Override
        public int getItemCount() {
            return originalImageUris.size();
        }

        public class ObjectViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;

            public ObjectViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
            //prod

            public void setObjects(Thumbnail thumbnail, String originalImageUri){
                // Assetから画像をロードしてImageViewに設定
                File imageDirectory = new File(getFilesDir(), originalImageUri);
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
                                .override(mScreenWidth)
                                .into(imageView);
                    } else {
                        //TODO: 画像が見つからないときの対応
                        Log.w("TEST","missing original lImage!");
                    }
                
                RecyclerView thumbnailRecyclerView;
                thumbnailRecyclerView = itemView.findViewById(R.id.ContainerThumbnailRecycleView);
                thumbnailRecyclerView
                        .setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                ThumbnailListAdapter adapter = new ThumbnailListAdapter(context,thumbnail);
                thumbnailRecyclerView.setAdapter(adapter);
            }
        }
    }
*/
/*
    protected class ThumbnailListAdapter extends RecyclerView.Adapter<ThumbnailListAdapter.ThumbnailViewHolder> {

        private Context context;
        private Thumbnail thumbnail;
        private AssetManager assetManager;
        protected ThumbnailListAdapter(Context context, Thumbnail thumbnail){
            this.context = context;
            this.thumbnail = thumbnail;
        }


        @Override
        public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_thumnaillist, parent, false);
            return new ThumbnailViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ThumbnailViewHolder holder, int position) {
            String segmentImageUri =thumbnail.thumbnailImageUris.get(position);
            String segmentClassName = thumbnail.thumbnailClassnameLists.get(position);
            holder.setObjects(segmentImageUri,segmentClassName);

        }

        @Override
        public int getItemCount() {
            //TODO:ユニークでない書き方しているの修正．クラス側にメソッドいれても良い．
            return thumbnail.thumbnailImageUris.size();
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
                File imageFile = new File(getFilesDir(), imageUri);
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

 */

}
