package firok.amber;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 初始化上下文的时候就传入一些 Java 类型
 * @since 3.1.0
 * @author Firok
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Type
{
    String value() default "";
}
