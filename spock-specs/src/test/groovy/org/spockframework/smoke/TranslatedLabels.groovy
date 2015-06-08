package org.spockframework.smoke

import spock.lang.Specification

class TranslatedLabels extends Specification {
    def doit() {
        förväntas:
        1 + 2 == 3
    }
}
