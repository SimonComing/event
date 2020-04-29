package cn.gowild.api;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.gowild.eremite.abstracts.IService;
import com.gowild.eremite.am.EreActivityInfo;
import com.gowild.eremite.am.EreActivityManager;
import com.gowild.eremite.app.EreActivity;
import com.gowild.eremite.app.GowildIntent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import cn.gowild.annotation.GowildRoute;
import cn.gowild.api.dialog.ErrorActionDialog;

/**
 * Created by SimonSun on 2019/3/5.
 */

public enum GowildRoutePresenter {

    INSTANCE;

    public static GowildRoutePresenter getInstance() {
        return INSTANCE;
    }

    private HashMap<String, IService>        mIServicePathMap       = new HashMap<>();
    private HashMap<String, Class<IService>> mIServicePath2ClassMap = new HashMap<>();

    private HashMap<String, EreActivityInfo> mEreActivityPathMap    = new HashMap<>();

    private Context mContext;

    public void init(Context context,String launchAction) {

        mContext = context;

        try {
            loadClassFile(context);
        } catch (Exception e) {
            Log.e("RoutePresenter", "error " + e.toString());
        }

        // 启动启动页跳转
        EreActivityManager.getEAM().createEreActivityManager(context, mEreActivityPathMap, launchAction ,getSharedspace());

        // 通知各模块所有初始化已经完成
        Iterator<IService> services = mIServicePathMap.values().iterator();
        while (services.hasNext()) {
            services.next().initialized();
        }

    }

    private void loadClassFile(Context context) throws PackageManager.NameNotFoundException, IOException {
        Set<String> classSet = ClassUtils.getClassNameInPackage(context,
                BuildConfig.DEBUG ? "c" :
                        Constrants.API_PACKAGE_ROOT_PACKAGE_NAME);
        Set<String> routeActionClassSet = new HashSet<>();
        Set<Class<?>> annotaionClass = new HashSet<>();
        Field[] finalFields = BuildConfig.DEBUG ? RouteActionNameCenter.class.getFields() : null;
        for (String className : classSet) {
            if (className.startsWith(Constrants.API_PACKAGE_ROOT_PACKAGE_NAME + Constrants.DOT + Constrants.EVENT_PATH_CENTER)) {
                // 要先初始化GowildEvent
                GowildEvent.getInstance().init(className);
            } else if (className.startsWith(Constrants.API_PACKAGE_ROOT_PACKAGE_NAME + Constrants.DOT + Constrants
                    .ROUTE_ACTION_CENTER)) {
                routeActionClassSet.add(className);
            } else if (BuildConfig.DEBUG) {
                Class<?> clazz = checkAnnotaionClass(className,finalFields);
                if (clazz != null) {
                    annotaionClass.add(clazz);
                }
            }
        }
        for (String className : routeActionClassSet) {
            initRouteMap(context, className);
        }

    }

    private Class<?> checkAnnotaionClass(String className,Field[] finalFields) {
        Class<?> clazz = null;
        try {
            if (!className.contains("gowild") || className.contains("$")){
                return clazz;
            }
            clazz = Class.forName(className,false,this.getClass().getClassLoader());
            if (clazz.isAnnotationPresent(GowildRoute.class)){
                GowildRoute gowildRoute = clazz.getAnnotation(GowildRoute.class);
                if (gowildRoute == null){
                    return null;
                }
                String action = gowildRoute.action();
                boolean isHasAction = false;
                // 检测action是否在命名中心，不在则提示异常
                for (Field field : finalFields){
                    if (field.get(null).equals(action)){
                        isHasAction = true;
                        break;
                    }
                }
                if (!isHasAction){
                    // 弹框提示异常
                    buildErrorDialog(className,action);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        }
        return clazz;
    }

    private void buildErrorDialog(String className ,String action){
        new ErrorActionDialog(mContext).showErrorDialog(className,action);
    }

    private void initRouteMap(Context context, String className) {
        synchronized (INSTANCE) {
            try {
                Class<?> clazz = Class.forName(className);
                Object instance = clazz.newInstance();
                Method loadIServicePathMethod = clazz.getMethod("loadIServiceRoutePath", new Class[]{Context.class});
                Method loadEreActivityPathMethod = clazz.getMethod("loadEreActivityRoutePath", new Class[]{Context.class});
                mIServicePathMap.putAll((HashMap<String, IService>) loadIServicePathMethod.invoke(instance, new
                        Object[]{context}));
                mEreActivityPathMap.putAll((HashMap<String, EreActivityInfo>) loadEreActivityPathMethod.invoke(instance, new
                        Object[]{context}));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }

    public <T> T getService(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException(String.format(Locale.CHINESE, "path %s is empty !!!", path));
            //return null;
        }

        return (T) mIServicePathMap.get(path);
    }

    public Iterator<IService> getAllServices(){
        return mIServicePathMap.values().iterator();
    }

    public RouteBundle buildBundle(String action) {
        return new RouteBundle(action);
    }

    /**
     * 指定action的EreActivity是否当前在活动栈顶
     *
     * @param action
     * @return
     */
    public boolean isTopEreActivity(String action) {
        EreActivity topActivity = EreActivityManager.getEAM().getTopEreActivity();
        if (topActivity == null || TextUtils.isEmpty(action)) {
            return false;
        }

        ArrayList<String> actions = topActivity.getEreActivityInfo().actions;
        for (String item : actions) {
            if (action.equals(item)) {
                return true;
            }
        }

        return false;
    }

    public void post(String action) {
        post(action, null);
    }

    public void post(String action, Bundle bundle) {

        if ("gowild.ereactivity.action.welcome".equals(action)) {
            EreActivityManager.getEAM().restoreInstanceState();
            return;
        }

        EreActivity ereActivity = EreActivityManager.getEAM().getTopEreActivity();
        if (ereActivity != null) {
            GowildIntent intent = new GowildIntent();
            intent.setAction(action);
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            ereActivity.startActivity(intent);
        }
    }

    public void post(GowildIntent intent) {
        EreActivity ereActivity = EreActivityManager.getEAM().getTopEreActivity();
        if (ereActivity != null) {
            ereActivity.startActivity(intent);
        }
    }

    private String getSharedspace() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/auspace");
        return dir.exists() || dir.mkdir() ? dir.getAbsolutePath() : null;
    }
}
