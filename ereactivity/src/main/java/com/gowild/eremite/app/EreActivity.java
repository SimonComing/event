package com.gowild.eremite.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.gowild.eremite.abstracts.ActivityInterceptor;
import com.gowild.eremite.abstracts.InterceptorCallback;
import com.gowild.eremite.am.EreActivityInfo;
import com.gowild.eremite.am.EreActivityManager;
import com.gowild.eremite.event.Event;
import com.gowild.eremite.event.EventManager;
import com.gowild.eremite.util.SuperNotCalledException;

/**
 * EreActivity全称为Eremite Activity，它的作用相当于Activity，但是EreActivity
 * 并不负责图像界面的显示，而是后台工作的，它有类似与Activity的生命周期
 *
 * @author Simon
 * @data: 2017/3/7 15:06
 * @version: V1.0
 */


public class EreActivity implements Event.Callback {

    protected Context context;
    private Instrumentation mInstrumentation;
    private EreActivityInfo mEreActivityInfo;
    private EreActivityThread mMainThread;
    private EventManager mEventManager;
    private ActivityInterceptor mInterceptor;
    Intent mIntent;

    private EreDialog mDialog;

    boolean mCalled;
    boolean mFinished;
    private boolean mStopped;
    boolean mResumed;
    private boolean mDestroyed;

    /**
     * 此EreActivity的唯一名字
     */
    String who;


    final void attach(Instrumentation instrumentation, EreActivityThread eThread, Context context, EreActivityInfo eInfo,
                      Intent intent) {
        mInstrumentation = instrumentation;
        this.context = context;
        mEreActivityInfo = eInfo;
        mEreActivityInfo.targetActivity = this;
        mMainThread = eThread;
        mIntent = intent;
        attachEvent();
    }

    final void attachEvent() {
        mEventManager = EventManager.getEventManager();
        mEventManager.setCallback(this);
    }

    protected void setInterceptor(ActivityInterceptor interceptor) {
        mInterceptor = interceptor;
        Log.d("EreActivityStack", "set interceptor "+interceptor);
    }

    public ActivityInterceptor getInterceptor(){
        return mInterceptor;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public EreActivityInfo getEreActivityInfo(){
        return mEreActivityInfo;
    }

    /**
     * 等同没有指定选择的{@link #startActivity(Intent, Bundle)}
     *
     * @see {@link #startActivity(Intent, Bundle)}
     * @see #startActivityForResult
     */
    public void startActivity(Intent intent) {
        startActivity(intent, null);
    }

    public void setIntent(Intent newIntent) {
        mIntent = newIntent;
    }

    public void startActivity(Intent intent, Bundle options) {
        if (options != null) {
            startActivityForResult(intent, -1, options);
        } else {
            startActivityForResult(intent, -1);
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, null);
    }

    public void startActivityForResult(Intent intent,int requestCode,Bundle options) {

        Instrumentation.EreActivityResult ear = mInstrumentation.exeStartEreActivity(context, this.mEreActivityInfo, intent,
                requestCode, options);

    }

    final void performCreate(Bundle bundle) {
        onCreate(bundle);
    }

    final void performRestart() {
        mCalled = false;
        onRestart();
        if (!mCalled) {
            throw new SuperNotCalledException(
                    "EreActivity " + who +
                            " did not call through to super.onRestart()");
        }
    }

    final void performNewIntent(Bundle bundle) {
        mCalled = false;
        onNewIntent(bundle);
        if (!mCalled) {
            throw new SuperNotCalledException(
                    "EreActivity " + who +
                            " did not call through to super.onNewIntent()");
        }
    }

    final void performResume() {
        mCalled = false;
        onResume();
        if (!mCalled) {
            throw new SuperNotCalledException(
                    "EreActivity " + who +
                            " did not call through to super.onResume()");
        }
    }

    final void performPause() {
        mCalled = false;
        onPause();
        if (!mCalled) {
            throw new SuperNotCalledException(
                    "EreActivity " + who +
                            " did not call through to super.onPause()");
        }
    }

    final void performStop() {
        mCalled = false;
        onStop();
        if (!mCalled) {
            throw new SuperNotCalledException(
                    "EreActivity " + who +
                            " did not call through to super.onStop()");
        }
    }

    final void performDestory() {
        mEreActivityInfo.targetActivity = null;
        mCalled = false;
        onDestory();
        if (!mCalled) {
            throw new SuperNotCalledException(
                    "EreActivity " + who +
                            " did not call through to super.onDestory()");
        }
    }

    protected void onCreate(Bundle bundle) {
        mCalled = true;
        saveInstanceState(bundle);
    }

    protected void onRestart() {
        mCalled = true;
    }

    protected void onNewIntent(Bundle bundle) {
        mCalled = true;
        saveInstanceState(bundle);
    }

    protected void onResume() {
        mCalled = true;
        saveInstanceState(getIntent().getExtras());
    }

    protected void onPause() {
        mCalled = true;
    }

    protected void onStop() {
        mCalled = true;
    }

    protected void onDestory() {
        mCalled = true;
    }

    public void finish() {
        if (EreActivityManager.getEAM().finishActivity(mEreActivityInfo)) {
            mFinished = true;
        }
    }

    @Override
    public boolean onEvent(Event event) {
        if (mDialog != null && mDialog.isShowed()) {
            return mDialog.onEvent(event);
        }
        return false;
    }

    private void saveInstanceState(Bundle bundle){
        if (bundle == null){
            return;
        }

        String mode = bundle.getString("persistableMode","never");
        if ("never".equals(mode) || "".equals(mode)){
            return;
        }

        if ("always".equals(mode)){
            EreActivityManager.getEAM().getEreActivityPersistence().saveState(this,bundle);
        }

        else if ("clear".equals(mode)){
            EreActivityManager.getEAM().getEreActivityPersistence().clearState();
        }

    }

}
