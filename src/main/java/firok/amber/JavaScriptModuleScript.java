package firok.amber;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * JavaScript ES 模块化脚本上下文映射
 * */
public interface JavaScriptModuleScript
{
    /**
     * 获取指定模块上下文的值
     * */
    Value get(String module, String key);

    /**
     * 获取指定模块上下文的值
     * */
    <TypeAny> TypeAny get(String module, String key, Class<TypeAny> classType);

    /**
     * 设置指定模块上下文的值
     * */
    void set(String module, String key, Object value);

    /**
     * 判断指定模块上下文是否存在某个值
     * */
    boolean has(String module, String key);

    /**
     * 移除指定模块上下文的值
     * */
    void remove(String module, String key);

    /**
     * 在指定模块上下文执行更多脚本内容
     * */
    Value eval(String module, String script);

    /**
     * 执行更多脚本内容
     * */
    Value eval(Source source);

    /**
     * 你知道自己在做什么
     * */
    @Deprecated
    Context context();
}
