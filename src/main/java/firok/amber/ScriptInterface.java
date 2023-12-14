package firok.amber;

import firok.topaz.annotation.Unstable;
import firok.topaz.function.MustCloseable;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * 所有脚本代理的基类
 * @implNote 子类不需要实现 {@link #close()} 方法, 这个方法会由代理类转发到脚本引擎的关闭接口
 * @apiNote 子类销毁时需要调用 {@link #close()} 方法
 * @implSpec 因为在这个类里增删改任何内容都是 breaking change, 所以需要加个新功能可能就得让主版本号加一
 * @since 2.0.0
 * @version 4.0.0
 * @author Firok
 * */
@Unstable
@SuppressWarnings("unused")
public interface ScriptInterface extends MustCloseable
{
    /**
     * 获取脚本引擎上下文的值
     * @since 3.0.0
     * */
    Value get(String key);

    /**
     * 获取指定语言上下文的值
     * @since 4.0.0
     * */
    Value get(String language, String key);

    /**
     * 获取脚本引擎上下文的值
     * @since 3.0.0
     * */
    <TypeAny> TypeAny get(String key, Class<TypeAny> classType);

    /**
     * 获取指定语言上下文的值
     * @since 4.0.0
     * */
    <TypeAny> TypeAny get(String language, String key, Class<TypeAny> classType);

    /**
     * 设置脚本引擎上下文的值
     * @since 3.0.0
     * */
    void set(String key, Object value);

    /**
     * 设置指定语言上下文的值
     * @since 4.0.0
     * */
    void set(String language, String key, Object value);

    /**
     * 判断脚本引擎上下文是否存在某个值
     * @since 3.0.0
     * */
    boolean has(String key);

    /**
     * 判断指定语言上下文是否存在某个值
     * @since 4.0.0
     * */
    boolean has(String language, String key);

    /**
     * 移除脚本引擎上下文的值
     * @since 3.0.0
     * */
    void remove(String key);

    /**
     * 移除指定语言上下文的值
     * @since 4.0.0
     * */
    void remove(String language, String key);

    /**
     * 执行更多脚本内容
     * */
    Value eval(String script);

    /**
     * 在指定语言上下文执行更多脚本内容
     * @since 3.0.0
     * */
    Value eval(String language, String script);

    /**
     * 执行更多脚本内容
     * @since 4.0.0
     * */
    Value eval(Source source);

    /**
     * 你知道自己在做什么
     * @since 3.0.0
     * */
    @Deprecated
    Context context();

    /**
     * 获取支持的脚本语言
     * @since 3.0.0
     * */
    String[] languages();
}
