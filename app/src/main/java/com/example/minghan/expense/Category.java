package com.example.minghan.expense;

/**
 * Created by MingHan on 6/1/2017.
 */

public class Category {

    private String id;
    private String cat_name;

    public Category(String cat_name, String id){
        this.cat_name = cat_name;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCat_name() {
        return cat_name;
    }

    public void setCat_name(String cat_name) {
        this.cat_name = cat_name;
    }

}
