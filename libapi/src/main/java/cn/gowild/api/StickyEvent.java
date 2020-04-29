package cn.gowild.api;

/**
 * Created by SimonSun on 2019/4/12.
 */

class StickyEvent {

    MethodInfo methodInfo;

    Object[] params;

    StickyEvent(MethodInfo methodInfo , Object[] params){
        this.methodInfo = methodInfo;
        this.params = params;
    }

}
