package com.gowild.eremite.am;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.gowild.eremite.app.EreActivity;
import com.gowild.eremite.app.EreActivityThread;
import com.gowild.eremite.app.GowildIntent;
import com.gowild.eremite.event.Event;
import com.gowild.eremite.event.EventManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * TODO<Simon说说这个类是干什么的呗>
 *
 * @author Simon
 * @data: 2017/3/7 17:09
 * @version: V1.0
 */

/**
 * {@hide}
 */
public class EreActivityManager {

    private EreActivityStack ereActivityStack;
    EventManager mEventManager;
    /**
     * EreActivity启动结果：intent没有包含指定class
     */
    public static final int START_WITHOUT_RESULT_OBJECT = -3;

    /**
     * EreActivity启动结果：intent没有包含指定class
     */
    public static final int START_CLASS_NOT_FOUND = -2;

    /**
     * EreActivity启动结果：intent没有定义指定EreActivity
     */
    public static final int START_INTENT_NOT_RESOLVED = -1;

    /**
     * EreActivity启动结果：启动成功
     */
    public static final int START_SUCCESS = 0;

    EreActivityThread ereActivityThread = null;

    private static EreActivityManager eam = new EreActivityManager();

    private HashMap<ComponentName, EreActivityInfo> mActivities = new HashMap<>();
    private HashMap<String, EreActivityInfo> mActionsActivities = new HashMap<>();

    private EreActivityPersistence mEreActivityPersistence;

    private EreActivityManager() {
        ereActivityStack = new EreActivityStack(this);
    }

    public EreActivityManager createEreActivityManager(Context context,HashMap<String, EreActivityInfo> activityInfoHashMap) {
        return createEreActivityManager(context,activityInfoHashMap,"gowild.ereactivity.action.LAUNCH",null);
    }

    public EreActivityManager createEreActivityManager(Context context,HashMap<String, EreActivityInfo> activityInfoHashMap,String launchAction ,String persistencePath) {
        loadEreActivity(activityInfoHashMap);
        ereActivityThread = new EreActivityThread(context);
        ereActivityStack.enterEreActivity(launchAction);
        setmEventManager(EventManager.getEventManager());
        mEreActivityPersistence = new EreActivityPersistence(persistencePath);
        mEreActivityPersistence.startPersistence();
        return eam;
    }

    public EreActivityPersistence getEreActivityPersistence(){
        return mEreActivityPersistence;
    }

    public void setmEventManager(EventManager mEventManager) {
        this.mEventManager = mEventManager;
    }

    public static EreActivityManager getEAM() {
        return eam;
    }

    public final int startEreActivity(Intent intent, EreActivityInfo resultTo, int requestCode, int flags, Bundle options) {
        return ereActivityStack.startActivityMayWait(intent, resultTo, requestCode, flags, options);
    }

    public EreActivityInfo resolveActivity(Intent intent, int flags) {
        ComponentName componentName = intent.getComponent();
        if (componentName != null) {
            return mActivities.get(componentName);
        }
        String action = intent.getAction();
        if (action != null) {
            return mActionsActivities.get(action);
        }
        return null;
    }

    public final void activityPaused(EreActivityThread ereActivityThread, EreActivityInfo ereActivity) {
        synchronized (this) {
            ereActivityStack.ereActivityPauseLocked(ereActivity);
        }
    }

    public final boolean finishActivity(EreActivityInfo ereActivity) {
        synchronized (this) {
            return ereActivityStack.requestFinishActivityLocked(ereActivity);
        }
    }

    private void loadEreActivity(HashMap<String, EreActivityInfo> activityInfoHashMap){
        mActionsActivities = new HashMap<>(activityInfoHashMap);
        for (String key : mActionsActivities.keySet()){
            EreActivityInfo tmp = mActionsActivities.get(key);
            ComponentName componentName = new ComponentName(tmp.packageName, tmp.name);
            if (mActivities.get(componentName) == null){
                mActivities.put(componentName,tmp);
            }else {
                Log.w("EreActivityManager","repeated ComponentName.");
            }
        }
    }

    private void parseEreActivitysManifest(Context context) {
        EreActivityParser parser = new EreActivityParser(context);
        parser.parseBaseInfo(mActivities, mActionsActivities);
    }

    public boolean dispatchEvent(Event event) {
        return mEventManager.callback == null ? false : mEventManager.callback.onEvent(event);
    }

    public EreActivity getTopEreActivity(){
        ArrayList<EreActivityInfo> mHistoryEreActivity = ereActivityStack.mHistoryEreActivity;
        if(mHistoryEreActivity.size() == 0 ){
            Log.w("EreActivityManager","mHistoryEreActivity is empty!");
            return null;
        }
        EreActivity ereActivity = ereActivityThread.getEreActivity(mHistoryEreActivity.get(mHistoryEreActivity.size() - 1));
        return ereActivity;
    }

    public void restoreInstanceState(){
        PersistenceParcel parcel = mEreActivityPersistence.loadPersistence();
        EreActivity ereActivity = getTopEreActivity();
        GowildIntent intent = new GowildIntent();
        if (parcel == null){
            intent.setAction("gowild.ereactivity.action.HOME");
            ereActivity.startActivity(intent);
        }else {
            String className = parcel.getClassName();

            try{
                Iterator<String> iterator = mActionsActivities.keySet().iterator();
                while (iterator.hasNext()){
                    String action = iterator.next();
                    EreActivityInfo ereActivityInfo = mActionsActivities.get(action);
                    if (className.equals(ereActivityInfo.name)){
                        intent.setAction(action);
                        intent.putExtras(parcel.getBundle());
                        ereActivity.startActivity(intent);
                        return;
                    }
                }
            }catch (Exception e){

                intent.setAction("gowild.ereactivity.action.HOME");
                ereActivity.startActivity(intent);

            }

        }
    }
}
