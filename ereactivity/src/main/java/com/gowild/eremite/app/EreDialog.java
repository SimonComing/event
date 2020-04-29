package com.gowild.eremite.app;

import android.content.Context;

import com.gowild.eremite.am.EreActivityManager;
import com.gowild.eremite.event.Event;

/**
 * TODO<Simon说说这个类是干什么的呗>
 *
 * @author Simon
 * @data: 2017/6/21 19:01
 * @version: V1.0
 */


public class EreDialog implements EreDialogInterface , Event.Callback {

    private EreActivity mOwnerEreActivity;

    private boolean isShowed;

    private Context mContext;

    @Override
    public void cancel() {
        dismiss();
    }

    @Override
    public void dismiss() {
        isShowed = false;
    }

    public void show(){
        isShowed = true;
    }

    public boolean isShowed(){
        return isShowed;
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }

    public final EreActivity getOwnerActivity() {
        return mOwnerEreActivity;
    }

    public final void setOwnerEreActivity(EreActivity ereActivity){
        mOwnerEreActivity = ereActivity;
    }

    public EreDialog createDialog(Context context){
        mContext = context;
        EreActivityManager.getEAM().getTopEreActivity();
        return this;
    }
}
