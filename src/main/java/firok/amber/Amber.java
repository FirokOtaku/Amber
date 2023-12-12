package firok.amber;

import firok.topaz.general.ProgramMeta;
import firok.topaz.general.Version;
import firok.topaz.thread.LockProxy;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Language;
import org.graalvm.polyglot.Source;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("all")
public final class Amber
{
    private Amber() { }

    public static final ProgramMeta META = new ProgramMeta(
            "firok.amber",
            "Amber",
            new Version(4, 0, 0),
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
            LockProxy lockProxy,
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
                        builderProxy,
                        lockProxy
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
                new firok.topaz.thread.ReentrantLockProxy(),
                classScriptInterface
        );
    }

    public static void withAllAccess(Context.Builder builder)
    {
        builder.allowAllAccess(true);
    }
}
