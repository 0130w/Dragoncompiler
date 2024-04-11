public enum Effect {
    UnknownEffect(-1),
    Underlined(4),
    ResetUnderlined(24),
    ;
    private final int code;
    Effect(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
    public int getResetCode() {
        return code + 20;
    }
}
