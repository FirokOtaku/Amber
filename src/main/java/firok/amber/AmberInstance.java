package firok.amber;

import firok.topaz.function.MayFunction;
import firok.topaz.function.MustCloseable;
import firok.topaz.reflection.Reflections;
import firok.topaz.thread.LockProxy;
import firok.topaz.thread.ReentrantLockProxy;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.OperationNotSupportedException;
import java.lang.reflect.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

import static firok.topaz.general.Collections.isEmpty;

@SuppressWarnings({"FieldCanBeLocal", "DataFlowIssue"})
class AmberInstance<TypeInterface extends AutoCloseable> implements InvocationHandler
{
    private final LockProxy lockProxy;
    private final Context context;
    private final Map<String, Value> mapBinding;
    private final Map<Method, MayFunction<Object[], ?>> mappedContext;
    @NotNull private final static Method FunGet = Reflections.methodOf(ScriptInterface.class, "get", String.class);
    private Object funGet(Object[] args)
    {
        var key = (String) args[0];
        for(var language : languages)
        {
            var value = mapBinding.get(language).getMember(key);
            if(value != null) return value;
        }
        return null;
    }
    @NotNull private final static Method FunGetFrom = Reflections.methodOf(ScriptInterface.class, "get", String.class, String.class);
    private Object funGetFrom(Object[] args)
    {
        var language = (String) args[0];
        var key = (String) args[1];
        return mapBinding.get(language).getMember(key);
    }
    @NotNull private final static Method FunGetType = Reflections.methodOf(ScriptInterface.class, "get", String.class, Class.class);
    private Object funGetType(Object[] args)
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
    @NotNull private final static Method FunGetTypeFrom = Reflections.methodOf(ScriptInterface.class, "get", String.class, String.class, Class.class);
    private Object funGetTypeFrom(Object[] args)
    {
        var language = (String) args[0];
        var key = (String) args[1];
        var type = (Class<?>) args[2];
        var value = mapBinding.get(language).getMember(key);
        if(value != null && !value.isNull())
            return value.as(type);
        return null;
    }
    @NotNull private final static Method FunSet = Reflections.methodOf(ScriptInterface.class, "set", String.class, Object.class);
    private Object funSet(Object[] args)
    {
        var key = (String) args[0];
        var value = args[1];
        for(var language : this.languages)
        {
            mapBinding.get(language).putMember(key, value);
        }
        return null;
    }
    @NotNull private final static Method FunSetTo = Reflections.methodOf(ScriptInterface.class, "set", String.class, String.class, Object.class);
    private Object funSetTo(Object[] args)
    {
        var language = (String) args[0];
        var key = (String) args[1];
        var value = args[2];
        mapBinding.get(language).putMember(key, value);
        return null;
    }
    @NotNull private final static Method FunHas = Reflections.methodOf(ScriptInterface.class, "has", String.class);
    private Object funHas(Object[] args)
    {
        var key = (String) args[0];
        for(var language : this.languages)
        {
            if(mapBinding.get(language).hasMember(key)) return true;
        }
        return false;
    }
    @NotNull private final static Method FunHasAt = Reflections.methodOf(ScriptInterface.class, "has", String.class, String.class);
    private Object funHasAt(Object[] args)
    {
        var language = (String) args[0];
        var key = (String) args[1];
        return mapBinding.get(language).hasMember(key);
    }
    @NotNull private final static Method FunRemove = Reflections.methodOf(ScriptInterface.class, "remove", String.class);
    private Object funRemove(Object[] args)
    {
        var key = (String) args[0];
        for(var language : this.languages)
        {
            mapBinding.get(language).removeMember(key);
        }
        return null;
    }
    @NotNull private final static Method FunRemoveAt = Reflections.methodOf(ScriptInterface.class, "remove", String.class, String.class);
    private Object funRemoveAt(Object[] args)
    {
        var language = (String) args[0];
        var key = (String) args[1];
        mapBinding.get(language).removeMember(key);
        return null;
    }
    @NotNull private final static Method FunEval = Reflections.methodOf(ScriptInterface.class, "eval", String.class);
    private Object funEval(Object[] args) throws Exception
    {
        var script = (String) args[0];
        if(this.languages.length > 1)
            throw new OperationNotSupportedException("eval(String) method is allowed when only one language is used");
        return this.context.eval(this.languages[0], script);
    }
    @NotNull private final static Method FunEvalAt = Reflections.methodOf(ScriptInterface.class, "eval", String.class, String.class);
    private Object funEvalAt(Object[] args)
    {
        var language = (String) args[0];
        var script = (String) args[1];
        return this.context.eval(language, script);
    }
    @NotNull private final static Method FunEvalSource = Reflections.methodOf(ScriptInterface.class, "eval", Source.class);
    private Object funEvalSource(Object[] args)
    {
        var source = (Source) args[0];
        return this.context.eval(source);
    }
    @NotNull private final static Method FunContext = Reflections.methodOf(ScriptInterface.class, "context");
    private Object funContext(Object[] args)
    {
        return this.context;
    }
    @NotNull private final static Method FunLanguages = Reflections.methodOf(ScriptInterface.class, "languages");
    private Object funLanguages(Object[] args)
    {
        return this.languages.clone();
    }
    @NotNull private final static Method FunClose = Reflections.methodOf(MustCloseable.class, "close");
    private Object funClose(Object[] args)
    {
        try { this.context.close(); }
        catch (Exception ignored) { }
        return null;
    }

    private final String[] languages;
    private Value[] getBindings(String language)
    {
        if(language == null) return mapBinding.values().toArray(new Value[0]);
        else return new Value[] { mapBinding.get(language) };
    }

    AmberInstance(
            Iterable<String> languages,
            Iterable<Source> scripts,
            Class<TypeInterface> classScriptInterface,
            Consumer<Context.Builder> buildProxy
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
        this.lockProxy = new ReentrantLockProxy();
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
            else if(FunRemove.equals(method)) this.mappedContext.put(method, this::funRemove);
            else if(FunRemoveAt.equals(method)) this.mappedContext.put(method, this::funRemoveAt);
            else if(FunEval.equals(method)) this.mappedContext.put(method, this::funEval);
            else if(FunEvalAt.equals(method)) this.mappedContext.put(method, this::funEvalAt);
            else if(FunEvalSource.equals(method)) this.mappedContext.put(method, this::funEvalSource);
            else if(FunContext.equals(method)) this.mappedContext.put(method, this::funContext);
            else if(FunLanguages.equals(method)) this.mappedContext.put(method, this::funLanguages);
            else // 转发到脚本引擎内部实现
            {
                var annoField = method.getAnnotation(firok.amber.Field.class);
                var annoMethod = method.getAnnotation(firok.amber.Method.class);
                if(annoField != null && annoMethod != null) throw new IllegalArgumentException("不能为方法指定多个目标");
                var nameMethod = method.getName();
                var countParam = method.getParameterCount();
                var typeReturnValue = method.getReturnType();
                var languageTarget = getLanguageTarget(annoField, annoMethod);

                final boolean isField = annoField != null;
                String nameTarget;
                if(isField)
                {
                    if(countParam > 1) throw new IllegalArgumentException("字段操作器只能有零或一个形参");

                    nameTarget = "".equals(annoField.value()) ? nameMethod : annoField.value();

                    if(countParam == 0)
                    {
                        if(languageTarget == null)
                        {
                            this.mappedContext.put(method, (args) -> {
                                for(var language : AmberInstance.this.languages)
                                {
                                    var bindings = mapBinding.get(language);
                                    if(bindings.hasMember(nameTarget))
                                        return bindings.getMember(nameTarget).as(typeReturnValue);
                                }
                                return null;
                            });
                        }
                        else
                        {
                            this.mappedContext.put(method, (args) -> {
                                var bindings = mapBinding.get(languageTarget);
                                return bindings.hasMember(nameTarget) ?
                                        bindings.getMember(nameTarget).as(typeReturnValue) :
                                        null;
                            });
                        }
                    }
                    else // countParam == 1
                    {
                        if(languageTarget == null)
                        {
                            this.mappedContext.put(method, (args) -> {
                                for(var language : AmberInstance.this.languages)
                                {
                                    var bindings = mapBinding.get(language);
                                    bindings.putMember(nameTarget, args[0]);
                                }
                                return null;
                            });
                        }
                        else
                        {
                            this.mappedContext.put(method, (args) -> {
                                var bindings = mapBinding.get(languageTarget);
                                bindings.putMember(nameTarget, args[0]);
                                return null;
                            });
                        }
                    }
                }
                else
                {
                    nameTarget = annoMethod != null && !"".equals(annoMethod.value()) ?
                            annoMethod.value() :
                            nameMethod;

                    if(languageTarget == null)
                    {
                        if(typeReturnValue == void.class || typeReturnValue == Void.class)
                        {
                            this.mappedContext.put(method, (args) -> {
                                var isEmptyArgs = isEmpty(args);
                                for(var language : AmberInstance.this.languages)
                                {
                                    var bindings = mapBinding.get(language);
                                    if(bindings.hasMember(nameTarget))
                                    {
                                        var target = bindings.getMember(nameTarget);
                                        if(target.canExecute())
                                        {
                                            if(isEmptyArgs) target.executeVoid();
                                            else target.executeVoid(args);
                                            return null;
                                        }
                                    }
                                }
                                throw new NoSuchMethodException("所有上下文内都不存在指定方法: " + nameTarget);
                            });
                        }
                        else
                        {
                            this.mappedContext.put(method, (args) -> {
                                var isEmptyArgs = isEmpty(args);
                                for(var language : AmberInstance.this.languages)
                                {
                                    var bindings = mapBinding.get(language);
                                    if(bindings.hasMember(nameTarget))
                                    {
                                        var target = bindings.getMember(nameTarget);
                                        if(target.canExecute())
                                        {
                                            return isEmptyArgs ?
                                                    target.execute().as(typeReturnValue) :
                                                    target.execute(args).as(typeReturnValue);
                                        }
                                    }
                                }
                                throw new NoSuchMethodException("所有上下文内都不存在指定方法: " + nameTarget);
                            });
                        }
                    }
                    else
                    {
                        if(typeReturnValue == void.class || typeReturnValue == Void.class)
                        {
                            this.mappedContext.put(method, (args) -> {
                                var isEmptyArgs = isEmpty(args);
                                var bindings = mapBinding.get(languageTarget);
                                if(bindings.hasMember(nameTarget))
                                {
                                    var target = bindings.getMember(nameTarget);
                                    if(target.canExecute())
                                    {
                                        if(isEmptyArgs) target.executeVoid();
                                        else target.executeVoid(args);
                                        return null;
                                    }
                                }
                                throw new NoSuchMethodException("指定上下文内不存在指定方法: " + nameTarget);
                            });
                        }
                        else
                        {
                            this.mappedContext.put(method, (args) -> {
                                var isEmptyArgs = isEmpty(args);
                                var bindings = mapBinding.get(languageTarget);
                                if(bindings.hasMember(nameTarget))
                                {
                                    var target = bindings.getMember(nameTarget);
                                    if(target.canExecute())
                                    {
                                        return isEmptyArgs ?
                                                target.execute().as(typeReturnValue) :
                                                target.execute(args).as(typeReturnValue);
                                    }
                                }
                                throw new NoSuchMethodException("指定上下文内不存在指定方法: " + nameTarget);
                            });
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private String getLanguageTarget(Field annoField, firok.amber.Method annoMethod)
    {
        String languageTarget;
        if(annoField != null && !"".equals(annoField.lang()))
            languageTarget = annoField.lang();
        else if(annoMethod != null && !"".equals(annoMethod.lang()))
            languageTarget = annoMethod.lang();
        else languageTarget = null;
        if(languageTarget != null)
        {
            boolean hasLanguageTarget = false;
            for(var language : this.languages)
            {
                if(language.equals(languageTarget))
                {
                    hasLanguageTarget = true;
                    break;
                }
            }
            if(!hasLanguageTarget) throw new IllegalArgumentException("脚本引擎不包含指定的语言上下文: " + languageTarget);
        }
        return languageTarget;
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
