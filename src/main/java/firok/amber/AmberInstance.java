package firok.amber;

import firok.topaz.function.MustCloseable;
import firok.topaz.reflection.Reflections;
import firok.topaz.thread.LockProxy;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

import javax.script.Bindings;
import java.lang.reflect.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static firok.topaz.general.Collections.isEmpty;

@SuppressWarnings({"FieldCanBeLocal", "DataFlowIssue"})
class AmberInstance<TypeInterface extends AutoCloseable> implements InvocationHandler
{
    private final LockProxy lockProxy;
    private final Context context;
    private final Map<String, Value> mapBinding;
    private final Map<Method, Function<Object[], ?>> mappedContext;
//    @NotNull private final static Method MethodClose = Reflections.methodOf(MustCloseable.class, "close");
//    @NotNull private final static Method MethodGet = Reflections.methodOf(ScriptInterface.class, "get", String.class);
//    @NotNull private final static Method MethodGetType = Reflections.methodOf(ScriptInterface.class, "get", String.class, Class.class);
//    @NotNull private final static Method MethodSet = Reflections.methodOf(ScriptInterface.class, "set", String.class, Object.class);
//    @NotNull private final static Method MethodHas = Reflections.methodOf(ScriptInterface.class, "has", String.class);
//    @NotNull private final static Method MethodRemove = Reflections.methodOf(ScriptInterface.class, "remove", String.class);
//    @NotNull private final static Method MethodEval = Reflections.methodOf(ScriptInterface.class, "eval", String.class);
//    @NotNull private final static Method MethodContext = Reflections.methodOf(ScriptInterface.class, "context");
//    @NotNull private final static Method MethodLanguage = Reflections.methodOf(ScriptInterface.class, "language");
    @NotNull private final static Method FunGet = Reflections.methodOf(ScriptInterface.class, "get", String.class);
    private Object funGet(Object[] args)
    {
        try
        {
            var key = (String) args[0];
            for(var language : languages)
            {
                var value = mapBinding.get(language).getMember(key);
                if(value != null) return value;
            }
            return null;
        }
        catch (Exception any) { return null; }
    }
    @NotNull private final static Method FunGetFrom = Reflections.methodOf(ScriptInterface.class, "get", String.class, String.class);
    private Object funGetFrom(Object[] args)
    {
        try
        {
            var language = (String) args[0];
            var key = (String) args[1];
            return mapBinding.get(language).getMember(key);
        }
        catch (Exception any) { return null; }
    }
    @NotNull private final static Method FunGetType = Reflections.methodOf(ScriptInterface.class, "get", String.class, Class.class);
    private Object funGetType(Object[] args)
    {
        try
        {
            var key = (String) args[0];
            var type = (Class<?>) args[1];
            for(var language : languages)
            {
                var value = mapBinding.get(language).getMember(key);
                if(value != null && !value.isNull())
                    return value.as(type);
            }
            return null;
        }
        catch (Exception any) { return null; }
    }
    @NotNull private final static Method FunGetTypeFrom = Reflections.methodOf(ScriptInterface.class, "get", String.class, String.class, Class.class);
    private Object funGetTypeFrom(Object[] args)
    {
        try
        {
            var language = (String) args[0];
            var key = (String) args[1];
            var type = (Class<?>) args[2];
            var value = mapBinding.get(language).getMember(key);
            if(value != null && !value.isNull())
                return value.as(type);
            return null;
        }
        catch (Exception any) { return null; }
    }
    @NotNull private final static Method FunSet = Reflections.methodOf(ScriptInterface.class, "set", String.class, Object.class);
    private Object funSet(Object[] args)
    {
        try
        {
            var key = (String) args[0];
            var value = args[1];
            for(var language : this.languages)
            {
                mapBinding.get(language).putMember(key, value);
            }
        }
        catch (Exception ignored) { }
        return null;
    }
    @NotNull private final static Method FunSetTo = Reflections.methodOf(ScriptInterface.class, "set", String.class, String.class, Object.class);
    private Object funSetTo(Object[] args)
    {
        try
        {
            var language = (String) args[0];
            var key = (String) args[1];
            var value = args[2];
            mapBinding.get(language).putMember(key, value);
        }
        catch (Exception ignored) { }
        return null;
    }
    @NotNull private final static Method FunHas = Reflections.methodOf(ScriptInterface.class, "has", String.class);
    private Object funHas(Object[] args)
    {
        try
        {
            var key = (String) args[0];
            for(var language : this.languages)
            {
                if(mapBinding.get(language).hasMember(key)) return true;
            }
            return false;
        }
        catch (Exception any) { return false; }
    }
    @NotNull private final static Method FunHasAt = Reflections.methodOf(ScriptInterface.class, "has", String.class, String.class);
    private Object funHasAt(Object[] args)
    {
        try
        {
            var language = (String) args[0];
            var key = (String) args[1];
            return mapBinding.get(language).hasMember(key);
        }
        catch (Exception any) { return false; }
    }
    @NotNull private final static Method FunRemove = Reflections.methodOf(ScriptInterface.class, "remove", String.class);
    @NotNull private final static Method FunRemoveAt = Reflections.methodOf(ScriptInterface.class, "remove", String.class, String.class);
    @NotNull private final static Method FunEval = Reflections.methodOf(ScriptInterface.class, "eval", String.class);
    @NotNull private final static Method FunEvalAt = Reflections.methodOf(ScriptInterface.class, "eval", String.class, String.class);
    @NotNull private final static Method FunContext = Reflections.methodOf(ScriptInterface.class, "context");
    @NotNull private final static Method FunLanguages = Reflections.methodOf(ScriptInterface.class, "languages");
    @NotNull private final static Method FunClose = Reflections.methodOf(MustCloseable.class, "close");
    private Object funClose(Object[] args)
    {
        try { this.context.close(); }
        catch (Exception ignored) { }
        return null;
    }

    private Object funFieldSetter()
    {
        ;
    }
    private Object funFieldGetter()
    {
        ;
    }
    private Object funMethod()
    {
        ;
    }

    private final String[] languages;

    private Object notMethod(Object[] args)
    {
        throw new UnsupportedOperationException("类型绑定器不允许被调用");
    }

    AmberInstance(
            Iterable<String> languages,
            Iterable<Source> scripts,
            Class<TypeInterface> classScriptInterface,
            Consumer<Context.Builder> buildProxy,
            LockProxy lockProxy
    )
    {
        var listLanguage = new ArrayList<String>();
        languages.forEach(listLanguage::add);
        this.languages = listLanguage.toArray(new String[0]);

        if (buildProxy != null)
        {
            var builder = Context.newBuilder(this.languages);
            buildProxy.accept(builder);
            this.context = builder.build();
        }
        else
        {
            this.context = Context.newBuilder(this.languages).allowAllAccess(true).build();
        }

        this.mapBinding = new HashMap<>();
        for(var language : this.languages)
        {
            mapBinding.put(language, context.getBindings(language));
        }

        // 执行初始化
        var annoInit = classScriptInterface.getAnnotation(Init.class);
        if(annoInit != null)
        {
            for(var type : annoInit.types())
            {
                var targetClass = type.target();
                var targetName = "".equals(type.name()) ? targetClass.getSimpleName() : type.name();
                for(var language : this.languages)
                {
                    mapBinding.get(language).putMember(targetName, targetClass);
                }
            }
            for(var script: annoInit.scripts())
            {
                try
                {
                    var source = Source.newBuilder(script.language(), script.source(), script.name())
                            .mimeType(script.mimeType())
                            .interactive(script.interactive())
                            .internal(script.internal())
                            .build();
                    context.eval(source);
                }
                catch (Exception any)
                {
                    throw new IllegalArgumentException("初始化并执行脚本内容失败", any);
                }
            }
        }

        // 运行用户脚本
        scripts.forEach(context::eval);

        // 读取并创建绑定
        this.lockProxy = lockProxy;
        this.mappedContext = new HashMap<>();

        for (var method : classScriptInterface.getMethods())
        {
            if (Modifier.isStatic(method.getModifiers())) continue; // 暂时不处理静态方法

            if(FunClose.equals(method)) this.mappedContext.put(method, this::funClose);
            else if(FunGet.equals(method)) this.mappedContext.put(method, this::funGet);
            else if(FunGetFrom.equals(method)) this.mappedContext.put(method, this::funGetFrom);
            else if(FunGetType.equals(method)) this.mappedContext.put(method, this::funGetType);
            else if(FunGetTypeFrom.equals(method)) this.mappedContext.put(method, this::funGetTypeFrom);
            else if(FunSet.equals(method)) this.mappedContext.put(method, this::funSet);
            else if(FunSetTo.equals(method)) this.mappedContext.put(method, this::funSetTo);
            else if(FunHas.equals(method)) this.mappedContext.put(method, this::funHas);
            else if(FunHasAt.equals(method)) this.mappedContext.put(method, this::funHasAt);
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
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception
    {
        try
        {
            lockProxy.lock();
            var function = this.mappedContext.get(method);
            if(function == null)
                throw new NoSuchMethodException(method.getName());
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