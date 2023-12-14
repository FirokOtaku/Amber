package firok.amber.test;

import firok.amber.Amber;
import firok.amber.Method;
import firok.amber.ScriptInterface;
import org.graalvm.polyglot.Source;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 对于方法和字段的映射的进行于运行时的:
 * interface 里声明的成员, 不一定必须在 Amber 实例化的时候就存在,
 * 也可以实例化之后再调用 {@link ScriptInterface#eval(Source)} 之类的方法添加到上下文里.
 * */
public class PostInitBindingTests
{
    @Language("JS")
    public static final String ScriptPostInit = """
            function testPostInit()
            {
                console.log('test post init')
                return 123
            }
            """;
    public interface PostInitInterface extends ScriptInterface
    {
        @Method(lang = "js", value = "testPostInit")
        int testPostInit();
    }
    @Test
    void testPostInit()
    {
        var pii = Amber.trap(List.of("js"), List.of(), PostInitInterface.class);
        // 一开始的时候上下文里不存在指定的方法, 调用会报错
        Assertions.assertThrows(Exception.class, pii::testPostInit);
        // 执行脚本, 这个时候上下文里就有指定方法了
        pii.eval("js", ScriptPostInit);
        Assertions.assertEquals(123, pii.testPostInit());
    }
}
