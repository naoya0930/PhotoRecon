package com.app.nao.photorecon;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.model.repository.LocalFileUtil;
import com.app.nao.photorecon.model.usecase.LoadAllPhotoResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AlbumViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    // private AlbumAdapter adapter;
    private List<Photo> mPhotoList;
    // service
    private LoadAllPhotoResult mLoadAllPhotoResult;

    private String[][] sets = {
            {"Object 1", "Object 2","Object A", "Object B"},
            {"Object 3", "Object 4"},
            {"Object 5", "Object 6"}
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        //画像取得

        // get Photo info
        mLoadAllPhotoResult = new LoadAllPhotoResult();
        mPhotoList = mLoadAllPhotoResult.getAllPhotoResult();

        //TODO: MAP(objectid:reconobj[])形に書き換えたい
        ArrayList<List<String>> allReconImageUrls= new ArrayList<List<String>>();
        // ArrayList <String> reconImageUrls = new ArrayList<>();
        // IDからソースファイルの場所を取得する
        ArrayList<String> sourceImageUrls = new ArrayList<>();

        File appDirectory = new File(getFilesDir(), LocalFileUtil.LOCAL_FILE_DIRECTORY);

        // ディレクトリ内のファイルおよびサブディレクトリをリストアップ
        File[] files = appDirectory.listFiles();
        // IDからソースファイルの場所を取得する

        if (files != null) {
            for (File _directory : files) {
                sourceImageUrls.add(null);

                if (_directory.isDirectory()) {
                    ArrayList <String> reconImageUrls = new ArrayList<>();
                    for(File _file : _directory.listFiles()) {
                        // ファイルの場合
                        String filePath = _file.getAbsolutePath();
                        String fileUrl = "file:/" + filePath; // ファイルのURLを生成
                        reconImageUrls.add(fileUrl); // URLをリストに追加
                        Log.i("TESTX", fileUrl);
                    }
                    allReconImageUrls.add(reconImageUrls);
                }
            }
        }

        recyclerView = findViewById(R.id.ContainerRecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ObjectListAdapter(this,sourceImageUrls,allReconImageUrls));
    }

    protected class ObjectListAdapter extends RecyclerView.Adapter<ObjectListAdapter.ObjectViewHolder> {

        private Context context;
        private AssetManager assetManager;
        private List<String> originalImageUris; // 1 つの画像 URI の文字列形式
        private List<List<String>> thumbnailImageUris; // 複数の画像 URI グループの文字列形式
        protected RecyclerView thumbnailRecyclerView;
        private ThumbnailListAdapter adapter;
        protected ObjectListAdapter(Context context,List<String> originalImageUris, List<List<String>> thumbnailImageUris) {
            this.context = context;
            this.assetManager = context.getAssets();
            this.originalImageUris = originalImageUris;
            this.thumbnailImageUris = thumbnailImageUris;
            this.adapter = new ThumbnailListAdapter(context);
        }


        @Override
        public ObjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_listview, parent, false);

            return new ObjectViewHolder(view);


        }

        @Override
        public void onBindViewHolder(ObjectViewHolder holder, int position) {
            String[] objects = sets[position];
            holder.setObjects(objects, assetManager);
        }

        @Override
        public int getItemCount() {
            return sets.length;
        }

        public class ObjectViewHolder extends RecyclerView.ViewHolder {
            // private LinearLayout objectListLayout;
            private ImageView imageView;
            private RecyclerView thumbnailRecyclerView;

            public ObjectViewHolder(View itemView) {
                super(itemView);
                // objectListLayout = itemView.findViewById(R.id.objectListLayout);
                imageView = itemView.findViewById(R.id.imageView);
                thumbnailRecyclerView =itemView.findViewById(R.id.ContainerThumbnailRecycleView);
                thumbnailRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            }

            public void setObjects(String[] objects, AssetManager assetManager) {
                // objectListLayout.removeAllViews();
                try {
                    // Assetから画像をロードしてImageViewに設定
                    InputStream inputStream = assetManager.open("test1.png");
                    Drawable drawable = Drawable.createFromStream(inputStream, null);
                    imageView.setImageDrawable(drawable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("TEST_?",objects.length+"");
                    // ThumbnailListAdapter adapter = new ThumbnailListAdapter(context);
                    // thumbnailRecyclerView = findViewById(R.id.ContainerThumbnailRecycleView);
                    // thumbnailRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                    // thumbnailRecyclerView.setAdapter(new ThumbnailListAdapter(context,object));
                    thumbnailRecyclerView.setAdapter(adapter);
            }
        }

    }
    ////////////////////////////////

    protected class ThumbnailListAdapter extends RecyclerView.Adapter<ThumbnailListAdapter.ThumbnailViewHolder> {

        private Context context;
        private AssetManager assetManager;
        // protected RecyclerView thumbnailRecyclerView;
        private String[] objects;

        protected ThumbnailListAdapter(Context context,String[] objects) {
            this.context = context;
            this.assetManager = context.getAssets();
            this.objects = objects;
        }
        protected ThumbnailListAdapter(Context context) {
            this.context = context;
            this.assetManager = context.getAssets();
            String[] str ={"string1","String2"};
            this.objects = str;
        }

        @Override
        public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_thumnaillist, parent, false);
            return new ThumbnailViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ThumbnailViewHolder holder, int position) {
            //ここは勝手にListの数渡してくれると考える
            String object = objects[position];
            holder.setObjects(object, assetManager);
        }

        @Override
        public int getItemCount() {
            // return sets.length;
            return objects.length;
        }

        public class ThumbnailViewHolder extends RecyclerView.ViewHolder {
            // private LinearLayout thumbnailListLayout;
            private TextView objectTextView;
            private ImageView objectImageView;

            public ThumbnailViewHolder(View itemView) {
                super(itemView);
                // thumbnailListLayout = itemView.findViewById(R.id.thumbnailListLayout);
                objectTextView = itemView.findViewById(R.id.objectTextView);
                objectImageView = itemView.findViewById(R.id.objectImageView);

            }

            public void setObjects(String object, AssetManager assetManager) {
                // thumbnailListLayout.removeAllViews();

                try {
                    // Assetから画像をロードしてImageViewに設定
                    InputStream inputStream = assetManager.open("test3.png");
                    Drawable drawable = Drawable.createFromStream(inputStream, null);
                    objectImageView.setImageDrawable(drawable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //ここは正常回数呼び出されている
                objectTextView.setText(object);
            }
        }
    }
}
