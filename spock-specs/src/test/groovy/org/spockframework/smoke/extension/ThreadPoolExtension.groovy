package org.spockframework.smoke.extension;
import spock.lang.Specification
import spock.lang.ThreadPool

/**
 * @author Bayo Erinle
 */
class ThreadPoolExtension extends Specification {

    @ThreadPool
    def "execute with 1 thread"(){
        setup:
        def alist = []
        100.times { alist << $it }

        expect:
        alist.size() == 100
    }

    @ThreadPool(5)
    def "execute with 5 threads"(){
        //should print 'hello world' 5 times, once for each thread
        setup: println "hello world!"
    }

}