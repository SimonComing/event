package com.gowild.eremite.app;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;

import com.gowild.eremite.am.EreActivityInfo;
import com.gowild.eremite.am.EreActivityManager;
import com.gowild.eremite.util.SuperNotCalledException;

import java.util.List;

/**
 * TODO<Simon说说这个类是干什么的呗>
 *
 * @author Simon
 * @data: 2017/3/7 15:29
 * @version: V1.0
 */


public class EreActivityThread {

    final H mH = new H();

    private final Context mContext;

    Instrumentation mInstrumentation = null;

    final ArrayMap<EreActivityInfo, EreActivity> mActivities
            = new ArrayMap<EreActivityInfo, EreActivity>();

    public EreActivityThread(Context context) {
        mContext = context;
        attach();
    }

    private void attach() {
        mInstrumentation = new Instrumentation();
    }


    private class H extends Handler {
        public static final int LAUNCH_ACTIVITY = 100;
        public static final int RELAUNCH_ACTIVITY = 101;
        public static final int RESUME_ACTIVITY = 102;
        public static final int PAUSE_ACTIVITY = 103;
        public static final int STOP_ACTIVITY = 104;
        public static final int SEND_RESULT = 108;
        public static final int DESTROY_ACTIVITY = 109;
        public static final int NEWINTENT_ACTIVITY = 110;
        public static final int LOW_MEMORY = 124;

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case LAUNCH_ACTIVITY: {
                    handleLaunchActivity((EreAcitivityClient) msg.obj);
                }
                break;
                case RELAUNCH_ACTIVITY: {
                    handleRelaunchActivity((EreAcitivityClient) msg.obj);
                }
                break;
                case RESUME_ACTIVITY: {

                }
                break;
                case PAUSE_ACTIVITY: {
                    handlePauseActivity(msg.obj);
                }
                break;
                case STOP_ACTIVITY: {
                    handleStopActivity(msg.obj);
                }
                break;
                case DESTROY_ACTIVITY: {
                    destoryEreActivity((EreActivityInfo)msg.obj);
                }
                break;
                case NEWINTENT_ACTIVITY:{
                    handleNewIntentActivity((EreAcitivityClient)msg.obj);
                }
                break;
            }
        }

    }

    static final class ResultData {
        String token;
        List<ResultInfo> results;

        public String toString() {
            return "ResultData{token=" + token + " results" + results + "}";
        }
    }

    public final void scheduleSendResult(String token, List<ResultInfo> results) {
        ResultData res = new ResultData();
        res.token = token;
        res.results = results;
        sendMessage(H.SEND_RESULT, res);
    }

    public final void schedulePauseActivity(Object target) {
        sendMessage(H.PAUSE_ACTIVITY, target);
    }

    public final void scheduleStopActivity(Object target) {
        sendMessage(H.STOP_ACTIVITY, target);
    }

    private void handlePauseActivity(Object object) {
        if (object == null) {
            return;
        }
        EreActivityInfo ereActivityInfo = (EreActivityInfo) object;
        Log.d(EreActivityThread.class.getSimpleName(),"info : "+ereActivityInfo.name);
        if(mActivities.containsKey(ereActivityInfo)){
            mInstrumentation.callActivityOnPause(mActivities.get(ereActivityInfo));
            // 通知暂停完成，开始后续操作
            EreActivityManager.getEAM().activityPaused(this, ereActivityInfo);
        }
    }

    public EreActivity getEreActivity(EreActivityInfo ereActivityInfo){
        return mActivities.get(ereActivityInfo);
    }

    private void handleStopActivity(Object object) {
        if (object == null) {
            return;
        }
        EreActivityInfo ereActivityInfo = (EreActivityInfo) object;
        if(mActivities.containsKey(ereActivityInfo)){
            mInstrumentation.callActivityOnStop(mActivities.get(ereActivityInfo));
        }
    }

    public final void  scheduleLaunchActivity(Intent intent, EreActivityInfo target) {
        sendLaunchMessage(intent,target,false);
    }

    public final void scheduleRelaunchActivity(Intent intent, EreActivityInfo target) {
        sendLaunchMessage(intent,target,true);
    }

    public final void scheduleNewIntentActivity(Intent intent, EreActivityInfo target){
        EreAcitivityClient ereAcitivityClient = new EreAcitivityClient();
        ereAcitivityClient.ereActivityInfo = target;
        ereAcitivityClient.intent = intent;
        sendMessage(H.NEWINTENT_ACTIVITY,ereAcitivityClient);
    }

    public final void scheduleDestoryActivity(EreActivityInfo target) {
        sendMessage(H.DESTROY_ACTIVITY, target);
    }

    private void sendLaunchMessage(Intent intent, EreActivityInfo target, boolean isRestart) {
        EreAcitivityClient ereAcitivityClient = new EreAcitivityClient();
        ereAcitivityClient.ereActivityInfo = target;
        ereAcitivityClient.intent = intent;
        sendMessage(isRestart ? H.RELAUNCH_ACTIVITY : H.LAUNCH_ACTIVITY, ereAcitivityClient);
    }

    private void handleLaunchActivity(EreAcitivityClient ereAcitivityClient) {
        Intent intent = ereAcitivityClient.intent;
        EreActivityInfo ereActivityInfo = ereAcitivityClient.ereActivityInfo;
        EreActivity ereActivity = null;
        try {
            ereActivity = mInstrumentation.newActivity(ereActivityInfo.name, intent);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to instantiate activity " + ereActivityInfo.name
                            + ": " + e.toString(), e);
        }
        mActivities.put(ereActivityInfo,ereActivity);
        ereActivity.attach(mInstrumentation, this, mContext, ereActivityInfo, intent);
        ereActivity.mCalled = false;
        mInstrumentation.callActivityOnCreate(ereActivity, intent.getExtras());
        if (!ereActivity.mCalled) {
            throw new SuperNotCalledException(
                    "Activity " + ereActivityInfo.name +
                            " did not call through to super.onCreate()");
        }
        if (!ereActivity.mFinished) {
            mInstrumentation.callActivityOnResume(ereActivity);
        }
    }

    private void handleRelaunchActivity(EreAcitivityClient ereAcitivityClient) {
        Intent intent = ereAcitivityClient.intent;
        EreActivityInfo ereActivityInfo = ereAcitivityClient.ereActivityInfo;
        EreActivity ereActivity = mActivities.get(ereActivityInfo);
        if(ereActivity ==null){
            return;
        }
        ereActivity.attachEvent();
        ereActivity.setIntent(intent);
        mInstrumentation.callActivityOnRestart(ereActivity);
        if (!ereActivity.mCalled) {
            throw new SuperNotCalledException(
                    "Activity " + ereActivityInfo.name +
                            " did not call through to super.onCreate()");
        }
        if (!ereActivity.mFinished) {
            mInstrumentation.callActivityOnResume(ereActivity);
        }
    }

    private void handleNewIntentActivity(EreAcitivityClient ereAcitivityClient){
        Intent intent = ereAcitivityClient.intent;
        EreActivityInfo ereActivityInfo = ereAcitivityClient.ereActivityInfo;
        if(mActivities.containsKey(ereActivityInfo)){
            EreActivity ereActivity = mActivities.get(ereActivityInfo);
            ereActivity.setIntent(intent);
            mInstrumentation.callActivityOnNewIntent(ereActivity,intent.getExtras());
        }
    }

    final void destoryEreActivity(EreActivityInfo ereActivityInfo){
        if (ereActivityInfo == null) {
            return;
        }
        if(mActivities.containsKey(ereActivityInfo)){
            mInstrumentation.callActivityOnDestory(mActivities.get(ereActivityInfo));
            mActivities.remove(ereActivityInfo);
        }
    }

    private void sendMessage(int what, Object obj) {
        sendMessage(what, obj, 0, 0, false);
    }

    private void sendMessage(int what, Object obj, int arg1, int arg2, boolean async) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        mH.sendMessage(msg);
    }


    private void handleSendResult(ResultData res) {

    }

    static final class EreAcitivityClient {
        Intent intent;
        EreActivityInfo ereActivityInfo;
    }
}
