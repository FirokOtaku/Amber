package firok.amber;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * 多语言的脚本上下文映射
 * */
public interface MultiLanguageScript
{
    /**
     * 获取指定语言上下文的值
     * */
    Value get(String language, String key);

    /**
     * 获取指定语言上下文的值
     * */
    <TypeAny> TypeAny get(String language, String key, Class<TypeAny> classType);

    /**
     * 设置指定语言上下文的值
     * */
    void set(String language, String key, Object value);

    /**
     * 判断指定语言上下文是否存在某个值
     * */
    boolean has(String language, String key);

    /**
     * 移除指定语言上下文的值
     * */
    void remove(String language, String key);

    /**
     * 在指定语言上下文执行更多脚本内容
     * */
    Value eval(String language, String script);

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
    String[] languages();
}
