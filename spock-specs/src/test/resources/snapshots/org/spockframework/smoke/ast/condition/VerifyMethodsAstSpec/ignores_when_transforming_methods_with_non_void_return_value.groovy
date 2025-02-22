public class Assertions extends java.lang.Object {

    @spock.lang.Verify
    public static boolean isPositiveWithReturn(int a) {
        a > 0
        return true
    }

}