package firok.amber;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @since 4.0.0
 * */
@Retention(RetentionPolicy.RUNTIME)
public @interface Script
{
    String language();

    String source();

    String name();

    String mimeType();

    boolean interactive() default false;

    boolean internal() default false;
}
