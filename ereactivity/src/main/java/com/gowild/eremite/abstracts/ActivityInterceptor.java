package com.gowild.eremite.abstracts;

import com.gowild.eremite.am.EreActivityInfo;

/**
 * Created by SimonSun on 2019/1/14.
 */

public interface ActivityInterceptor {

    /**
     *
     */
    void process(EreActivityInfo targetEreActivityInfo, InterceptorCallback callback);

}
