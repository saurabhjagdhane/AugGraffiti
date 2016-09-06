package com.example.saurabh.auggraffiti;


public class Tag {
    private String tagID;
    private double latitude;
    private double longitude;

    public Tag(String tagID, double latitude, double longitude) {
        this.tagID = tagID;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTagID() {
        return tagID;
    }

    public void setTagID(String tagID) {
        this.tagID = tagID;
    }
}
