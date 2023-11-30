package firok.amber;

import firok.topaz.general.ProgramMeta;
import firok.topaz.general.Version;

import java.util.List;

public final class Amber
{
    private Amber() { }

    public static final ProgramMeta META = new ProgramMeta(
            "firok.amber",
            "Amber",
            new Version(2, 0, 1),
            "a personal Java lib",
            List.of("Firok"),
            List.of("https://github.com/FirokOtaku/Amber"),
            List.of("https://github.com/FirokOtaku/Amber"),
            "MulanPSL-2.0"
    );
}
