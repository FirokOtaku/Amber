package firok.amber;

import firok.topaz.annotation.Unstable;
import firok.topaz.general.ProgramMeta;
import firok.topaz.general.Version;
import firok.topaz.thread.LockProxy;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static firok.topaz.general.Collections.isEmpty;

@SuppressWarnings("all")
@Unstable
public final class Amber
{
    private Amber() { }

    public static final ProgramMeta META = new ProgramMeta(
            "firok.amber",
            "Amber",
            new Version(4, 0, 1),
            "a personal Java lib",
            List.of("Firok"),
            List.of("https://github.com/FirokOtaku/Amber"),
            List.of("https://github.com/FirokOtaku/Amber"),
            "MulanPSL-2.0"
    );

    /**
     * @since 4.0.0
     * */
    public static <TypeInterface extends ScriptInterface>
    TypeInterface trap(
            Iterable<String> polyglotLanguages,
            Iterable<Source> scripts,
            Consumer<Context.Builder> builderProxy,
            Class<TypeInterface> classScriptInterface
    )
    {
        var setLang = new HashSet<String>();
        polyglotLanguages.forEach(setLang::add);
        scripts.forEach(script -> setLang.add(script.getLanguage()));
//        setLang.add(polyglotLanguage);

        return (TypeInterface) Proxy.newProxyInstance(
                classScriptInterface.getClassLoader(),
                new Class[] { classScriptInterface },
                new AmberInstance<>(
                        setLang,
                        scripts,
                        classScriptInterface,
                        builderProxy
                )
        );
    }

    public static <TypeInterface extends ScriptInterface>
    TypeInterface trap(
            Iterable<String> polyglotLanguage,
            Iterable<Source> scripts,
            Class<TypeInterface> classScriptInterface
    )
    {
        return trap(
                polyglotLanguage,
                scripts,
                Amber::withAllAccess,
                classScriptInterface
        );
    }

    static void checkNoneImplements(Class<?> classTarget, Class<?>... arrNotClass)
    {
        if(isEmpty(arrNotClass)) return;
        var setInterface = Set.of(classTarget.getInterfaces());
        for(var notClass : arrNotClass)
        {
            if(setInterface.contains(notClass))
                AmberExceptions.InvalidImplements.occur(
                        new IllegalArgumentException("class %s implements %s, which is not allowed".formatted(classTarget, notClass))
                );
        }
    }

    public static <TypeInterface extends SingleLanguageScript> TypeInterface
    trap(String language, Class<TypeInterface> classInterface)
    {
        checkNoneImplements(classInterface, MultiLanguageScript.class, JavaScriptModuleScript.class);
        return null;
    }

    public static <TypeInterface extends JavaScriptModuleScript> TypeInterface
    trap(Class<TypeInterface> classInterface)
    {
        checkNoneImplements(classInterface, SingleLanguageScript.class, MultiLanguageScript.class);
        return null;
    }

    public static <TypeInterface extends MultiLanguageScript> TypeInterface
    trap(Iterable<String> languages, Class<TypeInterface> classInterface)
    {
        checkNoneImplements(classInterface, SingleLanguageScript.class, JavaScriptModuleScript.class);
        return null;
    }

    public static void withAllAccess(Context.Builder builder)
    {
        builder.allowAllAccess(true);
    }
}
