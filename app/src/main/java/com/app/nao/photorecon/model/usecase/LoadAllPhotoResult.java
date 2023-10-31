package com.app.nao.photorecon.model.usecase;

import com.app.nao.photorecon.model.dao.RealmDAO;
import com.app.nao.photorecon.model.entity.Photo;

import java.util.List;


public class LoadAllPhotoResult extends RealmDAO<Photo> {
    public List<Photo> getAllPhotoResult(){
        return super.read_all_entity(super.realmConf,Photo.class);
    }
}
