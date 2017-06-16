package com.example.models;


import java.io.Serializable;

public class BarcodeItem implements Serializable{

    @com.google.gson.annotations.SerializedName("id")
    private String id;

    @com.google.gson.annotations.SerializedName("deleted")
    private boolean deleted;

    @com.google.gson.annotations.SerializedName("lengthPosition")
    private String lengthPosition;

    @com.google.gson.annotations.SerializedName("widthtPosition")
    private String widthtPosition;

    @com.google.gson.annotations.SerializedName("descriptionFirst")
    private String descriptionFirst;

    @com.google.gson.annotations.SerializedName("descriptionSecond")
    private String descriptionSecond;

    @com.google.gson.annotations.SerializedName("name")
    private String name;

    @com.google.gson.annotations.SerializedName("imageLinkFirst")
    private String imageLinkFirst;

    @com.google.gson.annotations.SerializedName("imageLinkSecond")
    private String imageLinkSecond;

    public BarcodeItem() {

    }

    public BarcodeItem(String id, boolean deleted, String lengthPosition, String widthtPosition, String descriptionFirst, String descriptionSecond, String name, String imageLinkFirst, String imageLinkSecond) {
        this.id = id;
        this.deleted = deleted;
        this.lengthPosition = lengthPosition;
        this.widthtPosition = widthtPosition;
        this.descriptionFirst = descriptionFirst;
        this.descriptionSecond = descriptionSecond;
        this.name = name;
        this.imageLinkFirst = imageLinkFirst;
        this.imageLinkSecond = imageLinkSecond;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getLengthPosition() {
        return lengthPosition;
    }

    public void setLengthPosition(String lengthPosition) {
        this.lengthPosition = lengthPosition;
    }

    public String getWidthtPosition() {
        return widthtPosition;
    }

    public void setWidthtPosition(String widthtPosition) {
        this.widthtPosition = widthtPosition;
    }

    public String getDescriptionFirst() {
        return descriptionFirst;
    }

    public void setDescriptionFirst(String descriptionFirst) { this.descriptionFirst = descriptionFirst; }

    public String getDescriptionSecond() {
        return descriptionSecond;
    }

    public void setDescriptionSecond(String descriptionSecond) { this.descriptionSecond = descriptionSecond; }

    public String getName() {return name; }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageLinkFirst() {
        return imageLinkFirst;
    }

    public void setImageLinkFirst(String imageLinkFirst) {
        this.imageLinkFirst = imageLinkFirst;
    }

    public String getImageLinkSecond() {
        return imageLinkSecond;
    }

    public void setImageLinkSecond(String imageLinkSecond) { this.imageLinkSecond = imageLinkSecond; }
}