package cn.gowild.api;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by SimonSun on 2018/7/25.
 */

public enum GowildEvent {

    INSTANCE;

    private HashMap<String, ArrayList<MethodInfo>> mPathMap = new HashMap<>();
    private HashMap<Class, ArrayList<MethodInfo>> mClassCache = new HashMap<>();
    private ArrayList<StickyEvent> mStickyEvents = new ArrayList<>();

    public static GowildEvent getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化时间分发器，包括：<br/>
     * <li>加载所有订阅事件对象映射关系</li>
     * <li>缓存订阅对象的Class信息</li>
     * <li>缓存订阅对象的对象信息</li>
     */
    public void init(String className) {

        synchronized (INSTANCE) {
            HashMap<String, ArrayList<MethodInfo>> tmpCache = null;
            try {
                Class<?> clazz = Class.forName(className);
                Object instance = clazz.newInstance();
                Method loadPathMethod = clazz.getMethod("loadEventPath");
                tmpCache = (HashMap<String, ArrayList<MethodInfo>>) loadPathMethod.invoke(instance);
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

            if (tmpCache != null) {

                Iterator<String> iterator = tmpCache.keySet().iterator();
                while (iterator.hasNext()){

                    String key = iterator.next();
                    ArrayList<MethodInfo> methodInfosOut = tmpCache.get(key);
                    ArrayList<MethodInfo> methodInfosIn  = mPathMap.get(key);
                    if (methodInfosIn != null){
                        methodInfosIn.addAll(methodInfosOut);
                    }else {
                        mPathMap.put(key,methodInfosOut);
                    }

                    for (MethodInfo methodInfo : methodInfosOut) {
                        ArrayList<MethodInfo> methodInfosInner = mClassCache.get(methodInfo.getClazzType());
                        if (methodInfosInner == null) {
                            methodInfosInner = new ArrayList<>();
                            mClassCache.put(methodInfo.getClazzType(), methodInfosInner);
                        }
                        methodInfosInner.add(methodInfo);
                    }
                }

            }
        }
    }

    public void register(Object subscriber) {
        Class clazz = subscriber.getClass();
        synchronized (INSTANCE) {
            ArrayList<MethodInfo> methodInfos = mClassCache.get(clazz);
            if (methodInfos == null) {
                Log.e("GowildEvent", subscriber.getClass().getCanonicalName() + " not need register.");
                return;
            }
            for (MethodInfo methodInfo : methodInfos) {
                methodInfo.setInstance(subscriber);

                // 检测是否有粘性事件依赖类被注册进来
                synchronized (mStickyEvents) {
                    for (int index = 0; index < mStickyEvents.size(); index++) {
                        StickyEvent stickyEvent = mStickyEvents.get(index);
                        if (stickyEvent.methodInfo == methodInfo) {
                            methodInfo.invoke(stickyEvent.params);
                            mStickyEvents.remove(index);
                            index--;
                        }
                    }
                }
            }

        }

    }

    public void unregister(Object subscriber) {
        Class clazz = subscriber.getClass();
        synchronized (INSTANCE) {
            ArrayList<MethodInfo> methodInfos = mClassCache.get(clazz);
            if (methodInfos == null) {
                Log.e("GowildEvent", subscriber.getClass().getCanonicalName() + " not need unregister.");
                return;
            }
            for (MethodInfo methodInfo : methodInfos) {
                methodInfo.setInstance(null);
            }
        }
    }

    /**
     * 通过路径，获取将接收函数的参数类型
     *
     * @param path
     * @return
     */
    public Class obtaionUniqueParamsTypeByPath(String path) {

        if (TextUtils.isEmpty(path)) {
            return null;
        }

        ArrayList<MethodInfo> methodInfos = mPathMap.get(toAscii(path));
        if (methodInfos == null || methodInfos.size() < 1) {
            return null;
        }
        MethodInfo methodInfo = methodInfos.get(0);
        Class[] clazzs = methodInfo.getParamsClass();
        return clazzs.length == 0 ? null : clazzs[0];
    }

    public boolean post(String path, Object... objects) {
        ArrayList<MethodInfo> methodInfos = mPathMap.get(toAscii(path));
        if (methodInfos == null) {
            return false;
        }
        boolean result = true;
        for (MethodInfo methodInfo : methodInfos) {
            result = methodInfo.invoke(objects) ? true : false;
        }
        return result;
    }

    public void postSticky(String path, Object... objects) {
        ArrayList<MethodInfo> methodInfos = mPathMap.get(toAscii(path));
        if (methodInfos == null) {
            return;
        }
        for (MethodInfo methodInfo : methodInfos) {
            if (methodInfo.getInstance() == null) {
                // 添加到粘性事件列表
                synchronized (mStickyEvents) {
                    mStickyEvents.add(new StickyEvent(methodInfo, objects));
                }
            } else {
                methodInfo.invoke(objects);
            }
        }
    }

    private String toAscii(String raw) {
        char[] chars = raw.toCharArray();
        StringBuffer buffer = new StringBuffer("g");
        for (char c : chars) {
            buffer.append((int) c + "_");
        }
        return buffer.toString();
    }
}
