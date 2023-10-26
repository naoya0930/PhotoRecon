package com.app.nao.photorecon.model.entity;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

import org.bson.types.ObjectId;

import java.net.URI;
import java.util.List;

// local
public class Photo extends RealmObject {
    // unique ID
    @PrimaryKey private ObjectId id;
    // source image local uri
    private String uri;
    // recognized image list
    private RealmList<SegmentedPhoto> recon_list;

    private String recon_list_uri;
    // model name forwarded by
    private String model_name;

    public Photo() {}

    public ObjectId getId() {
        return id;
    }
    public void setId() {
        this.id = new ObjectId();
    }
    public String getUri() {
        return uri;
    }
    public void setUri(URI uri) {
        this.uri = uri.toString();
    }
    public void setUri(String uri){ this.uri = uri;}
    public List<SegmentedPhoto> getRecon_list() {
        return recon_list;
    }
    public void setRecon_list(List<SegmentedPhoto> recon_list) {
        // トランザクション中で呼び出しが必要なので，切り出す．;
        this.recon_list = new RealmList<SegmentedPhoto>(recon_list.toArray(
                new SegmentedPhoto[recon_list.size()]));
    }
    public void setRecon_list(RealmList<SegmentedPhoto> recon_list){
        this.recon_list = recon_list;
    }
    public String getModel_name() {
        return model_name;
    }
    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }
    public String getRecon_list_uri() {
        return recon_list_uri;
    }
    public void setRecon_list_uri(String recon_list_uri) {
        this.recon_list_uri = recon_list_uri;
    }
}




