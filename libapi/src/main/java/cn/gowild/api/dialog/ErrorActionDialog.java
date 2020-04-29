package cn.gowild.api.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.WindowManager;

/**
 * Created by SimonSun on 2019/5/23.
 */

public class ErrorActionDialog {

    private Context mContext;

    public ErrorActionDialog(Context context){
        mContext = context;
    }

    public void showErrorDialog(String className , String action){

        StringBuffer message = new StringBuffer();
        message.append("<");
        message.append(action);
        message.append(">");
        message.append("未在命名中心找到相关action，请以命名中心为准!");
        final AlertDialog.Builder dialogBuilder =
                new AlertDialog.Builder(mContext);
        dialogBuilder.setTitle(className+"异常!");
        dialogBuilder.setMessage(message);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

}
