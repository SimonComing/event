package com.gowild.eremite.am;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.gowild.eremite.abstracts.ActivityInterceptor;
import com.gowild.eremite.abstracts.InterceptorCallback;
import com.gowild.eremite.app.EreActivity;
import com.gowild.eremite.app.GowildIntent;

import java.util.ArrayList;

/**
 * 类似于ActivityStack
 *
 * @author Simon
 * @data: 2017/3/7 17:53
 * @version: V1.0
 */


public class EreActivityStack {

    final EreActivityManager mEAManager;

    EreActivityStack stack;

    EreActivityInfo mPausedEreActivity = null;
    EreActivityInfo mNextEreActivity = null;
    EreActivityInfo mFinishingActivity = null;
    Intent mNextEreActivityIntent = null;

    final ArrayList<EreActivityInfo> mHistoryEreActivity = new ArrayList<>();

    public EreActivityStack(EreActivityManager eaManager) {
        mEAManager = eaManager;
    }

    final int startActivityMayWait(final Intent intent, EreActivityInfo resultTo, int requestCode, int flags, Bundle options) {
        boolean componentSpecified = intent.getComponent() != null;
        int result = EreActivityManager.START_SUCCESS;
//        intent = new Intent(intent);

        final EreActivityInfo targetEreActivityInfo = mEAManager.resolveActivity(intent, flags);
        if (targetEreActivityInfo != null) {
            Log.d("EreActivityStack", "target : " + targetEreActivityInfo.name);
            intent.setComponent(new ComponentName(targetEreActivityInfo.packageName, targetEreActivityInfo.name));
        } else {
            return EreActivityManager.START_INTENT_NOT_RESOLVED;
        }
        if (resultTo == null) {
            return EreActivityManager.START_WITHOUT_RESULT_OBJECT;
        }
        synchronized (mEAManager) {
            EreActivityInfo resultEreActivityInfo = resultTo;
            // 如果低级别的EreActivity企图打断比他级别高的EreActivity会被直接拒绝
            result = targetEreActivityInfo.launchLevel - resultEreActivityInfo.launchLevel;

            final EreActivityInfo topEreActivityInfo = mHistoryEreActivity.get(mHistoryEreActivity.size() - 1);
            if (targetEreActivityInfo == topEreActivityInfo) {
                if (mPausedEreActivity == targetEreActivityInfo) {
                    startSpecificActivityLocked(targetEreActivityInfo);
                    mPausedEreActivity = null;
                } else {
                    startNewIntentEreActivityLocked(intent, targetEreActivityInfo);
                }
            } else {

                ActivityInterceptor interceptor = topEreActivityInfo.targetActivity.getInterceptor();
                if (interceptor != null) {
                    interceptor.process(targetEreActivityInfo, new InterceptorCallback() {
                        @Override
                        public void onContinue() {
                            // 暂停上一个ereActivity
                            mPausedEreActivity = topEreActivityInfo;
                            mNextEreActivity = targetEreActivityInfo;
                            mNextEreActivityIntent = intent;
                            startPausingLocked(topEreActivityInfo);
                        }

                        @Override
                        public void onInterrupt() {

                        }
                    });
                }
                // without interceptor
                else {
                    Log.d("EreActivityStack", "without interceptor");
                    // 暂停上一个ereActivity
                    mPausedEreActivity = topEreActivityInfo;
                    mNextEreActivity = targetEreActivityInfo;
                    mNextEreActivityIntent = intent;
                    startPausingLocked(topEreActivityInfo);
                }

            }

        }

        return result;
    }

    private void startNewIntentEreActivityLocked(Intent intent, EreActivityInfo targetEreActivityInfo) {
        mEAManager.ereActivityThread.scheduleNewIntentActivity(intent, targetEreActivityInfo);
    }

    private void startPausingLocked(EreActivityInfo resultEreActivity) {
        mEAManager.ereActivityThread.schedulePauseActivity(resultEreActivity);
    }

    final void ereActivityPauseLocked(EreActivityInfo pausedEreActivity) {
        if (mPausedEreActivity != null && mPausedEreActivity == pausedEreActivity) {
            completePauseLocked(pausedEreActivity);
        }
        mPausedEreActivity = null;
    }

    private void completePauseLocked(EreActivityInfo pausedEreActivity) {
        mEAManager.mEventManager.interruptEventDispatch();
        mEAManager.ereActivityThread.scheduleStopActivity(pausedEreActivity);
        if (mFinishingActivity != null) {
            mEAManager.ereActivityThread.scheduleDestoryActivity(mFinishingActivity);
            mHistoryEreActivity.remove(mFinishingActivity);
            mFinishingActivity = null;
        }
        //  暂停后判断是否需要启动下一个EreActivity,如果没有指定下一个EreActivity,就重启上一个EreActivity
        mNextEreActivity = mNextEreActivity == null ? mHistoryEreActivity.get(mHistoryEreActivity.size() - 1) : mNextEreActivity;
        if (mNextEreActivity != null) {
            startSpecificActivityLocked(mNextEreActivity);
        }
        mNextEreActivity = null;
    }

    private void startSpecificActivityLocked(EreActivityInfo ereActivityInfo) {
        final int index = mHistoryEreActivity.indexOf(ereActivityInfo);
        if (index != -1) {
            for (int i = index + 1; i < mHistoryEreActivity.size(); ) {
                mEAManager.ereActivityThread.scheduleDestoryActivity(mHistoryEreActivity.get(i));
                mHistoryEreActivity.remove(i);
            }
//            mHistoryEreActivity.remove(index);
//            mHistoryEreActivity.add(ereActivityInfo);
            mEAManager.ereActivityThread.scheduleRelaunchActivity(mNextEreActivityIntent, ereActivityInfo);
        } else {
            realStartActivity(mNextEreActivityIntent, ereActivityInfo);
        }
    }

    private void realStartActivity(Intent intent, EreActivityInfo ereActivityInfo) {
        mEAManager.ereActivityThread.scheduleLaunchActivity(intent, ereActivityInfo);
        mHistoryEreActivity.add(ereActivityInfo);
    }

    final boolean requestFinishActivityLocked(EreActivityInfo ereActivityInfo) {
        // 先暂停本身
        mPausedEreActivity = mFinishingActivity = ereActivityInfo;
        startPausingLocked(ereActivityInfo);
        return true;
    }

    final void enterEreActivity(String action) {
        GowildIntent intent = new GowildIntent();
        intent.setAction(action);
        EreActivityInfo target = mEAManager.resolveActivity(intent, 0);
        realStartActivity(intent, target);
    }

    final void enterEreActivity(String action,Bundle bundle) {
        GowildIntent intent = new GowildIntent();
        intent.setAction(action);
        intent.putExtras(bundle);
        EreActivityInfo target = mEAManager.resolveActivity(intent, 0);
        realStartActivity(intent, target);
    }
}
