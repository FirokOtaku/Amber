package firok.amber.test;

import firok.amber.Amber;
import firok.amber.Field;
import firok.amber.ScriptInterface;
import org.graalvm.polyglot.Source;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
            """;
	@Language("PY")
	static final String ScriptPython = """
			def funPython():
				print('this message is print from Python context')
			valuePython = 'python'
			""";
	public interface TestInterface extends ScriptInterface
	{
		void funJS();
		@Field
		String valueJS();
		void funPython();
		@Field
		String valuePython();
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
			instance.funJS();
			instance.funPython();
			Assertions.assertEquals("js", instance.valueJS());
			Assertions.assertEquals("python", instance.valuePython());
		}
	}
}
