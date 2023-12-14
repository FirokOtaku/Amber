package firok.amber;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 明确指定获取某个上下文方法
 * @since 1.0.0
 * @version 4.0.0
 * @author Firok
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Method
{
    /**
     * 如果留空, 则默认使用方法名
     * */
    String value() default "";

    /**
     * 哪个上下文里的成员
     * @since 4.0.0
     * */
    String lang() default "";

    /**
     * 指定从哪个 ES 模块寻找方法. 仅可用于 JS 语言上下文
     * @since 5.0.0
     * */
    String module() default "";
}
