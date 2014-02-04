package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import spock.lang.ThreadPool;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.MethodInfo;

/**
 * @author Bayo Erinle
 */
public class ThreadPoolExtension extends AbstractAnnotationDrivenExtension<ThreadPool> {

    @Override
    public void visitFeatureAnnotation(ThreadPool threadPool, FeatureInfo feature) {
        feature.getFeatureMethod().addInterceptor(new ThreadPoolInterceptor(threadPool));
    }

    @Override
    public void visitFixtureAnnotation(ThreadPool threadPool, MethodInfo fixtureMethod) {
        fixtureMethod.addInterceptor(new ThreadPoolInterceptor(threadPool));
    }
}

