package com.example.tractorplus.viewobject;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import com.google.gson.annotations.SerializedName;

@Entity(primaryKeys = "id")
public class ItemTownshipLocation {

    @NonNull
    @SerializedName("id")
    public String id;

    @SerializedName("city_id")
    public String cityId;

    @SerializedName("township_name")
    public final String townshipName;

    @SerializedName("ordering")
    public final String ordering;

    @SerializedName("lat")
    public final String lat;

    @SerializedName("lng")
    public final String lng;

    @SerializedName("status")
    public final String status;

    @SerializedName("added_date")
    public final String addedDate;

    @SerializedName("added_date_str")
    public final String addedDateStr;


    public ItemTownshipLocation(@NonNull String id, String cityId, String townshipName, String ordering, String lat, String lng, String status, String addedDate, String addedDateStr) {
        this.id = id;
        this.cityId = cityId;
        this.townshipName = townshipName;
        this.ordering = ordering;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.addedDate = addedDate;
        this.addedDateStr = addedDateStr;

    }
}
