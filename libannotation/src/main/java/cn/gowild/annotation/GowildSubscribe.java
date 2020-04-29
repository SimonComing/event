package cn.gowild.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by SimonSun on 2018/7/25.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface GowildSubscribe {

    enum FunctionType{
        /**
         * 跨模块消息接收函数
         */
        IMC,
        /**
         * 远程通信消息接收函数
         */
        REMOTE}

    /**
     * 路由路径，将根据此路径通知订阅者
     * @return
     */
    String[] eventPath();

    FunctionType functionType() default FunctionType.IMC;

}
