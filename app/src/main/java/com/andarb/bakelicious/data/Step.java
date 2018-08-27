package com.andarb.bakelicious.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * POJO for one step in a Recipe.
 */
public class Step implements Parcelable {

    private int id;
    private String shortDescription;
    private String description;
    private String videoURL;
    private String thumbnailURL;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }


    /**
     * Parcelable interface implementation.
     */

    // Store values in a Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(shortDescription);
        out.writeString(description);
        out.writeString(videoURL);
        out.writeString(thumbnailURL);
    }

    // Read values previously stored in a Parcel
    private Step(Parcel in) {
        id = in.readInt();
        shortDescription = in.readString();
        description = in.readString();
        videoURL = in.readString();
        thumbnailURL = in.readString();
    }

    // Not applicable in our case, so return `0`
    @Override
    public int describeContents() {
        return 0;
    }

    // Helper constant and methods for creating a Parcel
    public static final Parcelable.Creator<Step> CREATOR
            = new Parcelable.Creator<Step>() {

        @Override
        public Step createFromParcel(Parcel in) {
            return new Step(in);
        }

        @Override
        public Step[] newArray(int size) {
            return new Step[size];
        }
    };
}
