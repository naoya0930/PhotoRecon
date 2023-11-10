package com.app.nao.photorecon.ui.album;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.app.nao.photorecon.model.entity.SegmentedPhoto;
import com.app.nao.photorecon.model.usecase.DeletePhotoFromLocalFile;
import com.app.nao.photorecon.model.usecase.DeletePhotoFromRealm;

import org.bson.types.ObjectId;

public class ThumbnailTapComponentAdapter implements View.OnClickListener{
    private Context context;
    private BoxPaintView mBoxPaintView;
    private SegmentedPhoto mSegmentedPhoto;
    ThumbnailTapComponentAdapter(Context context, SegmentedPhoto segmentedPhoto, BoxPaintView boxPaintView){
        this.context = context;
        this.mSegmentedPhoto = segmentedPhoto;
        this.mBoxPaintView = boxPaintView;

    }
    @Override
    public void onClick(View v) {
        Log.i("Action","サムネイルがタップされました．");
        if(mSegmentedPhoto==null) { return;
        } else {
            //通常のコンストラクタは使わないほうが良い？
            mBoxPaintView.setSegmentedPhoto(mSegmentedPhoto);
            mBoxPaintView.invalidate();
            mBoxPaintView.setVisibility(View.VISIBLE);
            Log.i("Action","描写が完了しました．");
        };
    }
}
