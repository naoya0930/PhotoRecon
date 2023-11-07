package com.app.nao.photorecon.ui.album;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.nao.photorecon.R;
import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.model.entity.SegmentedPhoto;
import com.app.nao.photorecon.model.usecase.LoadAllPhotoResult;

import java.util.List;

public class AlbumViewActivity extends AppCompatActivity {

    private RecyclerView mAlbumRecyclerView;
    private List<Photo> mPhotoList;
    private LoadAllPhotoResult mLoadAllPhotoResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        mLoadAllPhotoResult = new LoadAllPhotoResult();
        mPhotoList = mLoadAllPhotoResult.getAllPhotoResult();

        mAlbumRecyclerView = findViewById(R.id.ContainerRecycleView);
        mAlbumRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAlbumRecyclerView.setAdapter((new AlbumViewComponentAdapter(this, mPhotoList)));

    }

}