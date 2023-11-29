# Amber 琥珀

> ![amber](https://github.com/FirokOtaku/Amber/blob/main/docs/amber.jpg?raw=true)  
> 琥珀是松科松属植物的树脂化石，其状态透明似水晶，色泽如玛瑙。不透明的琥珀又称蜜蜡。  
> 自新石器时代开始，它的美就被人们赞誉。琥珀能制成各种装饰品，是从古至今备受重视的宝石。

> JavaScript's a trap!

一些对 GraalJS 的封装代码. 代码基于 **Java 21**.

由于 GraalJS 不支持 JVM 模块化, 遂从开启了模块化的 [Topaz 项目](https://github.com/FirokOtaku/Topaz) 分离出此项目.

* [改动记录](docs/changelog.md)
* [相关项目 - 托帕石](https://github.com/FirokOtaku/Topaz)

最新改动:

* **1.0.0**
  * 新增简易脚本代理

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

`firok.amber.test.SimpleScriptProxyTests` 内有示例调用代码.
