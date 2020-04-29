package com.gowild.eremite.event;

import android.os.Bundle;

/**
 * TODO<Simon说说这个类是干什么的呗>
 *
 * @author Simon
 * @data: 2017/6/19 16:05
 * @version: V1.0
 */


public class Event {

    /**
     * 唤醒事件
     */
    public static final int EVENTCODE_WAKEUP = 1;
    /**
     * 红外事件
     */
    public static final int EVENTCODE_INFRARED = 2;
    /**
     * 检测到人脸
     */
    public static final int EVENTCODE_FIND_FACE = 3;
    /**
     * 录音失败
     */
    public static final int EVENTCODE_RECOGNIZING_ERROR = 4;
    /**
     * 静音闭麦
     */
    public static final int EVENTCODE_MUTE = 5;

    public Bundle bundle;
    public int eventCode;

    public interface Callback{
        boolean onEvent(Event event);
    }

    //无参的构造不允许被调用
    private Event(){
    }

    public Event(int eventCode,Bundle bundle){
        this.eventCode = eventCode;
        this.bundle = bundle;
    }
}
