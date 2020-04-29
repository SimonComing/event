package com.gowild.eremite.event;

/**
 * 统一调度管理系统重要事件，如：语音、按键、红外等事件
 * @author Simon
 * @data: 2017/6/19 16:46
 * @version: V1.0
 */


public class EventManager {

    private static EventManager eventManager = new EventManager();

    public Event.Callback callback = null;

    private EventManager(){}

    public static EventManager getEventManager(){
        return eventManager;
    }

    /**
     * 暂时中断事件调用，因为当前EreActivity状态不适合处理任何事件
     */
    public void interruptEventDispatch(){
        callback = null;
    }

    public void setCallback(Event.Callback callback){
        this.callback = callback;
    }

}
