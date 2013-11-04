package spock.util.injectmock
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FieldInfo

class InjectMocksExtension extends AbstractAnnotationDrivenExtension<InjectMocks> {
    @Override
    @SuppressWarnings([ 'UnnecessaryGetter', 'GroovyGetterCallCanBePropertyAccess' ])
    void visitFieldAnnotation ( InjectMocks annotation, FieldInfo field ) {
        final interceptor = new InjectMocksInterceptor( field )
        field.parent.topSpec.setupMethods.each {
            it.addInterceptor( interceptor )
        }
        if(field.parent.topSpec.setupMethods.isEmpty()){
            field.parent.topSpec.features.each {it.addInterceptor(interceptor)}
        }
    }
}
