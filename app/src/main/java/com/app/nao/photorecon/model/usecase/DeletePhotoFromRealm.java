package com.app.nao.photorecon.model.usecase;

import com.app.nao.photorecon.model.dao.RealmDAO;
import com.app.nao.photorecon.model.entity.Photo;

import org.bson.types.ObjectId;

import io.realm.RealmObject;

public class DeletePhotoFromRealm extends RealmDAO {
    public RealmObject deleteAlbumFromRealm(ObjectId id) {
        return super.deleteEntityByStringPrimaryKey(super.realmConf, id, Photo.class);
    }
}