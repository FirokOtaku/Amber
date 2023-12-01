package firok.amber.test;

import firok.amber.ScriptInterface;
import firok.amber.SimpleScriptProxy;
import firok.amber.Type;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

@SuppressWarnings("all")
public class JavaTypeConvertTests
{
    public static class Bean
    {
        public String name;

        @Override
        public String toString()
        {
            return "Bean(" + name + ")";
        }
    }

    public interface BeanSupplier extends ScriptInterface
    {
        List<Bean> getBeans();
    }

    @Language("JS")
    public static final String ScriptSuccess = """
            const Bean = Java.type('firok.amber.test.JavaTypeConvertTests.Bean')
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
    public void testReturnBeanType()
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

    @Language("JS")
    public static final String ScriptInitWithType = """
            function getBeans()
            {
                const b1 = new Bean(), b2 = new Bean(), b3 = new Bean() // 直接实例化 Java 类
                const b4 = new Bean2() // 用自定义的名称实例化
                b1.name = '123'
                b2.name = '456'
                b3.name = '789'
                b4.name = '000'
                return [b1, b2, b3, b4]
            }
            """;

    public interface WithTypeInit extends ScriptInterface
    {
        /**
         * 方法返回值的类型在上下文初始化的时候就绑定进去了, 脚本里就不需要自己执行 Java.type 之类的操作了
         * */
        @Type Bean Bean();

        /**
         * Bean 类型会以 Bean2 的名字绑定
         * */
        @Type("Bean2") Bean anyName();

        List<Bean> getBeans();
    }

    @Test
    public void testTypeInit()
    {
        try(var beanSupplier = SimpleScriptProxy.connect(ScriptInitWithType, WithTypeInit.class))
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
