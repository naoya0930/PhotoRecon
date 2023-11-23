package com.app.nao.photorecon.ui.main;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.app.nao.photorecon.ui.album.AlbumViewActivity;


public class ActiveAlbumButtonHandler implements View.OnClickListener{
    Context context;
    ActiveAlbumButtonHandler(Context context){
        this.context=context;
    }
    public void onClick(View v) {
        final Intent intent = new Intent(context, AlbumViewActivity.class);
        context.startActivity(intent);
    }
}
