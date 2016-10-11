/**
 * Tag class represents both tags placed and collected by the user
 * Created for future use (after checkpoint) and implementation.
 */

package com.example.saurabh.auggraffiti;


import android.os.Parcel;
import android.os.Parcelable;

public class Tag implements Parcelable{
    private String tagID;
    private double latitude;
    private double longitude;
    private float azimuth;
    private float altitude;
    private String imageURL;

    public Tag(String tagID, double latitude, double longitude) {
        this.tagID = tagID;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected Tag(Parcel in) {
        tagID = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        imageURL = in.readString();
        azimuth = in.readFloat();
        altitude = in.readFloat();
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };

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

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(tagID);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(imageURL);
        parcel.writeFloat(azimuth);
        parcel.writeFloat(altitude);
    }
}
