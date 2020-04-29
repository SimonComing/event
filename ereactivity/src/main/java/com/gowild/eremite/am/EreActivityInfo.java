package com.gowild.eremite.am;


import android.content.pm.ComponentInfo;

import com.gowild.eremite.app.EreActivity;

import java.util.ArrayList;

/**
 * EreActivity的信息，这里的信息来自解析EreAndroidManifest.xml
 *
 * @author Simon
 * @data: 2017/3/7 11:21
 * @version: V1.0
 */


public class EreActivityInfo extends ComponentInfo {

    /**
     * 象征着这个组件是否被实例化
     */
    public boolean enabled = true;

    /**
     * 相当于 <code>standard</code>
     */
    public static final int LAUNCH_MULTIPLE = 0;
    /**
     * 相当于 <code>singleTop</code>
     */
    public static final int LAUNCH_SINGLE_TOP = 1;
    /**
     * 相当于 <code>singleTask</code>
     */
    public static final int LAUNCH_SINGLE_TASK = 2;
    /**
     * 相当于 <code>singleInstance</code>
     */
    public static final int LAUNCH_SINGLE_INSTANCE = 3;
    /**
     * ereactivity 的启动模式，取之于
     * {@link #LAUNCH_MULTIPLE},
     * {@link #LAUNCH_SINGLE_TOP}, {@link #LAUNCH_SINGLE_TASK}, or
     * {@link #LAUNCH_SINGLE_INSTANCE}.
     */
    public int launchMode;

    public EreActivity targetActivity;

    public String packageName;

    /**
     * 启动等级是一个关键的概念，一定要有充分理解再去修改代码。
     * 一个应用内会有很多个EreActvity，每个EreActvity都有它包含的信息，
     * launchLevel代表了这个EreActvity是否能在适当的时候被启动。
     * launchLevel的值从小到大表示着这个EreActvity被启动的优先级，
     * 低优先级的EreActivity不能打断高优先级的EreActivity，只能打断
     * 低于等于自身优先级的EreActvity
     */
    public int launchLevel = 0 ;

    public ArrayList<String> actions = new ArrayList<>();

    public EreActivityInfo(){

    }

    public EreActivityInfo(String name , String packageName , int launchLevel , String action ){
        this.name = name;
        this.packageName = packageName;
        this.launchLevel = launchLevel;
        this.actions.add(action);
        this.enabled = true;
    }

    public EreActivityInfo(EreActivityInfo ereActivityInfo){
        name = ereActivityInfo.name;
        packageName = ereActivityInfo.packageName;
        launchMode = ereActivityInfo.launchMode ;
        launchLevel = ereActivityInfo.launchLevel;
        enabled = ereActivityInfo.enabled ;
        actions = ereActivityInfo.actions;
    }

    public void addAction(String action){
        actions.add(action);
    }

    public int getLaunchLevel(){
        return launchLevel;
    }

    public int getLaunchMode(){
        return launchMode;
    }

    /**
     * 返回这个组件是否有效
     */
    public boolean isEnabled(){
        return enabled ;
    }

}
