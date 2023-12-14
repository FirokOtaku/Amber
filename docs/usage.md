# 使用说明

## 单语言

最简单的情况下, 只需要一个脚本字符串和一个接口类, 就可以创建一个单语言的脚本接口:

```javascript
// hello.js
function hello(name)
{
    return "Hello, " + name + "!";
}
```

```java
import firok.amber.Amber;
import firok.amber.ScriptInterface;
import org.graalvm.polyglot.Source;

import java.util.List;

public interface SingleLangInterface extends ScriptInterface
{
    String hello(String name);

    public static SingleLangInterface create()
    {
        var contentHelloJs = Files.readString(Path.of("hello.js"));
        var source = Source.newBuilder("js", contentHelloJs, "hello.js").buildLiteral();
        return Amber.trap(
                List.of("js"),
                List.of(contentHelloJs),
                SingleLangInterface.class
        );
    }
}
```

## JavaScript ES 模块支持

## 多语言
