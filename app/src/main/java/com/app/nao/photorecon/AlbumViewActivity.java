package com.app.nao.photorecon;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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

import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.model.entity.SegmentedPhoto;
import com.app.nao.photorecon.model.repository.LocalFileUtil;
import com.app.nao.photorecon.model.usecase.LoadAllPhotoResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class Thumbnail{
    public List<String> thumbnailImageUris; // 複数の画像 URI グループの文字列形式
    public List<String> thumbnailClassnameLists;
    Thumbnail(List<String> thumbnailImageUris,List<String> thumbnailClassnameLists) {
        this.thumbnailClassnameLists=thumbnailClassnameLists;
        this.thumbnailImageUris=thumbnailImageUris;
    }
}

public class AlbumViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Photo> mPhotoList;
    // service
    private LoadAllPhotoResult mLoadAllPhotoResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        //方法1. IDからソースファイルの場所を取得する
        mLoadAllPhotoResult = new LoadAllPhotoResult();
        mPhotoList = mLoadAllPhotoResult.getAllPhotoResult();
        ArrayList<String> sourceImageUris = new ArrayList<>();
        ArrayList<Thumbnail> thumbnails = new ArrayList<>();

        //ここ絶対遅いので方法2のほうがいい気がする
        for(Photo photo: mPhotoList){

            ArrayList<String> _reconImagesUris = new ArrayList<>();
            ArrayList<String> _segmentedClassStr = new ArrayList<>();

            sourceImageUris.add(photo.getSourceOriginalUri());
            List<SegmentedPhoto> segmentedPhoto = photo.getRecon_list();
            File reconImageDirectory=new File(getFilesDir(),photo.getRecon_list_uri());
            // ＊このfilesは絶対パスになるので直接使わないで
            File[] files = reconImageDirectory.listFiles();
            for(int x =0;x<files.length;x++){
                _reconImagesUris.add(photo.getRecon_list_uri()+"/"+x+".JPEG");
                // TODO:分類後のオブジェクトがあってるか確認する．
                _segmentedClassStr.add(segmentedPhoto.get(x).getCategorization_name());
            }
            thumbnails.add(new Thumbnail(_reconImagesUris,_segmentedClassStr));

        }
        /* 方法2. ディレクトリ内のファイルおよびサブディレクトリをリストアップ
        //この方法だと絶対パスが入って扱いにくくなるため使用しない
        File appDirectory = new File(getFilesDir(), LocalFileUtil.LOCAL_THUMBNAILS_FILE_DIRECTORY);
        File[] files = appDirectory.listFiles();
        if (files != null) {
            for (File _directory : files) {
                // sourceImageUris.add(null);
                if (_directory.isDirectory()) {
                    ArrayList<String> reconImageUrls = new ArrayList<>();
                    for (File _file : _directory.listFiles()) {
                        // ファイルの場合
                        String filePath = _file.getAbsolutePath();
                        String fileUrl =  filePath; // ファイルのURLを生成
                        reconImageUrls.add(fileUrl); // URLをリストに追加
                    }
                    allReconImageUrls.add(reconImageUrls);
                }
            }
        }
        */

        recyclerView = findViewById(R.id.ContainerRecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter((new ObjectListAdapter(this,sourceImageUris,thumbnails)));
    }

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
                                .into(imageView);
                    } else {
                        Log.i("s","s");
                    }
                
                RecyclerView thumbnailRecyclerView;
                thumbnailRecyclerView = itemView.findViewById(R.id.ContainerThumbnailRecycleView);
                thumbnailRecyclerView
                        .setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                ThumbnailListAdapter adapter = new ThumbnailListAdapter(context,thumbnail);
                thumbnailRecyclerView.setAdapter(adapter);
            }

            //debug
            public void setObjects(Thumbnail thumbnails, AssetManager assetManager) {
                try {
                    // Assetから画像をロードしてImageViewに設定
                    InputStream inputStream = assetManager.open("test1.png");
                    Drawable drawable = Drawable.createFromStream(inputStream, null);
                    imageView.setImageDrawable(drawable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                RecyclerView thumbnailRecyclerView;
                thumbnailRecyclerView = itemView.findViewById(R.id.ContainerThumbnailRecycleView);
                thumbnailRecyclerView
                        .setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                ThumbnailListAdapter adapter = new ThumbnailListAdapter(context);
                thumbnailRecyclerView.setAdapter(adapter);
            }
        }
    }

    protected class ThumbnailListAdapter extends RecyclerView.Adapter<ThumbnailListAdapter.ThumbnailViewHolder> {

        private Context context;
        private Thumbnail thumbnail;
        private AssetManager assetManager;
        protected ThumbnailListAdapter(Context context, Thumbnail thumbnail){
            this.context = context;
            this.thumbnail = thumbnail;
        }

        //debug
        protected ThumbnailListAdapter(Context context) {
            this.context = context;
            this.assetManager = context.getAssets();
            String[] str = { "string1", "String2" };
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
            //prod
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
                }
                objectTextView.setText(className);
            }

            //debug
            public void setObjects(String object, AssetManager assetManager) {
                try {
                    InputStream inputStream = assetManager.open("test3.png");
                    Drawable drawable = Drawable.createFromStream(inputStream, null);
                    objectImageView.setImageDrawable(drawable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                objectTextView.setText(object);
            }
        }
    }
}
