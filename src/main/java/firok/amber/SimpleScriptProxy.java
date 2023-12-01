package firok.amber;

import firok.topaz.thread.LockProxy;
import firok.topaz.thread.ReentrantLockProxy;
import org.graalvm.polyglot.Context;
import org.intellij.lang.annotations.Language;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Consumer;

/**
 * 基于 GraalJS, 提供单文件 JavaScript 脚本的动态代理封装.
 *
 * @since 1.0.0
 * @author Firok
 * @see Field
 * @see Method
 * */
public class SimpleScriptProxy
{
    public static void withAllAccess(Context.Builder builder)
    {
        builder.allowAllAccess(true);
    }

    /**
     * 创建一个脚本代理.
     * @apiNote 此方法构造的脚本解释器拥有对 JVM 的完整访问权限, 且所有方法的执行都是线程独立的
     * @param script 脚本内容
     * @param classScriptInterface 脚本接口. 其中所有方法都会被映射为一个脚本方法
     * */
    public static <TypeInterface extends ScriptInterface> TypeInterface connect(
            @Language("JS") String script,
            Class<TypeInterface> classScriptInterface
    )
    {
        return connect(
                script,
                classScriptInterface,
                SimpleScriptProxy::withAllAccess,
                new ReentrantLockProxy()
        );
    }

    /**
     * @see #connect(String, Class)
     * */
    public static <TypeInterface extends ScriptInterface> TypeInterface connect(
            List<String> scripts,
            Class<TypeInterface> classScriptInterface
    )
    {
        return connect(
                scripts,
                classScriptInterface,
                SimpleScriptProxy::withAllAccess,
                new ReentrantLockProxy()
        );
    }

    /**
     * @param buildProxy 脚本权限控制
     * @param lockProxy 调用任何方法执行的线程锁代理
     * @see #connect(String, Class)
     * */
    @SuppressWarnings("unchecked")
    public static <TypeInterface extends ScriptInterface> TypeInterface connect(
            @Language("JS") String script,
            Class<TypeInterface> classScriptInterface,
            Consumer<Context.Builder> buildProxy,
            LockProxy lockProxy
    )
    {
        return (TypeInterface) Proxy.newProxyInstance(
                classScriptInterface.getClassLoader(),
                new Class[] { classScriptInterface },
                new SimpleScriptProxyImpl<>("js", script, classScriptInterface, buildProxy, lockProxy)
        );
    }

    /**
     * @see #connect(String, Class, Consumer, LockProxy)
     * */
    @SuppressWarnings("unchecked")
    public static <TypeInterface extends ScriptInterface> TypeInterface connect(
            List<String> scripts,
            Class<TypeInterface> classScriptInterface,
            Consumer<Context.Builder> buildProxy,
            LockProxy lockProxy
    )
    {
        return (TypeInterface) Proxy.newProxyInstance(
                classScriptInterface.getClassLoader(),
                new Class[] { classScriptInterface },
                new SimpleScriptProxyImpl<>("js", scripts, classScriptInterface, buildProxy, lockProxy)
        );
    }

}
