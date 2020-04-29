package com.gowild.eremite.am;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

class PersistenceParcel implements Parcelable {

    private Bundle mBundle;
    private String mEreActivityClassName;
    private String mClassLoader;
    private int mVersion;

    public PersistenceParcel(int version, String activityClassName,String classLoader, Bundle bundle) {
        mVersion = version;
        mEreActivityClassName = activityClassName;
        mClassLoader = classLoader;
        mBundle = bundle;
    }

    public PersistenceParcel(Parcel parcel) {
        mVersion = parcel.readInt();
        mEreActivityClassName = parcel.readString();
        mClassLoader = parcel.readString();
        mBundle = parcel.readBundle();
    }

    public String getClassName(){
        return mEreActivityClassName;
    }

    public Bundle getBundle(){
        return mBundle;
    }

    public String getClassLoader(){
        return mClassLoader;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mVersion);
        dest.writeString(mEreActivityClassName);
        dest.writeString(mClassLoader);
        dest.writeBundle(mBundle);
    }

    public static final Creator<PersistenceParcel> CREATOR = new Creator<PersistenceParcel>() {
        @Override
        public PersistenceParcel createFromParcel(Parcel in) {
            return new PersistenceParcel(in);
        }

        @Override
        public PersistenceParcel[] newArray(int size) {
            return new PersistenceParcel[size];
        }
    };
}