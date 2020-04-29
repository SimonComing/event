package com.gowild.eremite.app;

import android.content.DialogInterface;

/**
 * TODO<Simon说说这个类是干什么的呗>
 *
 * @author Simon
 * @data: 2017/6/21 18:48
 * @version: V1.0
 */


public interface EreDialogInterface {

    interface OnCancelListener {
        void onCancel(DialogInterface dialog);
    }

    interface OnDismissListener {
        void onDismiss(DialogInterface dialog);
    }

    void cancel();

    void dismiss();
}
