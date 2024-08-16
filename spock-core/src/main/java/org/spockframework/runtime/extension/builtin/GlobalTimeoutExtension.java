package org.spockframework.runtime.extension.builtin;

import spock.lang.Timeout;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.Beta;
import org.spockframework.util.Nullable;

/**
 * Applies a timeout to every feature and fixture depending on the configuration.
 *
 * @author Leonard Brünings
 * @since 2.4
 */
@Beta
public class GlobalTimeoutExtension implements IGlobalExtension {
  private final TimeoutConfiguration timeoutConfiguration;
  private @Nullable TimeoutInterceptor timeoutInterceptor;

  public GlobalTimeoutExtension(TimeoutConfiguration timeoutConfiguration) {
    // TimeoutConfiguration is mutable and will be configured after the extension is created,
    // so we need to store a reference to it and delay until the extension is started to create the interceptor.
    this.timeoutConfiguration = timeoutConfiguration;
  }

  @Override
  public void start() {
    timeoutInterceptor = timeoutConfiguration.globalTimeout == null
        ? null
        : new TimeoutInterceptor(timeoutConfiguration.globalTimeout, timeoutConfiguration);
  }

  @Override
  public void visitSpec(SpecInfo spec) {
    if (timeoutInterceptor == null
        || spec.getReflection().isAnnotationPresent(Timeout.class)) {
      return;
    }

    Stream<MethodInfo> features = spec.getAllFeatures().stream()
        .map(FeatureInfo::getFeatureMethod);
    Stream<MethodInfo> fixtures = timeoutConfiguration.applyGlobalTimeoutToFixtures
        ? StreamSupport.stream(spec.getAllFixtureMethods().spliterator(), false)
        : Stream.empty();

    Stream.concat(features, fixtures)
        .filter(method -> !method.getReflection().isAnnotationPresent(Timeout.class))
        .forEach(method -> method.addInterceptor(timeoutInterceptor));
  }
}
