package spock.platform

import org.junit.jupiter.api.Test
import org.junit.platform.launcher.PostDiscoveryFilter
import org.junit.platform.launcher.TagFilter
import org.junit.platform.testkit.engine.EngineExecutionResults
import spock.testkit.testsources.SimpleTagSpec

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage

class TagCompatibilityTest extends SpockEngineBase {

  public static final String PARENT_SIMPLE_FEATURE = '[engine:spock]/[spec:spock.testkit.testsources.SimpleTagSpec]/[feature:$spock_feature_0_0]'
  public static final String CHILD_SIMPLE_FEATURE = '[engine:spock]/[spec:spock.testkit.testsources.ChildTagSpec]/[feature:$spock_feature_0_0]'
  public static final String PARENT_COMPLEX_FEATURE = '[engine:spock]/[spec:spock.testkit.testsources.SimpleTagSpec]/[feature:$spock_feature_0_1]'
  public static final String CHILD_COMPLEX_FEATURE = '[engine:spock]/[spec:spock.testkit.testsources.ChildTagSpec]/[feature:$spock_feature_0_1]'
  public static final String PARENT_BLAND_FEATURE =  '[engine:spock]/[spec:spock.testkit.testsources.SimpleTagSpec]/[feature:$spock_feature_0_2]'
  public static final String CHILD_BLAND_FEATURE =  '[engine:spock]/[spec:spock.testkit.testsources.ChildTagSpec]/[feature:$spock_feature_0_2]'
  public static final String CHILD_CHILD_FEATURE = '[engine:spock]/[spec:spock.testkit.testsources.ChildTagSpec]/[feature:$spock_feature_1_0]'
  public static final String CHILD_PLAIN_FEATURE = '[engine:spock]/[spec:spock.testkit.testsources.SimpleTagSpec]/[feature:$spock_feature_1_1]'

  @Test
  void executesSimpleTag() {
    PostDiscoveryFilter filter = TagFilter.includeTags("simple")
    def results = executeWithTagFilter(filter)
    assert succeededUids(results) =~ [PARENT_SIMPLE_FEATURE, CHILD_SIMPLE_FEATURE]
  }

  @Test
  void executesComplexTag() {
    PostDiscoveryFilter filter = TagFilter.includeTags("complex")
    def results = executeWithTagFilter(filter)
    assert succeededUids(results) =~ [PARENT_COMPLEX_FEATURE, CHILD_COMPLEX_FEATURE]
  }

  @Test
  void executesSharedTag() {
    PostDiscoveryFilter filter = TagFilter.includeTags("shared")
    def results = executeWithTagFilter(filter)
    assert succeededUids(results) =~ [PARENT_SIMPLE_FEATURE, PARENT_COMPLEX_FEATURE, CHILD_SIMPLE_FEATURE, CHILD_COMPLEX_FEATURE, CHILD_CHILD_FEATURE]
  }

  @Test
  void executesChildTag() {
    PostDiscoveryFilter filter = TagFilter.includeTags("child")
    def results = executeWithTagFilter(filter)
    assert succeededUids(results) == [CHILD_CHILD_FEATURE]
  }

  @Test
  void executesInheritedTag() {
    PostDiscoveryFilter filter = TagFilter.includeTags("inherited")
    def results = executeWithTagFilter(filter)
    assert succeededUids(results) =~ [PARENT_SIMPLE_FEATURE, PARENT_COMPLEX_FEATURE, CHILD_SIMPLE_FEATURE, CHILD_COMPLEX_FEATURE, CHILD_CHILD_FEATURE, PARENT_BLAND_FEATURE, CHILD_BLAND_FEATURE, CHILD_PLAIN_FEATURE]
  }

  @Test
  void executesChildSpecTag() {
    PostDiscoveryFilter filter = TagFilter.includeTags("childSpec")
    def results = executeWithTagFilter(filter)
    assert succeededUids(results) =~ [CHILD_SIMPLE_FEATURE, CHILD_COMPLEX_FEATURE, CHILD_CHILD_FEATURE, CHILD_BLAND_FEATURE, CHILD_PLAIN_FEATURE]
  }

  private ArrayList<String> succeededUids(EngineExecutionResults results) {
    results.testEvents().succeeded().list().testDescriptor.uniqueId*.toString()
  }

  private EngineExecutionResults executeWithTagFilter(PostDiscoveryFilter filter) {
    execute([selectPackage(SimpleTagSpec.getPackage().name)], [filter])
  }
}
