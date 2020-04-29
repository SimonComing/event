package cn.gowild.api;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.gowild.annotation.GowildSubscribe;

/**
 * Created by SimonSun on 2018/7/25.
 */

public class MethodInfo {

    private Method method;

    private Class clazzType;

    private Class[] paramsClass;

    private Object instance;

    private GowildSubscribe.FunctionType functionType;

    private MethodInfo() {
    }

    public MethodInfo(Method method, Class clazzType) {
        this.method = method;
        this.clazzType = clazzType;
    }

    public MethodInfo(Method method, Class clazzType, Class[] paramsClass) {
        this.method = method;
        this.clazzType = clazzType;
        this.paramsClass = paramsClass;
    }

    public MethodInfo(Method method, Class clazzType, Class[] paramsClass, GowildSubscribe.FunctionType functionType){
        this.method = method;
        this.clazzType = clazzType;
        this.paramsClass = paramsClass;
        this.functionType = functionType;
    }

    public MethodInfo(Method method, Class clazzType, Class[] paramsClass, Object instance) {
        this.method = method;
        this.clazzType = clazzType;
        this.paramsClass = paramsClass;
        this.instance = instance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class getClazzType() {
        return clazzType;
    }

    public void setClazzType(Class clazzType) {
        this.clazzType = clazzType;
    }

    public Class[] getParamsClass() {
        return paramsClass;
    }

    public void setParamsClass(Class[] paramsClass) {
        this.paramsClass = paramsClass;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public GowildSubscribe.FunctionType getFunctionType() {
        return functionType;
    }

    public void setFunctionType(GowildSubscribe.FunctionType functionType) {
        this.functionType = functionType;
    }

    public boolean invoke(Object... objects) {
        boolean result = false;

        // 未注册实例，返回执行失败
        if(instance == null){
            return false;
        }

        // 确认参数长度
        // 如果指定函数不包含参数则忽略objects值
        int classesLen = paramsClass == null ? 0 : paramsClass.length;
        int objectsLen = objects.length;
        try {
            if (classesLen == 0) {
                method.invoke(instance);
                result = true;
            } else if (classesLen == 1 && paramsClass[0].isArray()) {
                method.invoke(instance, (Object) objects);
                result = true;
            } else if(classesLen == 1 ){
                method.invoke(instance, objects);
                result = true;
            }else if (classesLen == objectsLen) {
                Object[] params = new Object[classesLen];
                for (int index = 0; index < classesLen; index++) {
                    params[index] = objects[index];
                }
                method.invoke(instance, params);
                result = true;
            } else {
                Log.e("MethodInfo", "can not invoke method " + method.getName());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return result;
    }
}
