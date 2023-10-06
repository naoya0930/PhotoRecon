package org.pytorch.demo.photorecon.model.entity;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

import org.bson.types.ObjectId;

// local
public class Photo extends RealmObject {
    // unique ID
    @PrimaryKey private ObjectId id;
    // source image local uri
    @Required private String uri;
    // recognized image list
    private RealmList<SegmentedPhoto> recon_list;
    // model name forwarded by
    private String model_name;
    public ObjectId getId() {
        return id;
    }
    public void setId(ObjectId id) {
        this.id = id;
    }
    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    public RealmList<SegmentedPhoto> getRecon_list() {
        return recon_list;
    }
    public void setRecon_list(RealmList<SegmentedPhoto> recon_list) {
        this.recon_list = recon_list;
    }
    public String getModel_name() {
        return model_name;
    }
    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }
    public Photo() {}
}




