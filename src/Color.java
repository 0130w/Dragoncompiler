import java.util.HashMap;
import java.util.Map;

public enum Color {
    Reset(0),
    Magenta(35),
    ResetFore(39),
    LightRed(91),
    LightGreen(92),
    LightYellow(93),
    LightBlue(94),
    LightMagenta(95),
    LightCyan(96),
    White(97);

    private final int code;

    Color(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
