package com.gowild.eremite.am;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import com.gowild.eremite.app.EreActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by SimonSun on 2019/3/19.
 */

public class EreActivityPersistence extends HandlerThread {

    private final static int PERSISTENCE_VERSION = -20;
    private final String PERSISTENCE_PATH;

    private PersistenceHandler mPersistenceHandler;

    public EreActivityPersistence(String persistencePath) {
        super("EreActivityPersistence");

        PERSISTENCE_PATH = persistencePath+"/.state";
    }

    public void startPersistence(){
        this.start();
        mPersistenceHandler = new PersistenceHandler(this.getLooper());
    }

    public void saveState(EreActivity ereActivity, Bundle bundle) {
        Message message = Message.obtain();
        message.setData(bundle);
        message.obj = ereActivity;
        mPersistenceHandler.handleMessage(message);
    }

    public void clearState(){
        Message message = Message.obtain();
        message.what = PersistenceHandler.FLAG_CLEAR;
        mPersistenceHandler.handleMessage(message);
    }

    public PersistenceParcel loadPersistence() {

        FileInputStream inputStream = null;
        PersistenceParcel persistenceParcel = null;
        try {
            File file = new File(PERSISTENCE_PATH);
            inputStream = new FileInputStream(file);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(bytes,0,bytes.length);
            parcel.setDataPosition(0);

            int version = parcel.readInt();

            if (version != PERSISTENCE_VERSION){
                file.delete();
                return null;
            }
            String mEreActivityClassName = parcel.readString();
            String mClassLoader = parcel.readString();
            Bundle bundle = parcel.readBundle();
            bundle.setClassLoader(Class.forName(mClassLoader).getClassLoader());

            persistenceParcel = new PersistenceParcel(version, mEreActivityClassName,mClassLoader, bundle);

        } catch (Exception e) {
            //e.printStackTrace();
            Log.d("EreActivityPersistence","error : "+e.toString());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return persistenceParcel;

    }

    private class PersistenceHandler extends Handler {

        public static final int FLAG_CLEAR = -1000;

        public PersistenceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            if (FLAG_CLEAR == msg.what){
                clearState();
                return;
            }

            saveStateInner((EreActivity) msg.obj, msg.getData());
        }

        private void saveStateInner(EreActivity ereActivity, Bundle bundle) {

            String classLoader = bundle.getString("persistableClassLoader","");
            if (TextUtils.isEmpty(classLoader)){
                return;
            }

            Parcel parcel = Parcel.obtain();
            parcel.writeInt(PERSISTENCE_VERSION);
            parcel.writeString(ereActivity.getClass().getCanonicalName());
            parcel.writeString(classLoader);
            parcel.writeBundle(bundle);
            byte[] bytes = parcel.marshall();

            FileOutputStream outputStream = null;
            BufferedOutputStream bos = null;
            try {
                outputStream = new FileOutputStream(PERSISTENCE_PATH);
                bos = new BufferedOutputStream(outputStream);

                bos.write(bytes);
                bos.flush();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void clearState(){
            File file = new File(PERSISTENCE_PATH);
            file.delete();
        }

    }

}
