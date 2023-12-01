package firok.amber.test;

import firok.amber.Field;
import firok.amber.Method;
import firok.amber.ScriptInterface;
import firok.amber.SimpleScriptProxy;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

public class SimpleScriptProxyTests
{
    @SuppressWarnings("all")
    @Language("JS")
    public static final String ScriptCallFunctionWithoutArg = """
            function log()
            {
                console.log(`this message is print from JavaScript context`)
            }
            """;
    public interface LogInterface extends ScriptInterface
    {
        void log();
    }

    /**
     * 对无参方法的直接调用
     * */
    @Test
    public void testCallFunctionWithoutArg()
    {
        try(var proxy = SimpleScriptProxy.connect(ScriptCallFunctionWithoutArg, LogInterface.class))
        {
            proxy.log();
        }
    }

    @SuppressWarnings("all")
    @Language("JS")
    public static final String ScriptFunctionWithArgCall = """
            function max(n1, n2) { return n1 > n2 ? n1 : n2 }
            function min(n1, n2) { return n1 < n2 ? n1 : n2 }
            """;
    public interface MathInterface extends ScriptInterface
    {
        int max(int n1, int n2);
        int min(int n1, int n2);
    }

    /**
     * 对有参方法的调用
     * */
    @Test
    public void testCallFunctionWithArg()
    {
        try(var proxy = SimpleScriptProxy.connect(ScriptFunctionWithArgCall, MathInterface.class))
        {
            Assertions.assertEquals(1, proxy.min(1, 100));
            Assertions.assertEquals(1, proxy.min(1, 200));
            Assertions.assertEquals(1, proxy.min(1, 300));
            Assertions.assertEquals(100, proxy.min(100, 300));
            Assertions.assertEquals(100, proxy.max(100, 0));
            Assertions.assertEquals(100, proxy.max(100, -100));
            Assertions.assertEquals(100, proxy.max(100, 50));
        }
    }

    @SuppressWarnings("all")
    @Language("JS")
    public static final String ScriptContextMemberBinding = """
            const num1 = 1
            const num2 = 2
            function rands(count)
            {
                const ret = []
                for(let step = 0; step < count ?? 0; step++)
                {
                    ret.push(Math.random())
                }
                return ret
            }
            """;
    public interface MemberBindingInterface extends ScriptInterface
    {
        @Field
        int num1();

        @Field("num2")
        int num2withDifferentMethodName();

        @Method("rands")
        List<Float> listRandomNumber(int count);
    }

    /**
     * 手动指定脚本内容对 Java 接口的映射
     * */
    @Test
    void testMemberBinding()
    {
        try(var proxyBinding = SimpleScriptProxy.connect(ScriptContextMemberBinding, MemberBindingInterface.class))
        {
            Assertions.assertEquals(1, proxyBinding.num1());
            Assertions.assertEquals(2, proxyBinding.num2withDifferentMethodName());
            var randoms = proxyBinding.listRandomNumber(10);
            Assertions.assertEquals(10, randoms.size());
            System.out.println(randoms.getClass());
            System.out.println(randoms);
        }
    }

    @SuppressWarnings("all")
    @Language("JS")
    public static final String ScriptContextOperation = """
            let value1 = 1
            function getValue1() { return value1 }
            function getValue2() { return value2 }
            const arr1 = [1, 2, 3]
            const arr2 = [1.1, 2.2, 3.3]
            const arr3 = ['s1', 's2', 's3']
            const arr4 = ['s1', 2, 3.1, false]
            """;

    public interface ScriptContextOperationInterface extends ScriptInterface
    {
        @Field
        int value1();

        @Field
        void value1(int value);

        @Field
        Integer value2();

        @Field
        void value2(Integer value);

        @Method
        int getValue1();

        @Method
        Integer getValue2();
    }

    @Test
    public void testContextOperation()
    {
        try(var proxy = SimpleScriptProxy.connect(ScriptContextOperation, ScriptContextOperationInterface.class))
        {
            Assertions.assertEquals(1, proxy.value1());
            Assertions.assertNull(proxy.value2());
            Assertions.assertTrue(proxy.has("value1"));
            Assertions.assertFalse(proxy.has("value2"));
            Assertions.assertEquals(1, proxy.getValue1());
            Assertions.assertThrowsExactly(UndeclaredThrowableException.class, () -> Assertions.assertNull(proxy.getValue2())); // 这个时候 value2 还没定义
            proxy.value1(2);
            proxy.value2(3);
            Assertions.assertTrue(proxy.has("value1"));
            Assertions.assertTrue(proxy.has("value2"));
            Assertions.assertEquals(2, proxy.get("value1", int.class));
            Assertions.assertEquals(2, proxy.get("value1", Integer.class));
            Assertions.assertEquals(3, proxy.get("value2", int.class));
            Assertions.assertEquals(3, proxy.get("value2", Integer.class));
            Assertions.assertEquals(2, proxy.value1());
            Assertions.assertEquals(3, proxy.value2());
            Assertions.assertEquals(2, proxy.getValue1());
            Assertions.assertEquals(3, proxy.getValue2());
            proxy.remove("value2");
            Assertions.assertFalse(proxy.has("value2"));
            Assertions.assertThrowsExactly(UndeclaredThrowableException.class, () -> Assertions.assertNull(proxy.getValue2()));

            Assertions.assertArrayEquals(new int[]{1, 2, 3}, proxy.get("arr1", int[].class)); // 整型没问题, 可以直接拿
            Assertions.assertArrayEquals(null, proxy.get("arr2", float[].class)); // 这么转换只能获取到 null, 从 JS 上下文转换要用 double 类型
            Assertions.assertArrayEquals(new double[]{1.1, 2.2, 3.3}, proxy.get("arr2", double[].class)); // 这样就没错了
            Assertions.assertArrayEquals(new String[]{"s1", "s2", "s3"}, proxy.get("arr3", String[].class));
            Assertions.assertArrayEquals(new Object[]{"s1", 2, 3.1d, false}, proxy.get("arr4", Object[].class));
        }
    }
}
