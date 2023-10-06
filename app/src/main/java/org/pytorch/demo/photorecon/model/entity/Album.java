package org.pytorch.demo.photorecon.model.entity;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
public class Album extends RealmObject{


    private Date created_at;
    private RealmList<Photo> photoLists;

    public RealmList<Photo> getPhotoLists() {
        return photoLists;
    }
    public void setPhotoList(RealmList<Photo> lst){
        this.photoLists = lst;
    }
    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
    public Album(){}

}
