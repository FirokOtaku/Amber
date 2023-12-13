package firok.amber.test;

import firok.amber.*;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

/**
 * 测试单个实例里运行多种语言
 * */
public class PolyglotTests
{
	@Language("JS")
	static final String ScriptJS = """
            function funJS()
            {
                console.log('this message is print from JavaScript context')
            }
            const valueJS = 'js'
            function initPolyglot()
            {
                pub.funJS = funJS
            }
            function callPolyglot()
            {
                console.log('call Python function from JavaScript context:')
                pub.funPython()
            }
            """;
	@SuppressWarnings({"PyInterpreter", "PyUnresolvedReferences", "PyPep8Naming"})
	@Language("PY")
	static final String ScriptPython = """
            def funPython():
               print('this message is print from Python context')
            valuePython = 'python'
            
            def initPolyglot():
               pub['funPython'] = funPython
            def callPolyglot():
               print('call JS function from Python context:')
               pub['funJS']()
            """;
	public interface TestInterface extends ScriptInterface
	{
		void funJS();
		@Field
		String valueJS();
		void funPython();
		@Field
		String valuePython();
		@Method(context = "python", value = "initPolyglot")
		void initPolyglotPython();
		@Method(context = "js", value = "initPolyglot")
		void initPolyglotJS();
		@Method(context = "python", value = "callPolyglot")
		void callPolyglotPython();
		@Method(context = "js", value = "callPolyglot")
		void callPolyglotJS();
	}
	@Test
	public void testPolyglot() throws Exception
	{
		var sourceJS = Source.newBuilder("js", ScriptJS, "test.js").build();
		var sourcePython = Source.newBuilder("python", ScriptPython, "test.py").build();
		try(var instance = Amber.trap(
				List.of("js", "python"),
				List.of(sourceJS, sourcePython),
				TestInterface.class
		))
		{
			// 先简单调用多个语言提供的方法
			instance.funJS();
			instance.funPython();
			Assertions.assertEquals("js", instance.valueJS());
			Assertions.assertEquals("python", instance.valuePython());
			// 创建一个公共对象, 作为多个语言之间的交互变量
			var pub = Value.asValue(new HashMap<String, Object>());
			instance.set("js", "pub", pub);
			instance.set("python", "pub", pub);
			instance.initPolyglotJS();
			instance.initPolyglotPython();
			// 交叉调用
			instance.callPolyglotJS();
			instance.callPolyglotPython();
		}
	}
}
