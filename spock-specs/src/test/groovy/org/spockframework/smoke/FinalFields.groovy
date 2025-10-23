package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException

import spock.lang.Shared
import spock.lang.Specification

class FinalFields extends Specification {
    @Shared
    final String finalShared = 'final shared field'

    @Shared
    String normalShared = 'normal shared field'

    final String finalField = 'final field'

    String normalField = 'normal field'

    def "can modify normal fields"() {
        expect:
        normalField == 'normal field'

        when:
        normalField = 'new value'

        then:
        normalField == 'new value'
    }

    def "can modify normal shared fields"() {
        expect:
        normalShared == 'normal shared field'

        when:
        normalShared = 'new value'

        then:
        normalShared == 'new value'
    }

    def "cannot modify final fields"() {
        expect:
        finalField == 'final field'

        when:
        finalField = 'new value'

        then:
        ReadOnlyPropertyException ex = thrown()
        ex.message ==~ /\QCannot set read\E-?\Qonly property: finalField for class: org.spockframework.smoke.FinalFields\E/
        finalField == 'final field'
    }

    def "cannot modify final shared fields"() {
        expect:
        finalShared == 'final shared field'

        when:
        finalShared = 'new value'

        then:
        ReadOnlyPropertyException ex = thrown()
        ex.message ==~ /\QCannot set read\E-?\Qonly property: finalShared for class: org.spockframework.smoke.FinalFields\E/
        finalShared == 'final shared field'
    }
}

class FinalFieldsInitializer extends EmbeddedSpecification {
    @Shared
    String normalShared // no initializer necessary

    String normalField // no initializer necessary

    def "normal fields don't need an initializer"() {
        expect:
        normalShared == null
        normalField == null
    }

    def "final fields need an initializer"() {
        when:
        compiler.compileSpecBody('''
          final String finalField
        ''')

        then:
        InvalidSpecCompileException e = thrown()
        e.message == "Final field 'finalField' is not initialized. @ line 1, column 56."
    }

    def "final shared fields need an initializer"() {
        when:
        compiler.compileSpecBody('''
          @Shared
          final String finalShared
        ''')

        then:
        InvalidSpecCompileException e = thrown()
        e.message == "@Shared final field 'finalShared' is not initialized. @ line 1, column 56."
    }
}
