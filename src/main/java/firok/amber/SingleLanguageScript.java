package firok.amber;

import firok.topaz.annotation.Unstable;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * 单一语言的脚本上下文映射
 * */
@Unstable
public interface SingleLanguageScript
{
    /**
     * 获取脚本引擎上下文的值
     * @since 3.0.0
     * */
    Value get(String key);

    /**
     * 获取脚本引擎上下文的值
     * */
    <TypeAny> TypeAny get(String key, Class<TypeAny> classType);

    /**
     * 设置脚本引擎上下文的值
     * */
    void set(String key, Object value);

    /**
     * 判断脚本引擎上下文是否存在某个值
     * @since 3.0.0
     * */
    boolean has(String key);

    /**
     * 移除脚本引擎上下文的值
     * @since 3.0.0
     * */
    void remove(String key);

    /**
     * 执行更多脚本内容
     * */
    Value eval(String script);

    /**
     * 执行更多脚本内容
     * */
    Value eval(Source source);

    /**
     * 你知道自己在做什么
     * */
    @Deprecated
    Context context();

    /**
     * 获取支持的脚本语言
     * */
    String languages();
}
