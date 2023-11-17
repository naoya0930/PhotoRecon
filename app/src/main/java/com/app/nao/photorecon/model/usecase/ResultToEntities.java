package com.app.nao.photorecon.model.usecase;

import com.app.nao.photorecon.model.dao.RealmDAO;
import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.ui.main.Result;
import com.app.nao.photorecon.model.entity.SegmentedClass;
import com.app.nao.photorecon.model.entity.SegmentedPhoto;
import com.app.nao.photorecon.model.repository.LocalFileUtil;

import java.util.ArrayList;


public class ResultToEntities extends RealmDAO<Photo> {
    public Photo resultToPhoto(
            ArrayList<Result> res,
            String source_image_uri,
            SegmentedClass mSegmentedClass,
            String model_name
            ) {
        ArrayList<SegmentedPhoto> ph = new ArrayList<SegmentedPhoto>();
        ArrayList<String> clss = new ArrayList<>(mSegmentedClass.getClassname());

        for(Result r: res){
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
        photo.setRecon_list_uri(LocalFileUtil.LOCAL_THUMBNAILS_FILE_DIRECTORY + photo.getId().toString()+"/");
        photo.setSourceOriginalUri(LocalFileUtil.LOCAL_ORIGINAL_FILE_DIRECTORY +  photo.getId()+"/");
        return photo;
    }
}