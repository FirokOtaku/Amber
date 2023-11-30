package firok.amber.test;

import firok.amber.ScriptInterface;
import firok.amber.SimpleScriptProxy;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class JavaTypeConvertTests
{
    public static class Bean
    {
        public String name;
    }

    public interface BeanSupplier extends ScriptInterface
    {
        List<Bean> getBeans();
    }

    @Language("JS")
    public static final String ScriptSuccess = """
            const Bean = Java.type('firok.amber.test.TypeConvertTests.Bean')
            function getBeans()
            {
                const b1 = new Bean(), b2 = new Bean(), b3 = new Bean()
                b1.name = '123'
                b2.name = '456'
                b3.name = '789'
                return [b1, b2, b3]
            }
            """;

    @Test
    public void testReturnBeanType() throws Exception
    {
        try(var beanSupplier = SimpleScriptProxy.connect(ScriptSuccess, BeanSupplier.class))
        {
            var beans = beanSupplier.getBeans();
            for (Bean bean : beans)
            {
                System.out.println(bean);
            }
        }
    }

    /**
     * 这样写会出现转换错误
     * */
    @Language("JS")
    public static final String ScriptError = """
            function getBeans()
            {
                return [
                    { name: '123' },
                    { name: '456' },
                    { name: '789' },
                ]
            }
            """;

    @Test
    public void testConvertError()
    {
        Assertions.assertThrowsExactly(ClassCastException.class, () -> {
            try(var beanSupplier = SimpleScriptProxy.connect(ScriptError, BeanSupplier.class))
            {
                // 这里 beans 的类型实际上是 com.oracle.truffle.polyglot.PolyglotList<com.oracle.truffle.polyglot.PolyglotMap>
                // 不是期望的 List<Bean>
                var beans = beanSupplier.getBeans();
                for (Bean bean : beans) // 所以会在这里抛出 ClassCastException
                {
                    System.out.println(bean);
                }
            }
            catch (Exception any)
            {
                any.printStackTrace(System.err);
                throw any;
            }
        });

    }
}
