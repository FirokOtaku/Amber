package firok.amber;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 从模块里取字段. 目前只支持 JS
 * @since 5.0.0
 * @author Firok
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface ModuleField
{
    String context() default "";
    String module();
    String name() default "";
}
