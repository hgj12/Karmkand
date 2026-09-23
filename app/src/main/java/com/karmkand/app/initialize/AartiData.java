package com.karmkand.app.initialize;

import java.io.Serializable;

public class AartiData implements Serializable {

    private String id;
    private String name;
    private String aartiDesc;
    private int isHindi = 0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAartiDesc() {
        return aartiDesc;
    }

    public void setAartiDesc(String aartiDesc) {
        this.aartiDesc = aartiDesc;
    }

    public int getIsHindi() {
        return isHindi;
    }

    public void setIsHindi(int isHindi) {
        this.isHindi = isHindi;
    }
}
