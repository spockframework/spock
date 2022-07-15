package org.spockframework.smoke.ast.condition

import org.spockframework.EmbeddedSpecification

class CollectionConditionAstSpec extends EmbeddedSpecification {

  def "collection condition matchCollectionsAsSet is transformed correctly"() {
    when:
    def result = compiler.transpileFeatureBody('''
        given:
        def x = [1]
        expect:
        x =~ [1]
    ''')

    then:
    /* language=Groovy */
    result.source == '''\
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    java.lang.Object x = [1]
    try {
        org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'x =~ [1]', 4, 9, null, org.spockframework.runtime.SpockRuntime, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), 'matchCollectionsAsSet'), new java.lang.Object[]{$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), x), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(3), [$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), 1)])}, $spock_valueRecorder.realizeNas(6, false), false, 5)
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'x =~ [1]', 4, 9, null, $spock_condition_throwable)}
    finally {
    }
    this.getSpecificationContext().getMockController().leaveScope()
}'''
  }

  def "collection condition matchCollectionsInAnyOrder is transformed correctly"() {
    when:
    def result = compiler.transpileFeatureBody('''
        given:
        def x = [1]
        expect:
        x ==~ [1]
    ''')

    then:
    /* language=Groovy */
    result.source == '''\
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    java.lang.Object x = [1]
    try {
        org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'x ==~ [1]', 4, 9, null, org.spockframework.runtime.SpockRuntime, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), 'matchCollectionsInAnyOrder'), new java.lang.Object[]{$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), x), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(3), [$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), 1)])}, $spock_valueRecorder.realizeNas(6, false), false, 5)
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'x ==~ [1]', 4, 9, null, $spock_condition_throwable)}
    finally {
    }
    this.getSpecificationContext().getMockController().leaveScope()
}'''
  }

  def "regex find conditions are transformed correctly"() {
    when:
    def result = compiler.transpileFeatureBody('''
        given:
        def x = "[1]"
        expect:
        x =~ /\\d/
    ''')

    then:
    /* language=Groovy */
    result.source == '''\
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    java.lang.Object x = '[1]\'
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'x =~ /\\\\d/', 4, 9, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), x) =~ $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), '\\\\d')))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'x =~ /\\\\d/', 4, 9, null, $spock_condition_throwable)}
    finally {
    }
    this.getSpecificationContext().getMockController().leaveScope()
}'''
  }

  def "regex match conditions are transformed correctly"() {
    when:
    def result = compiler.transpileFeatureBody('''
        given:
        def x = "a1b"
        expect:
        x ==~ /a\\db/
    ''')

    then:
    /* language=Groovy */
    result.source == '''\
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    java.lang.Object x = 'a1b'
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'x ==~ /a\\\\db/', 4, 9, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), x) ==~ $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), 'a\\\\db')))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'x ==~ /a\\\\db/', 4, 9, null, $spock_condition_throwable)}
    finally {
    }
    this.getSpecificationContext().getMockController().leaveScope()
}'''
  }
}
