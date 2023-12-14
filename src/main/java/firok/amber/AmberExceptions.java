package firok.amber;

import firok.topaz.general.CodeExceptionThrower;
import firok.topaz.general.I18N;
import org.jetbrains.annotations.Nullable;

public enum AmberExceptions implements CodeExceptionThrower
{
    /**
     * 提供的接口类存在错误的 implements 列表
     * */
    InvalidImplements(10),
    ;

    private final int code;
    AmberExceptions(int code)
    {
        this.code = code;
    }

    @Override
    public int getExceptionCode()
    {
        return 0;
    }

    @Override
    public @Nullable I18N getI18N()
    {
        return null;
    }
}
