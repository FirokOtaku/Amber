package firok.amber;

import firok.topaz.function.MustCloseable;

/**
 * 所有脚本代理的基类
 * @implNote 子类不需要实现 {@link #close()} 方法, 这个方法会由代理类转发到脚本引擎的关闭接口
 * @since 2.0.0
 * @author Firok
 * */
public interface ScriptInterface extends MustCloseable
{
}
