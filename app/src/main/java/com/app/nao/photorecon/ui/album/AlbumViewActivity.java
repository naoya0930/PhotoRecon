package com.app.nao.photorecon.ui.album;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.nao.photorecon.R;
import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.model.entity.SegmentedPhoto;
import com.app.nao.photorecon.model.usecase.LoadAllPhotoResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class AlbumViewActivity extends AppCompatActivity {

    private RecyclerView mAlbumRecyclerView;
    private List<Photo> mPhotoList;
    private LoadAllPhotoResult mLoadAllPhotoResult;
    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SubAppTheme);
        setContentView(R.layout.activity_album_view);

        mLoadAllPhotoResult = new LoadAllPhotoResult();
        mPhotoList = mLoadAllPhotoResult.getAllPhotoResult();


        mAlbumRecyclerView = findViewById(R.id.ContainerRecycleView);
        mAlbumRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFloatingActionButton =findViewById(R.id.filterMenu);
        mFloatingActionButton.setOnClickListener(new FloatingButtonListener(this));

        mAlbumRecyclerView.setAdapter((new AlbumViewComponentAdapter(this, mPhotoList)));

    }

}