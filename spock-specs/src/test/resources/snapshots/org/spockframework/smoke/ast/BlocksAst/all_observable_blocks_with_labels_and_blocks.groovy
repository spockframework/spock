package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = ['given', 'and given']), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = ['expect', 'and expect']), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.WHEN, texts = ['when', 'and when']), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.THEN, texts = ['then', 'and then']), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.THEN, texts = ['then2', 'and then2']), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.CLEANUP, texts = ['cleanup', 'and cleanup']), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.WHERE, texts = ['where', 'combine']), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.FILTER, texts = ['only one execution'])], parameterNames = [])
public void $spock_feature_0_0() {
    java.lang.Throwable $spock_feature_throwable
    try {
        org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
        org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
        org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
        org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
        org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 2)
        org.spockframework.runtime.SpockRuntime.callBlockExited(this, 2)
        org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 3)
        org.spockframework.runtime.SpockRuntime.callBlockExited(this, 3)
        org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 4)
        org.spockframework.runtime.SpockRuntime.callBlockExited(this, 4)
    }
    catch (java.lang.Throwable $spock_tmp_throwable) {
        $spock_feature_throwable = $spock_tmp_throwable
        throw $spock_tmp_throwable
    }
    finally {
        org.spockframework.runtime.model.BlockInfo $spock_failedBlock = null
        try {
            if ( $spock_feature_throwable != null) {
                $spock_failedBlock = this.getSpecificationContext().getCurrentBlock()
            }
            org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 5)
            org.spockframework.runtime.SpockRuntime.callBlockExited(this, 5)
        }
        catch (java.lang.Throwable $spock_tmp_throwable) {
            if ( $spock_feature_throwable != null) {
                $spock_feature_throwable.addSuppressed($spock_tmp_throwable)
            } else {
                throw $spock_tmp_throwable
            }
        }
        finally {
            if ( $spock_feature_throwable != null) {
                ((org.spockframework.runtime.SpecificationContext) this.getSpecificationContext()).setCurrentBlock($spock_failedBlock)
            }
        }
    }
    this.getSpecificationContext().getMockController().leaveScope()
}
/*--------- end::snapshot[] ---------*/
  }
}
