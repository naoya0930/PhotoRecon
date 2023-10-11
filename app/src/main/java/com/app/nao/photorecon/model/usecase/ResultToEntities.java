package com.app.nao.photorecon.model.usecase;

import android.graphics.Bitmap;
import android.graphics.Paint;

import com.app.nao.photorecon.PrePostProcessor;
import com.app.nao.photorecon.model.dao.RealmDAO;
import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.Result;
import com.app.nao.photorecon.model.entity.SegmentedClass;
import com.app.nao.photorecon.model.entity.SegmentedPhoto;

import org.bson.types.ObjectId;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;


public class ResultToEntities extends RealmDAO<Photo> {
    public Photo resultToPhoto(
            ArrayList<Result> res,
            URI source_image_uri,
            SegmentedClass mSegmentedClass,
            String model_name
            ) {
        ArrayList<SegmentedPhoto> ph = new ArrayList<SegmentedPhoto>();
        ArrayList<String> clss = new ArrayList<>(mSegmentedClass.getClassname());

        for(Result r: res){
            // categorized nameを持ってくる
            SegmentedPhoto p = new SegmentedPhoto();
            p.setCategorization_id(r.getClassIndex());
            p.setCategorization_name(clss.get(r.getClassIndex()));
            p.setRect(r.getRect());
            p.setScore(r.getScore());
            ph.add(p);
        }
        Photo photo =new Photo();
        photo.setId();
        photo.setUri(source_image_uri);
        photo.setRecon_list(new ArrayList<SegmentedPhoto>(ph));
        photo.setModel_name(model_name);

        return photo;
    }
}
/*
    private int categorization_id;
    private String categorization_name;
    // recognized image local uri
    private RealmList<String> recon_image_uri;
    //detected info from source image
    private Rect rect;

    private float score;
 */