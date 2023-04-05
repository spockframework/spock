public final class Alpha extends java.lang.Enum<Alpha> {

    public static final Alpha A
    public static final Alpha B
    public static final Alpha C
    public static final Alpha MIN_VALUE
    public static final Alpha MAX_VALUE
    private static final Alpha[] $VALUES

    public static final Alpha[] values() {
        return $VALUES.clone()
    }

    public Alpha next() {
        java.lang.Object ordinal = this.ordinal().next()
        if ( ordinal >= $VALUES.size()) {
            ordinal = 0
        }
        return $VALUES.getAt( ordinal )
    }

    public Alpha previous() {
        java.lang.Object ordinal = this.ordinal().previous()
        if ( ordinal < 0) {
            ordinal = $VALUES.size().minus(1)
        }
        return $VALUES.getAt( ordinal )
    }

    public static Alpha valueOf(java.lang.String name) {
        return Alpha.valueOf(Alpha, name)
    }

    public static final Alpha $INIT(java.lang.Object[] para) {
        return this (* para )
    }

    static {
        A = Alpha.$INIT('A', 0)
        B = Alpha.$INIT('B', 1)
        C = Alpha.$INIT('C', 2)
        MIN_VALUE = A
        MAX_VALUE = C
        $VALUES = new Alpha[]{A, B, C}
    }

}