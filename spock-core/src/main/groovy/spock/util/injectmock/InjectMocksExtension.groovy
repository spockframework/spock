package spock.util.injectmock

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FieldInfo

class InjectMocksExtension extends AbstractAnnotationDrivenExtension<Subject> {

    @Override
    @SuppressWarnings(['UnnecessaryGetter', 'GroovyGetterCallCanBePropertyAccess'])
    void visitFieldAnnotation(Subject annotation, FieldInfo field) {
        InjectMocksInterceptor interceptor = new InjectMocksInterceptor(field)
        field.parent.topSpec.addSetupInterceptor(interceptor)
    }

}
