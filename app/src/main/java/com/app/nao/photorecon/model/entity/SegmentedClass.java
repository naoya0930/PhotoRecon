package com.app.nao.photorecon.model.entity;

import java.util.List;

public class SegmentedClass {
    String modelName;
    List<String> classname;
    public SegmentedClass(){}
    public SegmentedClass(String modelName, List<String> classname) {
        this.modelName = modelName;
        this.classname = classname;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<String> getClassname() {
        return classname;
    }

    public void setClassname(List<String> classname) {
        this.classname = classname;
    }
}
