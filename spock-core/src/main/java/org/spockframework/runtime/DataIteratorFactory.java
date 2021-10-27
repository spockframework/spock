package org.spockframework.runtime;

import static java.util.stream.Collectors.toList;

import org.spockframework.runtime.model.*;

import java.util.*;

public class DataIteratorFactory {

  protected final IRunSupervisor supervisor;

  public DataIteratorFactory(IRunSupervisor supervisor) {
    this.supervisor = supervisor;
  }

  public DataIterator createFeatureDataIterator(SpockExecutionContext context) {
    return new FeatureDataProviderIterator(supervisor, context);
  }

  private static class FeatureDataProviderIterator implements DataIterator {
    private final IRunSupervisor supervisor;
    private final SpockExecutionContext context;
    private final Object[] dataProviders;
    private final Iterator<?>[] iterators;
    private final int estimatedNumIterations;
    private int iteration = 0;

    public FeatureDataProviderIterator(IRunSupervisor supervisor, SpockExecutionContext context) {
      this.supervisor = supervisor;
      this.context = context;
      // order is important as they rely on each other
      this.dataProviders = createDataProviders();
      this.iterators = createIterators();
      this.estimatedNumIterations = estimateNumIterations();
    }

    @Override
    public void close() {
      if (dataProviders == null) {
        return; // there was an error creating the providers
      }

      for (Object provider : dataProviders) {
        GroovyRuntimeUtil.invokeMethodQuietly(provider, "close");
      }
    }

    @Override
    public boolean hasNext() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return false;
      }
      if (iterators.length == 0 && iteration > 0) {
        // no iterators => no data providers => only derived parameterizations => limit to one iteration
        return false;
      }

      boolean haveNext = true;

      for (int i = 0; i < iterators.length; i++)
        try {
          boolean hasNext = iterators[i].hasNext();
          if (i == 0) {
            haveNext = hasNext;
          } else if (haveNext != hasNext) {
            DataProviderInfo provider = context.getCurrentFeature().getDataProviders().get(i);
            supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(provider.getDataProviderMethod(),
              createDifferentNumberOfDataValuesException(provider, hasNext)));
            return false;
          }

        } catch (Throwable t) {
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProviders().get(i).getDataProviderMethod(), t));
          return false;
        }

      return haveNext;
    }

    @Override
    public Object[] next() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return null;
      }
      iteration++;

      // advances iterators and computes args
      Object[] next = new Object[iterators.length];
      for (int i = 0; i < iterators.length; i++)
        try {
          next[i] = iterators[i].next();
        } catch (Throwable t) {
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProviders().get(i).getDataProviderMethod(), t));
          return null;
        }

      try {
        return (Object[])invokeRaw(context, context.getSharedInstance(), context.getCurrentFeature().getDataProcessorMethod(), next);
      } catch (Throwable t) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProcessorMethod(), t));
        return null;
      }
    }

    @Override
    public int getEstimatedNumIterations() {
      return estimatedNumIterations;
    }

    private int estimateNumIterations() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return -1;
      }
      if (dataProviders.length == 0) {
        return 1;
      }

      int result = Integer.MAX_VALUE;
      for (Object prov : dataProviders) {
        if (prov instanceof Iterator)
        // unbelievably, DGM provides a size() method for Iterators,
        // although it is of course destructive (i.e. it exhausts the Iterator)
        {
          continue;
        }

        Object rawSize = GroovyRuntimeUtil.invokeMethodQuietly(prov, "size");
        if (!(rawSize instanceof Number)) {
          continue;
        }

        int size = ((Number)rawSize).intValue();
        if (size < 0 || size >= result) {
          continue;
        }

        result = size;
      }

      return result == Integer.MAX_VALUE ? -1 : result;
    }

    private Object invokeRaw(SpockExecutionContext context, Object target, MethodInfo method, Object... arguments) {
      try {
        return method.invoke(target, arguments);
      } catch (Throwable throwable) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(method, throwable));
        return null;
      }
    }

    private Object[] createDataProviders() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return null;
      }

      List<DataProviderInfo> dataProviderInfos = context.getCurrentFeature().getDataProviders();
      if (dataProviderInfos.isEmpty()) {
        return new Object[0];
      }

      List<String> dataProviderVariables = dataProviderInfos
        .stream()
        .map(DataProviderInfo::getDataVariables)
        .flatMap(List::stream)
        .collect(toList());

      Object[] dataProviders = new Object[dataProviderInfos.size()];

      for (int i = 0, size = dataProviderInfos.size(); i < size; i++) {
        DataProviderInfo dataProviderInfo = dataProviderInfos.get(i);
        MethodInfo method = dataProviderInfo.getDataProviderMethod();

        Object provider = invokeRaw(
          context, context.getCurrentInstance(), method,
          getPreviousDataTableProviders(dataProviderVariables, dataProviders, dataProviderInfo));

        if (context.getErrorInfoCollector().hasErrors()) {
          if (provider != null) {
            dataProviders[i] = provider;
          }
          break;
        } else if (provider == null) {
          SpockExecutionException error = new SpockExecutionException("Data provider is null!");
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(method, error));
          break;
        }

        dataProviders[i] = provider;
      }

      return dataProviders;
    }

    private Object[] getPreviousDataTableProviders(List<String> dataProviderVariables, Object[] dataProviders,
                                                   DataProviderInfo dataProviderInfo) {
      List<Object> result = new ArrayList<>();
      previousDataTableVariablesLoop:
      for (String previousDataTableVariable : dataProviderInfo.getPreviousDataTableVariables()) {
        for (int i = 0, size = dataProviderVariables.size(); i < size; i++) {
          String dataProviderVariable = dataProviderVariables.get(i);
          if (previousDataTableVariable.equals(dataProviderVariable)) {
            result.add(dataProviders[i]);
            continue previousDataTableVariablesLoop;
          }
        }
        throw new IllegalStateException(String.format("Variable name not defined (%s not in %s)!",
          previousDataTableVariable, dataProviderVariables));
      }
      return result.toArray();
    }

    private Iterator<?>[] createIterators() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return null;
      }

      Iterator<?>[] iterators = new Iterator<?>[dataProviders.length];
      for (int i = 0; i < dataProviders.length; i++)
        try {
          Iterator<?> iter = GroovyRuntimeUtil.asIterator(dataProviders[i]);
          if (iter == null) {
            supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProviders().get(i).getDataProviderMethod(),
              new SpockExecutionException("Data provider's iterator() method returned null")));
            return null;
          }
          iterators[i] = iter;
        } catch (Throwable t) {
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProviders().get(i).getDataProviderMethod(), t));
          return null;
        }

      return iterators;
    }

    private SpockExecutionException createDifferentNumberOfDataValuesException(DataProviderInfo provider,
                                                                               boolean hasNext) {
      String msg = String.format("Data provider for variable '%s' has %s values than previous data provider(s)",
        provider.getDataVariables().get(0), hasNext ? "more" : "fewer");
      SpockExecutionException exception = new SpockExecutionException(msg);
      FeatureInfo feature = provider.getParent();
      SpecInfo spec = feature.getParent();
      StackTraceElement elem = new StackTraceElement(spec.getReflection().getName(),
        feature.getName(), spec.getFilename(), provider.getLine());
      exception.setStackTrace(new StackTraceElement[]{elem});
      return exception;
    }

  }
}
