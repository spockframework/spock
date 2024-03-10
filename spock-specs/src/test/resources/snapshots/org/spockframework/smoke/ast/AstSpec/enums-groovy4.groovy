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
        java.lang.Integer ordinal = this.ordinal() + 1
        if ( ordinal >= $VALUES.length) {
            return MIN_VALUE
        }
        return $VALUES [ ordinal ]
    }

    public Alpha previous() {
        java.lang.Integer ordinal = this.ordinal() - 1
        if ( ordinal < 0) {
            return MAX_VALUE
        }
        return $VALUES [ ordinal ]
    }

    public static Alpha valueOf(java.lang.String name) {
        return java.lang.Enum.valueOf(Alpha, name)
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