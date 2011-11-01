package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import spock.lang.ThreadPool;

/**
 * Implementation of @ThreadPool
 *
 * @author Bayo Erinle
 */
class ThreadPoolInterceptor implements IMethodInterceptor {

    //privately held instance of threadPool
    private final ThreadPool threadPool;

    ThreadPoolInterceptor(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public void intercept(final IMethodInvocation invocation) throws Throwable {

        final Throwable[] exception = new Throwable[1];

        int numOfThreads = threadPool.value();

        //IDEA: Use blocking barrier to make sure all threads
        //get kicked off at the same time
        for(int i=0; i<numOfThreads; i++){
            new Thread(){
                public void run(){
                    try{
                        invocation.proceed();
                    } catch(Throwable t){
                        exception[0] = t;
                    }
                }

            }.start();
        }

    }
}
