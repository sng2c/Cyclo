package com.mabook.cyclo.core;

import android.location.Criteria;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sng2c on 2014. 9. 18..
 */
public class CycloProfile implements Parcelable {

    public static final Parcelable.Creator<CycloProfile> CREATOR
            = new Parcelable.Creator<CycloProfile>() {
        public CycloProfile createFromParcel(Parcel in) {
            return new CycloProfile(in);
        }

        public CycloProfile[] newArray(int size) {
            return new CycloProfile[size];
        }
    };

    long minTime;
    float minDistance;
    Criteria criteria;

    public CycloProfile() {
    }


    private CycloProfile(Parcel in) {
        minTime = in.readLong();
        minDistance = in.readFloat();
        criteria = in.readParcelable(Criteria.class.getClassLoader());
    }

    public long getMinTime() {
        return minTime;
    }

    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    public float getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(float minDistance) {
        this.minDistance = minDistance;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(minTime);
        parcel.writeFloat(minDistance);
        parcel.writeParcelable(criteria, i);
    }


}
