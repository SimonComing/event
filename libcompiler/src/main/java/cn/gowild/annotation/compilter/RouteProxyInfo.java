package cn.gowild.annotation.compilter;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.Locale;

import javax.lang.model.element.Modifier;

/**
 * Created by SimonSun on 2019/3/4.
 */

public class RouteProxyInfo {

    private String mClassName;
    private String mSimpleName;
    private String mAction;
    private String mAlias;
    private int    mLanchLevel;

    private final static String SERVICE_CONSTANT_NAME = "SERVICE_ACTION_%s";
    private final static String EREACTIVITY_CONSTANT_NAME = "EREACTIVITY_ACTION_%s";

    public RouteProxyInfo(String className,String simpleName,String action,String alias , int lanchLevel){
        mClassName = className;
        mSimpleName = simpleName;
        mAction = action;
        mAlias = alias;
        mLanchLevel = lanchLevel;
    }


    public void generateIServiceJavaCode(ArrayList<FieldSpec> constantBuilders , MethodSpec.Builder builder){

        //String value = "\"" + mAction + "\"";
        //FieldSpec fieldSpec = FieldSpec.builder(String.class,String.format(Locale.CHINESE,SERVICE_CONSTANT_NAME,mAlias.toUpperCase()), Modifier.PUBLIC,Modifier.FINAL,Modifier.STATIC).initializer(value).build();
        //constantBuilders.add(fieldSpec);

        // 添加行为名注释
        builder.addStatement("// action : "+ mAction);

        String classForName = "Class<?> %sClazz = Class.forName(\"%s\")";
        classForName = String.format(Locale.CHINESE,classForName,mSimpleName,mClassName);
        builder.addStatement(classForName);

        String getConstructor = "java.lang.reflect.Constructor<?> %sConstructor = %sClazz.getConstructor(Context.class,String.class)";
        getConstructor = String.format(Locale.CHINESE,getConstructor,mSimpleName,mSimpleName);
        builder.addStatement(getConstructor);

        String newInstance = "IService %sService = (IService) %sConstructor.newInstance(context,\"%s\")";
        newInstance = String.format(Locale.CHINESE,newInstance,mSimpleName,mSimpleName,mAlias);
        builder.addStatement(newInstance);

        String put = "pathMap.put(\"%s\",%sService)";
        put = String.format(Locale.CHINESE,put, mAction,mSimpleName);
        builder.addStatement(put);
    }

    public void generateEreActivityJavaCode(ArrayList<FieldSpec> constantBuilders , MethodSpec.Builder builder){

        //String value = "\"" + mAction + "\"";
        //FieldSpec fieldSpec = FieldSpec.builder(String.class,String.format(Locale.CHINESE,EREACTIVITY_CONSTANT_NAME,mSimpleName.toUpperCase()), Modifier.PUBLIC,Modifier.FINAL,Modifier.STATIC).initializer(value).build();
        //constantBuilders.add(fieldSpec);

        // 添加行为名注释
        builder.addStatement("// action : "+ mAction);

        String classForName = "Class<?> %sClazz = Class.forName(\"com.gowild.eremite.am.EreActivityInfo\")";
        classForName = String.format(Locale.CHINESE,classForName,mSimpleName);
        builder.addStatement(classForName);

        String getConstructor = "java.lang.reflect.Constructor<?> %sConstructor = %sClazz.getConstructor(String.class,String.class,int.class,String.class)";
        getConstructor = String.format(Locale.CHINESE,getConstructor,mSimpleName,mSimpleName);
        builder.addStatement(getConstructor);

        // String name , String packageName , int launchLevel , String action
        String newInstance = "EreActivityInfo %sActivityInfo = (EreActivityInfo) %sConstructor.newInstance(\"%s\", \"%s\" , %d ,\"%s\")";
        newInstance = String.format(Locale.CHINESE,newInstance,mSimpleName,mSimpleName,mClassName,"cn.gowild.voiceserver.xera",mLanchLevel,mAction);
        builder.addStatement(newInstance);

        String put = "pathMap.put(\"%s\",%sActivityInfo)";
        put = String.format(Locale.CHINESE,put, mAction,mSimpleName);
        builder.addStatement(put);
    }

}
