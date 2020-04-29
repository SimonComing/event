package com.gowild.eremite.am;

import com.gowild.eremite.app.EreActivityThread;

/**
 * TODO<Simon说说这个类是干什么的呗>
 *
 * @author Simon
 * @data: 2017/3/7 19:00
 * @version: V1.0
 */


public class EreActivityRecord {

    TaskRecord stack ;

    EreActivityThread thread ;

    boolean finishing;

    static EreActivityRecord forToken(String token) {
        try {
            return token != null ? get(token): null;
        } catch (ClassCastException e) {
            return null;
        }
    }

    private static EreActivityRecord get(String token){
        return null ;
    }
}
