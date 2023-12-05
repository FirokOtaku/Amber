package firok.amber;

import firok.topaz.general.ProgramMeta;
import firok.topaz.general.Version;
import firok.topaz.thread.LockProxy;
import org.graalvm.polyglot.Context;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("all")
public final class Amber
{
    private Amber() { }

    public static final ProgramMeta META = new ProgramMeta(
            "firok.amber",
            "Amber",
            new Version(3, 2, 0),
            "a personal Java lib",
            List.of("Firok"),
            List.of("https://github.com/FirokOtaku/Amber"),
            List.of("https://github.com/FirokOtaku/Amber"),
            "MulanPSL-2.0"
    );

    public static <TypeInterface extends ScriptInterface>
    TypeInterface trapWithScripts(
            Language polyglotLanguage,
            Iterable<String> scripts,
            Consumer<Context.Builder> builderProxy,
            LockProxy lockProxy,
            Class<TypeInterface> classScriptInterface
    )
    {
        return (TypeInterface) Proxy.newProxyInstance(
                classScriptInterface.getClassLoader(),
                new Class[] { classScriptInterface },
                new AmberInstance<>(
                        polyglotLanguage,
                        scripts,
                        classScriptInterface,
                        builderProxy,
                        lockProxy
                )
        );
    }

    public static <TypeInterface extends ScriptInterface>
    TypeInterface trapWithoutScripts(
            Iterable<Language> polyglotLanguages,
            Consumer<Context.Builder> builderProxy,
            LockProxy lockProxy,
            Class<TypeInterface> classScriptInterface
    )
    {
        return (TypeInterface) Proxy.newProxyInstance(
                classScriptInterface.getClassLoader(),
                new Class[] { classScriptInterface },
                new AmberInstance<>(
                        polyglotLanguages,
                        classScriptInterface,
                        builderProxy,
                        lockProxy
                )
        );
    }

    public static void withAllAccess(Context.Builder builder)
    {
        builder.allowAllAccess(true);
    }
}
