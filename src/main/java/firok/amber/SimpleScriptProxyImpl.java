package firok.amber;

import firok.topaz.thread.LockProxy;
import org.graalvm.polyglot.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static firok.topaz.general.Collections.isEmpty;

@SuppressWarnings("FieldCanBeLocal")
class SimpleScriptProxyImpl<TypeInterface> implements InvocationHandler
{
    private final LockProxy lockProxy;
    private final Context context;
    private final Map<Method, Function<Object[], ?>> mappedContext;

    SimpleScriptProxyImpl(
            List<String> scripts,
            Class<TypeInterface> classScriptInterface,
            Consumer<Context.Builder> buildProxy,
            LockProxy lockProxy
    )
    {
        if (buildProxy != null)
        {
            var builder = Context.newBuilder("js");
            buildProxy.accept(builder);
            this.context = builder.build();
        }
        else
        {
            this.context = Context.newBuilder("js").allowAllAccess(true).build();
        }

        // 执行初始化脚本
        for(var script : scripts)
        {
            context.eval("js", script);
        }

        this.lockProxy = lockProxy;
        this.mappedContext = new HashMap<>();
        var bindings = context.getBindings("js");
        for (var method : classScriptInterface.getMethods())
        {
            if (Modifier.isStatic(method.getModifiers())) continue; // 暂时不处理静态方法
            var annoField = method.getAnnotation(firok.amber.Field.class);
            var annoMethod = method.getAnnotation(firok.amber.Method.class);
            if(annoField != null && annoMethod != null) throw new IllegalArgumentException("不能为方法指定多个目标");
            var nameMethod = method.getName();

            boolean isField = annoField != null;
            String nameTarget;
            if(annoField != null) nameTarget = "".equals(annoField.value()) ? nameMethod : annoField.value();
            else if(annoMethod != null) nameTarget = "".equals(annoMethod.value()) ? nameMethod : annoMethod.value();
            else nameTarget = method.getName();

            var typeReturnValue = method.getReturnType();

            this.mappedContext.put(method, (args) -> {
                var target = bindings.getMember(nameTarget);

                if (isField)
                {
                    return target.as(typeReturnValue);
                }

                if (typeReturnValue == void.class || typeReturnValue == Void.class)
                {
                    if (isEmpty(args)) target.executeVoid();
                    else target.executeVoid(args);
                    return null;
                }
                else
                {
                    return isEmpty(args) ?
                            target.execute().as(typeReturnValue) :
                            target.execute(args).as(typeReturnValue);
                }
            });
        }
    }

    SimpleScriptProxyImpl(
            String script,
            Class<TypeInterface> classScriptInterface,
            Consumer<Context.Builder> buildProxy,
            LockProxy lockProxy
    )
    {
        this(List.of(script), classScriptInterface, buildProxy, lockProxy);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception
    {
        try
        {
            lockProxy.lock();
            var function = this.mappedContext.get(method);
            return function.apply(args);
        }
        catch (Exception any)
        {
            throw new Exception("script invoke failed", any);
        }
        finally
        {
            lockProxy.unlock();
        }
    }
}
