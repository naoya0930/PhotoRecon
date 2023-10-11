package com.app.nao.photorecon.model.usecase;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.app.nao.photorecon.Result;

import java.net.URI;
import java.util.ArrayList;

public class SnapRectanglePhoto {
    public Bitmap makeSegmentedImage(Bitmap sourceImg, Rect rect){
        return Bitmap.createBitmap(
                sourceImg,
                rect.left,
                rect.top,
                rect.right-rect.left,
                rect.bottom- rect.top
        );
    }
    public ArrayList<Bitmap> makeSegmentedImages(Bitmap sourceImg,ArrayList<Result> results){
        ArrayList<Bitmap> bl=new ArrayList<Bitmap>();
        for(Result result:results){
            Rect rect = result.getRect();
            Bitmap b =Bitmap.createBitmap(
                sourceImg,
                rect.left,
                rect.top,
                rect.right-rect.left,
                rect.bottom- rect.top
            );
            bl.add(b);
        }
        return bl;
    }
}
