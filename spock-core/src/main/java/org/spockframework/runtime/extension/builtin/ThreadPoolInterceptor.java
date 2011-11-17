package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import spock.lang.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

        List<Thread> threads = new ArrayList<Thread>();

        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        final CyclicBarrier barrier = new CyclicBarrier(numOfThreads);
        
        for(int i=0; i<numOfThreads; i++){
            Thread t = new Thread() {
                public void run() {
                    try {
                        barrier.await();
                        invocation.proceed();
                    } catch (Throwable t) {
                        exception[0] = t;
                    }
                }

            };
            threads.add(t);
        }

        

        for (Thread thread : threads) {
            executor.execute(thread);
        }

        executor.shutdown();
        //FIXME: find a way to get rid of this
        executor.awaitTermination(1, TimeUnit.MINUTES);

    }
}
