# Amber 琥珀

> ![amber](https://github.com/FirokOtaku/Amber/blob/master/docs/amber.jpg?raw=true)  
> 琥珀是松科松属植物的树脂化石，其状态透明似水晶，色泽如玛瑙。不透明的琥珀又称蜜蜡。  
> 自新石器时代开始，它的美就被人们赞誉。琥珀能制成各种装饰品，是从古至今备受重视的宝石。

> JavaScript's a trap!

为了方便使用 Java 和 JavaScript 做交叉编程,  
对 GraalJS 进行了一点微小的封装,  
将一些初始化解释器和类型转换的样板代码封装成工厂方法以供使用.

由于 GraalJS 不支持 JVM 模块化,  
不方便把这部分代码放在 [Topaz 项目](https://github.com/FirokOtaku/Topaz) 里,  
所以单独分离作为一个子项目.

代码基于 **Java 21**.

* [改动记录](docs/changelog.md)
* [相关项目 - Topaz 托帕石](https://github.com/FirokOtaku/Topaz)

最新改动:

* **4.0.0**
  * 接口重做, 支持多语言脚本处理

## 安装

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/FirokOtaku/Amber</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>firok</groupId>
    <artifactId>amber</artifactId>
    <version>{VERSION}</version>
  </dependency>
</dependencies>
```

## 使用

`firok.amber.test.SimpleScriptProxyTests` 内有基础示例调用代码.

请注意, 由于 GraalJS 的 `Value.as` 方法不能直接将 JavaScript Object 转换为 Java Object,
如果你需要映射的方法所返回 Java 类型, 目前已知两种方式可以实现:

1. (推荐) 在 JavaScript 脚本中使用 `Java.type()` 等 API 先获取到 Java 类型, 然后对其实例化
2. 从 JavaScript 脚本返回 `JSON.stringify()` 后的字符串, 然后在 Java 中使用 Jackson 或 GSON 等库转换为 Java 类型

详见 `firok.amber.test.JavaTypeConvertTests`.
