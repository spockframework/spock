package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import org.spockframework.util.InternalSpockError;
import spock.lang.Specification;

import org.junit.platform.engine.*;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.unmodifiableList;

public class SpockExecutionContext implements EngineExecutionContext, Cloneable {
  private EngineExecutionListener engineExecutionListener;

  private RunContext runContext;

  private PlatformParameterizedSpecRunner runner;

  private SpecInfo spec;

  private FeatureInfo currentFeature;

  private IterationInfo currentIteration;

  private Specification sharedInstance;

  private Specification currentInstance;

  private UniqueId parentId;

  private ErrorInfoCollector errorInfoCollector;

  private final AtomicBoolean hadSuccess = new AtomicBoolean();

  private final List<Throwable> abortions = new CopyOnWriteArrayList<>();

  private final List<Throwable> failures = new CopyOnWriteArrayList<>();

  public SpockExecutionContext(EngineExecutionListener engineExecutionListener) {
    this.engineExecutionListener = engineExecutionListener;
  }

  private SpockExecutionContext setRunContext(RunContext runContext) {
    this.runContext = runContext;
    return this;
  }

  private SpockExecutionContext setRunner(PlatformParameterizedSpecRunner runner) {
    this.runner = runner;
    return this;
  }

  private SpockExecutionContext setSpec(SpecInfo spec) {
    this.spec = spec;
    return this;
  }

  private SpockExecutionContext setCurrentFeature(FeatureInfo currentFeature) {
    if (this.currentFeature != currentFeature) {
      hadSuccess.set(false);
      abortions.clear();
      failures.clear();
    }
    this.currentFeature = currentFeature;
    return this;
  }

  private SpockExecutionContext setCurrentIteration(IterationInfo currentIteration) {
    this.currentIteration = currentIteration;
    return this;
  }

  private SpockExecutionContext setSharedInstance(Specification sharedInstance) {
    this.sharedInstance = sharedInstance;
    return this;
  }

  private SpockExecutionContext setCurrentInstance(Specification currentInstance) {
    this.currentInstance = currentInstance;
    return this;
  }

  public SpockExecutionContext setParentId(UniqueId parentId) {
    this.parentId = parentId;
    return this;
  }

  public SpockExecutionContext setErrorInfoCollector(ErrorInfoCollector errorInfoCollector) {
    this.errorInfoCollector = errorInfoCollector;
    return this;
  }

  public void setHadSuccess() {
    hadSuccess.set(true);
  }

  public void addAbortion(Throwable abortion) {
    abortions.add(abortion);
  }

  public void addFailure(Throwable failure) {
    failures.add(failure);
  }

  @Override
  protected SpockExecutionContext clone() {
    try {
      return (SpockExecutionContext)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalSpockError("Could not clone context", e);
    }
  }

  public PlatformParameterizedSpecRunner getRunner() {
    return runner;
  }

  public RunContext getRunContext() {
    return runContext;
  }

  public SpockExecutionContext withRunContext(RunContext runContext) {
    return clone().setRunContext(runContext);
  }

  public SpockExecutionContext withRunner(PlatformParameterizedSpecRunner runner) {
    return clone().setRunner(runner);
  }

  public SpockExecutionContext withSharedInstance(Specification sharedInstance) {
    return clone().setSharedInstance(sharedInstance);
  }

  public SpockExecutionContext withCurrentInstance(Specification currentInstance) {
    return clone().setCurrentInstance(currentInstance);
  }

  SpockExecutionContext withSpec(SpecInfo spec) {
    return clone().setSpec(spec);
  }

  public SpockExecutionContext withCurrentFeature(FeatureInfo feature) {
    return clone().setCurrentFeature(feature);
  }

  public SpockExecutionContext withCurrentIteration(IterationInfo iteration) {
    return clone().setCurrentFeature(iteration.getFeature()).setCurrentIteration(iteration);
  }

  public SpockExecutionContext withErrorInfoCollector(ErrorInfoCollector errorInfoCollector) {
    return clone().setErrorInfoCollector(errorInfoCollector);
  }

  public SpockExecutionContext withParentId(UniqueId uniqueId) {
    return clone().setParentId(uniqueId);
  }

  public Specification getSharedInstance() {
    return sharedInstance;
  }

  public Specification getCurrentInstance() {
    return currentInstance;
  }

  public SpecInfo getSpec() {
    return spec;
  }

  public FeatureInfo getCurrentFeature() {
    return currentFeature;
  }

  public IterationInfo getCurrentIteration() {
    return currentIteration;
  }

  public UniqueId getParentId() {
    return parentId;
  }

  public ErrorInfoCollector getErrorInfoCollector() {
    return errorInfoCollector;
  }

  public boolean hadSuccess() {
    return hadSuccess.get();
  }

  public List<Throwable> getAbortions() {
    return unmodifiableList(abortions);
  }

  public List<Throwable> getFailures() {
    return unmodifiableList(failures);
  }
}
