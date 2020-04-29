package com.gowild.eremite.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gowild.eremite.am.EreActivityInfo;
import com.gowild.eremite.am.EreActivityManager;

/**
 * 本类就是用来监控EreActivity行为的
 *
 * @author Simon
 * @data: 2017/3/7 15:58
 * @version: V1.0
 */


public class Instrumentation {

    private final Object mSync = new Object();
    private EreActivityThread mThread;
    private Context mAppContext;
    private ComponentName mComponent;

    public Instrumentation() {
    }


    final void init(EreActivityThread thread, Context appContext, ComponentName component) {
        mThread = thread;
        mAppContext = appContext;
        mComponent = component;
    }

    public EreActivityResult exeStartEreActivity(Context context, EreActivityInfo resultTo, Intent intent, int requestCode, Bundle
            options) {
        int resultCode = EreActivityManager.getEAM().startEreActivity(intent, resultTo, requestCode, 0, options);
        return new EreActivityResult(resultCode, null);
    }

    public EreActivity newActivity(String className,
                                   Intent intent) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        Class ereActivityClass = Class.forName(className);
        return (EreActivity) ereActivityClass.newInstance();
    }

    public void callActivityOnCreate(EreActivity activity,Bundle bundle){
        activity.performCreate(bundle);
    }

    public void callActivityOnResume(EreActivity activity){
        activity.performResume();
    }

    public void callActivityOnRestart(EreActivity activity){
        activity.performRestart();
    }

    public void callActivityOnNewIntent(EreActivity activity,Bundle bundle){
        activity.performNewIntent(bundle);
    }

    public void callActivityOnPause(EreActivity activity) {
        activity.performPause();
    }

    public void callActivityOnStop(EreActivity activity){
        activity.performStop();
    }

    public void callActivityOnDestory(EreActivity activity){
        activity.performDestory();
    }

    public static final class EreActivityResult {
        private final int mResultCode;
        private final Intent mResultData;

        public EreActivityResult(int resultCode, Intent resultData) {
            mResultCode = resultCode;
            mResultData = resultData;
        }

        public int getResultCode() {
            return mResultCode;
        }

        public Intent getResultData() {
            return mResultData;
        }
    }


}
