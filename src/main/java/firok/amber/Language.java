package firok.amber;

/**
 * 列出目前 Amber 支持的语言
 * @since 4.0.0
 * @author Firok
 * */
public enum Language
{
	JavaScript("js"),
	Python("python"),

	;

	public final String id;
	Language(String id)
	{
		this.id = id;
	}
}
