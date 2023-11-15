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
    private List<Photo> mFilterdPhotoList;
    private LoadAllPhotoResult mLoadAllPhotoResult;
    private FloatingActionButton mFloatingActionButton;
    private AlbumViewComponentAdapter mAlbumViewComponentAdapter;

    List<Photo> getAllPhotoList(){
        return mPhotoList;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SubAppTheme);
        setContentView(R.layout.activity_album_view);

        mLoadAllPhotoResult = new LoadAllPhotoResult();
        if(mPhotoList==null){
            mPhotoList = mLoadAllPhotoResult.getAllPhotoResult();
        }


        mAlbumRecyclerView = findViewById(R.id.ContainerRecycleView);
        mAlbumRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAlbumViewComponentAdapter = new AlbumViewComponentAdapter(this,mPhotoList);
        mAlbumRecyclerView.setAdapter(mAlbumViewComponentAdapter);
        mFloatingActionButton =findViewById(R.id.filterMenu);
        mFloatingActionButton.setOnClickListener(new FloatingButtonListener(this, mPhotoList));

    }
    public void updateRecyclerView(List<Photo> photoList){
        // this.mPhotoList = photoList;
        mFilterdPhotoList = photoList;
        mAlbumViewComponentAdapter.setPhotoList(mFilterdPhotoList);
        mAlbumViewComponentAdapter.notifyDataSetChanged();
    }

}