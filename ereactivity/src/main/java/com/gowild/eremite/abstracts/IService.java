package com.gowild.eremite.abstracts;

/**
 * Created by SimonSun on 2018/3/7.
 */

public interface IService {

    String getAlias();

    /**
     * 所有服务初始化完成回调
     */
    default void initialized(){}

    /**
     * 本服务所需资源是否已经加载完成
     * @return
     */
    default boolean isResourcesSupport(){return true;}

    /**
     * 本服务所需依赖其他服务的资源
     * @return
     */
    default String[] resourcesDepends(){return null;}

    /**
     * 本服务资源加载的优先级，各个服务之间可能存在资源依赖的关系，所以更基础的服务需要优先加载<br>
     * 默认值1000
     * @return
     */
    default int resourcesPriority() {return 1000;}

}
