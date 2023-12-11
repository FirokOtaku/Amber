package firok.amber;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在绑定内容之前就初始化一些内容
 * @since 4.0.0
 * @author Firok
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Init
{
    /**
     * 要绑定的 Java 类型. 这些会为所有语言上下文传入.
     * */
    Type[] types() default {};

    /**
     * 要执行的脚本. 这会在对应语言的上下文执行.
     * */
    Script[] scripts() default {};
}
