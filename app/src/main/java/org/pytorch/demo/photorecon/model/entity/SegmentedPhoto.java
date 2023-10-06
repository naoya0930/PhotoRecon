package org.pytorch.demo.photorecon.model.entity;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class SegmentedPhoto extends RealmObject {
    @Required private int categorization_id;
    private String categorization_name;
    // recognized image local uri
    private RealmList<String> recon_uri;
    //detected info from source image
    @Required private float x;
    @Required private float y;
    @Required private float w;
    @Required private float h;
    public SegmentedPhoto(int categorization_id) {
        this.categorization_id = categorization_id;
    }
    public SegmentedPhoto(){ }
    
    public int getCategorization_id() {
        return categorization_id;
    }
    public void setCategorization_id(int categorization_id) {
        this.categorization_id = categorization_id;
    }
    public String getCategorization_name() {
        return categorization_name;
    }
    public void setCategorization_name(String categorization_name) {
        this.categorization_name = categorization_name;
    }
    public RealmList<String> getRecon_uri() {
        return recon_uri;
    }
    public void setRecon_uri(RealmList<String> recon_uri) {
        this.recon_uri = recon_uri;
    }
    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }
    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }
    public float getW() {
        return w;
    }
    public void setW(float w) {
        this.w = w;
    }
    public float getH() {
        return h;
    }
    public void setH(float h) {
        this.h = h;
    }
}
