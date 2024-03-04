package com.app.nao.photorecon.model.usecase;

import com.app.nao.photorecon.model.entity.Photo;
import com.app.nao.photorecon.model.entity.SegmentedPhoto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


// TODO:愚直に組んでみるが，アルゴリズム確認して！
public class FilterPhotoBySegmentedName {

    public static List<Photo> filterPhotoBySegmentedName(List<Photo> photoList, Set<CharSequence> objNames){
        List<Photo> output =new ArrayList<>();
        for(Photo photo: photoList){
            boolean isInclude =false;
            for(SegmentedPhoto segmentedPhoto: photo.getRecon_list()){
                if (objNames.contains(segmentedPhoto.getCategorization_name())) {
                    isInclude = true;
                    break;
                }
            }
            if(isInclude){output.add(photo);}
        }
        return output;
    }
}
