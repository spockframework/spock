package spock.util.injectmock
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import spock.lang.Specification

class InjectMocksInterceptor extends AbstractMethodInterceptor {
    private final FieldInfo fieldInfo

    InjectMocksInterceptor(FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo
    }

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) {
        final specInstance = getSpec(invocation)
		def spyOrMocksFields = findAllDeclaredFieldsWithAnnotation(specInstance, Spy, Mock)
		def spyOrMocks = createFieldInstances(specInstance, spyOrMocksFields)
        // default constructor injection
        def instantiatedInjectMocks = fieldInfo.type.newInstance()
        specInstance[fieldInfo.name] = instantiatedInjectMocks
        injectTestDoublesIntoObjectUnderTest(spyOrMocks, instantiatedInjectMocks)
        invocation.proceed()
    }

    protected final Specification getSpec( IMethodInvocation invocation ) {
        ( Specification ) invocation.target.with { delegate instanceof Specification ? delegate : invocation.sharedInstance }
    }

    private static def injectTestDoublesIntoObjectUnderTest(ArrayList spyOrMocks, instantiatedInjectMocks) {
        spyOrMocks.each { spyOrMock ->
            instantiatedInjectMocks.metaPropertyValues.each { metaPropertyValue ->
                if (metaPropertyValue.type != Class && metaPropertyValue.type.isAssignableFrom(spyOrMock.class)) {
                    instantiatedInjectMocks[metaPropertyValue.name] = spyOrMock
                }
            }

        }
    }

    private static def createFieldInstances(Object specInstance, List spyOrMocsFields) {
		def spyOrMocks = []
		spyOrMocsFields.each {
			if (it.declaredAnnotations.any { annotation ->
				annotation.annotationType() == Spy
			}){
				specInstance[it.name] = specInstance.SpyImpl(it.name, it.type)
			}
			else {
				specInstance[it.name] = specInstance.MockImpl(it.name, it.type)
			}
			spyOrMocks << specInstance[it.name]
		}
		spyOrMocks
	}

    private static def findAllDeclaredFieldsWithAnnotation(obj, Class... annotClasses) {
        obj.getClass().declaredFields.findAll { field->
            annotClasses.any { annotClass ->
                annotClass in field.declaredAnnotations*.annotationType()
            }
        }
    }
}
