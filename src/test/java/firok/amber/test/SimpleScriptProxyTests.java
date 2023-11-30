package firok.amber.test;

import firok.amber.Field;
import firok.amber.Method;
import firok.amber.ScriptInterface;
import firok.amber.SimpleScriptProxy;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SimpleScriptProxyTests
{
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
}
