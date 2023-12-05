package firok.amber;

import firok.topaz.function.MustCloseable;
import firok.topaz.reflection.Reflections;
import firok.topaz.thread.LockProxy;
import org.graalvm.polyglot.Context;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static firok.topaz.general.Collections.isEmpty;

@SuppressWarnings({"FieldCanBeLocal", "DataFlowIssue"})
class AmberInstance<TypeInterface extends AutoCloseable> implements InvocationHandler
{
    private final LockProxy lockProxy;
    private final Context context;
    private final Map<Method, Function<Object[], ?>> mappedContext;
    @NotNull private final static Method MethodClose = Reflections.methodOf(MustCloseable.class, "close");
    @NotNull private final static Method MethodGet = Reflections.methodOf(ScriptInterface.class, "get", String.class);
    @NotNull private final static Method MethodGetType = Reflections.methodOf(ScriptInterface.class, "get", String.class, Class.class);
    @NotNull private final static Method MethodSet = Reflections.methodOf(ScriptInterface.class, "set", String.class, Object.class);
    @NotNull private final static Method MethodHas = Reflections.methodOf(ScriptInterface.class, "has", String.class);
    @NotNull private final static Method MethodRemove = Reflections.methodOf(ScriptInterface.class, "remove", String.class);
    @NotNull private final static Method MethodEval = Reflections.methodOf(ScriptInterface.class, "eval", String.class);
    @NotNull private final static Method MethodContext = Reflections.methodOf(ScriptInterface.class, "context");
    @NotNull private final static Method MethodLanguage = Reflections.methodOf(ScriptInterface.class, "language");

    private final List<Language> languages;
    AmberInstance(
            Iterable<Language> languages,
            Iterable<String> scripts,
            Class<TypeInterface> classScriptInterface,
            Consumer<Context.Builder> buildProxy,
            LockProxy lockProxy
    )
    {
        this.languages = languages;

        if (buildProxy != null)
        {
            var builder = Context.newBuilder(this.language);
            buildProxy.accept(builder);
            this.context = builder.build();
        }
        else
        {
            this.context = Context.newBuilder(this.language).allowAllAccess(true).build();
        }

        this.lockProxy = lockProxy;
        this.mappedContext = new HashMap<>();
        var bindings = context.getBindings(this.language);
        for (var method : classScriptInterface.getMethods())
        {
            if (Modifier.isStatic(method.getModifiers())) continue; // 暂时不处理静态方法

            if(MethodClose.equals(method))
            {
                this.mappedContext.put(method, args -> {
                    try { context.close(); }
                    catch (Exception ignored) { }
                    return null;
                });
            }
            else if(MethodGet.equals(method))
            {
                this.mappedContext.put(method, args -> {
                    try { return bindings.getMember((String) args[0]); }
                    catch (Exception any) { return null; }
                });
            }
            else if(MethodGetType.equals(method))
            {
                this.mappedContext.put(method, args -> {
                    try
                    {
                        var value = bindings.getMember((String) args[0]);
                        return value != null && !value.isNull() ? value.as((Class<?>) args[1]) : null;
                    }
                    catch (Exception any) { return null; }
                });
            }
            else if(MethodSet.equals(method))
            {
                this.mappedContext.put(method, args -> {
                    try { bindings.putMember((String) args[0], args[1]); }
                    catch (Exception ignored) { }
                    return null;
                });
            }
            else if(MethodHas.equals(method))
            {
                this.mappedContext.put(method, args -> {
                    try { return bindings.hasMember((String) args[0]); }
                    catch (Exception any) { return false; }
                });
            }
            else if(MethodRemove.equals(method))
            {
                this.mappedContext.put(method, args -> {
                    try { bindings.removeMember((String) args[0]); }
                    catch (Exception ignored) { }
                    return null;
                });
            }
            else if(MethodEval.equals(method))
            {
                this.mappedContext.put(method, args -> context.eval(this.language, (String) args[0]));
            }
            else if(MethodContext.equals(method))
            {
                this.mappedContext.put(method, args -> context);
            }
            else if(MethodLanguage.equals(method))
            {
                this.mappedContext.put(method, args -> this.language);
            }
            else // 转发到脚本引擎内部实现
            {
                var annoField = method.getAnnotation(firok.amber.Field.class);
                var annoMethod = method.getAnnotation(firok.amber.Method.class);
                var annoType = method.getAnnotation(firok.amber.Type.class);
                if(annoField != null && annoMethod != null) throw new IllegalArgumentException("不能为方法指定多个目标");
                var nameMethod = method.getName();
                var countParam = method.getParameterCount();
                var typeReturnValue = method.getReturnType();

                final boolean isField = annoField != null;
                final boolean isType = annoType != null;
                String nameTarget;
                if(isField)
                {
                    if(countParam > 1) throw new IllegalArgumentException("字段操作器只能有零或一个形参");

                    nameTarget = "".equals(annoField.value()) ? nameMethod : annoField.value();

                    this.mappedContext.put(method, (args) -> {
                        if (countParam == 0)
                        {
                            return bindings.hasMember(nameTarget) ? bindings.getMember(nameTarget).as(typeReturnValue) : null;
                        }
                        else
                        {
                            bindings.putMember(nameTarget, args[0]);
                            return null;
                        }
                    });
                }
                else if(isType)
                {
                    if(countParam > 0) throw new IllegalArgumentException("类型绑定器不能有形参");

                    nameTarget = "".equals(annoType.value()) ? nameMethod : annoType.value();

                    bindings.putMember(nameTarget, typeReturnValue);

                    this.mappedContext.put(method, this::notMethod);
                }
                else
                {
                    nameTarget = annoMethod != null && !"".equals(annoMethod.value()) ?
                            annoMethod.value() :
                            nameMethod;

                    this.mappedContext.put(method, (args) -> {
                        var target = bindings.getMember(nameTarget);

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
        }

        // 执行初始化脚本
        for(var script : scripts)
        {
            context.eval(this.language, script);
        }
    }

    private Object notMethod(Object[] args)
    {
        throw new UnsupportedOperationException("类型绑定器不允许被调用");
    }

    AmberInstance(
            Iterable<Language> languages,
            Class<TypeInterface> classScriptInterface,
            Consumer<Context.Builder> buildProxy,
            LockProxy lockProxy
    )
    {
        this(languages, List.of(), classScriptInterface, buildProxy, lockProxy);
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
