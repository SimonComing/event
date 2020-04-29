package cn.gowild.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by SimonSun on 2019/3/4.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface GowildRoute {

    /**
     * 行为名，服务或者活动以此对外指向
     *
     */
    String action() ;

    /**
     * 服务或者活动等的别名,服务必须有别名，否则将编译不通过
     * @return
     */
    String alias() default "";

    int launchLevel() default 0;

}
