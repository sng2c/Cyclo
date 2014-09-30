package com.mabook.android.cyclo.core.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sng2c on 2014. 9. 30..
 */
public class CycloTrack implements Parcelable {

    public static final Parcelable.Creator<CycloTrack> CREATOR
            = new Parcelable.Creator<CycloTrack>() {
        public CycloTrack createFromParcel(Parcel in) {
            return new CycloTrack(in);
        }

        public CycloTrack[] newArray(int size) {
            return new CycloTrack[size];
        }
    };

    private long id;

    private long mSessionId;
    private String mRegTime;
    private float mAccuracy;
    private double mLatitude;
    private double mLongitude;
    private double mAltitude;
    private double mSpeed;

    public CycloTrack(long id, long sessionId, String regtime, float accuracy, double lat, double lng, double alt, double speed) {
        this.id = id;
        mSessionId = sessionId;
        mRegTime = regtime;
        mAccuracy = accuracy;
        mLatitude = lat;
        mLongitude = lng;
        mAltitude = alt;
        mSpeed = speed;
    }

    private CycloTrack(Parcel in) {
        id = in.readLong();
        mSessionId = in.readLong();
        mRegTime = in.readString();
        mAccuracy = in.readFloat();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mAltitude = in.readDouble();
        mSpeed = in.readDouble();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSessionId() {
        return mSessionId;
    }

    public void setSessionId(long mSessionId) {
        this.mSessionId = mSessionId;
    }

    public String getRegDate() {
        return mRegTime;
    }

    public void setRegTime(String mRegTime) {
        this.mRegTime = mRegTime;
    }

    public float getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(float mAccuracy) {
        this.mAccuracy = mAccuracy;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(double mAltitude) {
        this.mAltitude = mAltitude;
    }

    public double getSpeed() {
        return mSpeed;
    }

    public void setSpeed(double mSpeed) {
        this.mSpeed = mSpeed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeLong(mSessionId);
        parcel.writeString(mRegTime);
        parcel.writeFloat(mAccuracy);
        parcel.writeDouble(mLatitude);
        parcel.writeDouble(mLongitude);
        parcel.writeDouble(mAltitude);
        parcel.writeDouble(mSpeed);
    }
}
