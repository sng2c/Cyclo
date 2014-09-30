package com.mabook.android.cyclo.core.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sng2c on 2014. 9. 30..
 */
public class CycloSession implements Parcelable {

    public static final Parcelable.Creator<CycloSession> CREATOR
            = new Parcelable.Creator<CycloSession>() {
        public CycloSession createFromParcel(Parcel in) {
            return new CycloSession(in);
        }

        public CycloSession[] newArray(int size) {
            return new CycloSession[size];
        }
    };

    private long id;
    private String mPackageName;
    private String mAppName;
    private String mSessionName;
    private String mStartTime;
    private String mEndTIme;

    public CycloSession(long id, String mPackageName, String mAppName, String mSessionName, String mStartTime, String mEndTIme) {
        this.id = id;
        this.mPackageName = mPackageName;
        this.mAppName = mAppName;
        this.mSessionName = mSessionName;
        this.mStartTime = mStartTime;
        this.mEndTIme = mEndTIme;
    }

    private CycloSession(Parcel in) {
        id = in.readLong();
        mPackageName = in.readString();
        mAppName = in.readString();
        mSessionName = in.readString();
        mStartTime = in.readString();
        mEndTIme = in.readString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String mAppName) {
        this.mAppName = mAppName;
    }

    public String getSessionName() {
        return mSessionName;
    }

    public void setSessionName(String mSessionName) {
        this.mSessionName = mSessionName;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String mStartTime) {
        this.mStartTime = mStartTime;
    }

    public String getEndTIme() {
        return mEndTIme;
    }

    public void setEndTIme(String mEndTIme) {
        this.mEndTIme = mEndTIme;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(mPackageName);
        parcel.writeString(mAppName);
        parcel.writeString(mSessionName);
        parcel.writeString(mStartTime);
        parcel.writeString(mEndTIme);
    }
}
