public class Assertions extends java.lang.Object {

    @spock.lang.VerifyAll
    public static boolean isPositiveWithReturn(int a) {
        a > 0
        return true
    }

}