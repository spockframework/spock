public class Assertions extends java.lang.Object {

    @spock.lang.Verify
    public void isPositiveAndEven(int a) {
        org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
        org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
        try {
            org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'a > 0', 5, 9, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), a) > $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), 0)))
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'a > 0', 5, 9, null, $spock_condition_throwable)}
        finally {
        }
        try {
            org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'a % 2 == 0', 6, 9, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(4), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), a) % $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), 2)) == $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(3), 0)))
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'a % 2 == 0', 6, 9, null, $spock_condition_throwable)}
        finally {
        }
    }

}