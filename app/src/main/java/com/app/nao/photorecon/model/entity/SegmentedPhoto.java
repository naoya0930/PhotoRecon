package com.app.nao.photorecon.model.entity;

import android.graphics.Rect;

import java.net.URI;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class SegmentedPhoto extends RealmObject {
    
    private int categorization_id;
    private String categorization_name;
    // recognized image local uri
    // ディレクトリ構成で対応する．uriはいらない．
    //private URI recon_image_uri;
    //detected info from source image
    // private Rect rect;
    private int left;
    private int top;
    private int width;
    private int height;


    private float score;

    public SegmentedPhoto() {}

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
    public void setRect(Rect rect){
        this.left = rect.left;
        this.top = rect.top;
        this.width = rect.width();
        this.height = rect.height();

    }
    public Rect getRect(){
        return new Rect(this.left,this.top,this.width,this.height);
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }


}
