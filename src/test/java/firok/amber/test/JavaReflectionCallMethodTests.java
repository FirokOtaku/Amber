package firok.amber.test;

import org.junit.jupiter.api.Test;

public class JavaReflectionCallMethodTests
{
    interface TestInterface
    {
        default void log()
        {
            System.out.println("hello world");
        }
    }
    @Test
    void testCallDefaultMethod() throws Exception
    {
        var fun = TestInterface.class.getMethod("log");

        var ti1 = new TestInterface() {};
        var fun1 = ti1.getClass().getMethod("log");
        System.out.println(fun1.isDefault());
        fun1.invoke(ti1);

        var ti2 = new TestInterface() {
            @Override
            public void log()
            {
                System.out.println("hello world 2");
            }
        };
        var fun2 = ti2.getClass().getMethod("log");
        System.out.println(fun2.isDefault());
        fun2.invoke(ti2);

        System.out.println("original function isDefault");
        System.out.println(fun.isDefault());
        System.out.println("original function invoke on ti1");
        fun.invoke(ti1);
        System.out.println("original function invoke on ti2");
        fun.invoke(ti2);

        System.out.println("equals?");
        System.out.println(fun1.equals(fun));
        System.out.println(fun2.equals(fun));
    }
}
