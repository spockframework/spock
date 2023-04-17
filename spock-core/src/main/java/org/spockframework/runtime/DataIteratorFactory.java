package org.spockframework.runtime;

import org.spockframework.runtime.model.*;

import java.util.*;

import org.jetbrains.annotations.NotNull;

import static java.util.Collections.emptyIterator;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class DataIteratorFactory {

  protected final IRunSupervisor supervisor;

  public DataIteratorFactory(IRunSupervisor supervisor) {
    this.supervisor = supervisor;
  }

  public IDataIterator createFeatureDataIterator(SpockExecutionContext context) {
    if (context.getCurrentFeature().getDataProcessorMethod() == null) {
      return new SingleEmptyIterationDataIterator();
    }
    return new DataProcessorIterator(supervisor, context, new FeatureDataProviderIterator(supervisor, context));
  }

  private abstract static class BaseDataIterator implements IDataIterator {
    protected final IRunSupervisor supervisor;
    protected final SpockExecutionContext context;

    public BaseDataIterator(IRunSupervisor supervisor, SpockExecutionContext context) {
      this.supervisor = supervisor;
      this.context = context;
    }

    protected Object invokeRaw(Object target, MethodInfo method, Object... arguments) {
      try {
        return method.invoke(target, arguments);
      } catch (Throwable throwable) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(method, throwable));
        return null;
      }
    }

    protected int estimateNumIterations(Object dataProvider) {
      if (context.getErrorInfoCollector().hasErrors()) {
        return -1;
      }

      // unbelievably, DefaultGroovyMethods provides a size() method for Iterators,
      // although it is of course destructive (i.e. it exhausts the Iterator)
      if (dataProvider instanceof Iterator) {
        return -1;
      }

      Object rawSize = GroovyRuntimeUtil.invokeMethodQuietly(dataProvider, "size");
      if (!(rawSize instanceof Number)) {
        return -1;
      }

      int size = ((Number) rawSize).intValue();
      if (size < 0) {
        return -1;
      }

      return size;
    }

    protected int estimateNumIterations(Object[] dataProviders) {
      if (context.getErrorInfoCollector().hasErrors()) {
        return -1;
      }
      if (dataProviders.length == 0) {
        return 1;
      }

      int result = Integer.MAX_VALUE;
      for (Object prov : dataProviders) {
        int size = estimateNumIterations(prov);
        if (size < 0 || size >= result) {
          continue;
        }
        result = size;
      }

      return result == Integer.MAX_VALUE ? -1 : result;
    }

    protected boolean haveNext(Iterator<?>[] iterators, List<DataProviderInfo> dataProviderInfos) {
      boolean result = true;

      for (int i = 0; i < iterators.length; i++)
        try {
          boolean hasNext = iterators[i].hasNext();
          if (i == 0) {
            result = hasNext;
          } else if (result != hasNext) {
            DataProviderInfo provider = dataProviderInfos.get(i);
            supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(provider.getDataProviderMethod(),
              createDifferentNumberOfDataValuesException(provider, hasNext)));
            return false;
          }

        } catch (Throwable t) {
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(dataProviderInfos.get(i).getDataProviderMethod(), t));
          return false;
        }

      return result;
    }

    protected Iterator<?> createIterator(Object dataProvider, DataProviderInfo dataProviderInfo) {
      if (context.getErrorInfoCollector().hasErrors()) {
        return null;
      }

      try {
        Iterator<?> iter = GroovyRuntimeUtil.asIterator(dataProvider);
        if (iter == null) {
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(dataProviderInfo.getDataProviderMethod(),
            new SpockExecutionException("Data provider's iterator() method returned null")));
          return null;
        }
        return iter;
      } catch (Throwable t) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(dataProviderInfo.getDataProviderMethod(), t));
        return null;
      }
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

  /**
   * A fallback iterator that returns a single empty iteration.
   * <p>
   * This is used when a feature has no data provider method and iteration behavior is forcefully enabled.
   *
   * @since 2.3
   */
  private static class SingleEmptyIterationDataIterator implements IDataIterator {

    public static final List<Object[]> DEFAULT_PARAMS = singletonList(new Object[0]);
    private final Iterator<Object[]> delegate;

    public SingleEmptyIterationDataIterator() {
      delegate = DEFAULT_PARAMS.iterator();
    }

    @Override
    public boolean hasNext() {
      return delegate.hasNext();
    }

    @Override
    public Object[] next() {
      return delegate.next();
    }

    @Override
    public int getEstimatedNumIterations() {
      return 1;
    }

    @Override
    public List<String> getDataVariableNames() {
      return Collections.emptyList();
    }

    @Override
    public void close() throws Exception {
    }
  }

  private static class DataProcessorIterator extends BaseDataIterator {
    private final IDataIterator delegate;
    private final List<String> dataVariableNames;

    private DataProcessorIterator(IRunSupervisor supervisor, SpockExecutionContext context, IDataIterator delegate) {
      super(supervisor, context);
      this.delegate = delegate;
      this.dataVariableNames = readDataVariableNames();
    }

    @NotNull
    private List<String> readDataVariableNames() {
      return Collections.unmodifiableList(Arrays.asList(
        context.getCurrentFeature().getDataProcessorMethod().getAnnotation(DataProcessorMetadata.class)
          .dataVariables()));
    }

    @Override
    public int getEstimatedNumIterations() {
      return delegate.getEstimatedNumIterations();
    }

    @Override
    public List<String> getDataVariableNames() {
      return dataVariableNames;
    }

    @Override
    public void close() throws Exception {
      delegate.close();
    }

    @Override
    public boolean hasNext() {
      return delegate.hasNext();
    }

    @Override
    public Object[] next() {
      Object[] next = delegate.next();

      // delegate.next() will return null if an error occurred
      if (next == null) {
        return null;
      }

      try {
        return (Object[]) invokeRaw(context.getSharedInstance(), context.getCurrentFeature().getDataProcessorMethod(), next);
      } catch (Throwable t) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProcessorMethod(), t));
        return null;
      }
    }
  }

  private static class FeatureDataProviderIterator extends BaseDataIterator {
    private final Object[] dataProviders;
    private final IDataIterator[] dataProviderIterators;
    private final int estimatedNumIterations;
    private final List<String> dataVariableNames;
    private boolean firstIteration = true;

    public FeatureDataProviderIterator(IRunSupervisor supervisor, SpockExecutionContext context) {
      super(supervisor, context);
      // order is important as they rely on each other
      dataVariableNames = dataVariableNames();
      dataProviders = createDataProviders();
      estimatedNumIterations = estimateNumIterations(dataProviders);
      dataProviderIterators = createDataProviderIterators();
    }

    @Override
    public void close() {
      if (dataProviders != null) {
        for (Object provider : dataProviders) {
          GroovyRuntimeUtil.invokeMethodQuietly(provider, "close");
        }
      }

      if (dataProviderIterators != null) {
        for (IDataIterator dataProviderIterator : dataProviderIterators) {
          GroovyRuntimeUtil.invokeMethodQuietly(dataProviderIterator, "close");
        }
      }
    }

    @Override
    public boolean hasNext() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return false;
      }
      if (dataProviderIterators.length == 0 && !firstIteration) {
        // no iterators => no data providers => only derived parameterizations => limit to one iteration
        return false;
      }

      return haveNext(dataProviderIterators, context.getCurrentFeature().getDataProviders());
    }

    @Override
    public Object[] next() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return null;
      }
      firstIteration = false;

      // advances iterators and computes args
      Object[] next = new Object[dataProviders.length];
      for (int i = 0; i < dataProviders.length; ) {
        try {
          Object[] nextValues = dataProviderIterators[i].next();
          if (nextValues == null) {
            return null;
          }
          System.arraycopy(nextValues, 0, next, i, nextValues.length);
          i += nextValues.length;
        } catch (Throwable t) {
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProviders().get(i).getDataProviderMethod(), t));
          return null;
        }
      }
      return next;
    }

    @Override
    public int getEstimatedNumIterations() {
      return estimatedNumIterations;
    }

    @Override
    public List<String> getDataVariableNames() {
      return dataVariableNames;
    }

    private Object[] createDataProviders() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return null;
      }

      List<DataProviderInfo> dataProviderInfos = context.getCurrentFeature().getDataProviders();
      if (dataProviderInfos.isEmpty()) {
        return new Object[0];
      }

      Object[] dataProviders = new Object[dataProviderInfos.size()];

      for (int i = 0, size = dataProviderInfos.size(); i < size; i++) {
        DataProviderInfo dataProviderInfo = dataProviderInfos.get(i);
        MethodInfo method = dataProviderInfo.getDataProviderMethod();

        Object provider = invokeRaw(
          context.getCurrentInstance(), method,
          getPreviousDataTableProviders(dataVariableNames, dataProviders, dataProviderInfo));

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

    private IDataIterator[] createDataProviderIterators() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return null;
      }

      MethodInfo dataVariableMultiplicationsMethod = context.getCurrentFeature().getDataVariableMultiplicationsMethod();
      Iterator<DataVariableMultiplication> dataVariableMultiplications;
      DataVariableMultiplication nextDataVariableMultiplication;
      if (dataVariableMultiplicationsMethod != null) {
        try {
          dataVariableMultiplications = Arrays.stream(((DataVariableMultiplication[]) invokeRaw(null, dataVariableMultiplicationsMethod))).iterator();
          nextDataVariableMultiplication = dataVariableMultiplications.next();
        } catch (Throwable t) {
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(dataVariableMultiplicationsMethod, t));
          return null;
        }
      } else {
        dataVariableMultiplications = emptyIterator();
        nextDataVariableMultiplication = null;
      }

      List<IDataIterator> dataIterators = new ArrayList<>(dataProviders.length);
      for (int i = 0; i < dataProviders.length; i++) {
        String nextDataVariableName = dataVariableNames.get(i);
        if ((nextDataVariableMultiplication != null)
          && (nextDataVariableMultiplication.getDataVariables()[0].equals(nextDataVariableName))) {

          // a cross multiplication starts
          dataIterators.add(createDataProviderMultiplier(nextDataVariableMultiplication, i));
          // skip processed variables
          i += nextDataVariableMultiplication.getDataVariables().length - 1;
          // wait for next cross multiplication
          nextDataVariableMultiplication = dataVariableMultiplications.hasNext() ? dataVariableMultiplications.next() : null;
        } else {
          // not a cross multiplication, just use a data provider iterator
          dataIterators.add(new DataProviderIterator(
            supervisor, context, nextDataVariableName,
            context.getCurrentFeature().getDataProviders().get(i), dataProviders[i]));
        }
      }
      return dataIterators.toArray(new IDataIterator[0]);
    }

    private DataProviderMultiplier createDataProviderMultiplier(DataVariableMultiplication dataVariableMultiplication, int i) {
      DataVariableMultiplicationFactor multiplier = dataVariableMultiplication.getMultiplier();
      DataVariableMultiplicationFactor multiplicand = dataVariableMultiplication.getMultiplicand();

      int multiplierDataVariablesLength = multiplier.getDataVariables().length;
      int multiplicandDataVariablesLength = multiplicand.getDataVariables().length;

      if (multiplier instanceof DataVariableMultiplication) {
        DataProviderMultiplier multiplierProvider = createDataProviderMultiplier((DataVariableMultiplication) multiplier, i);
        Object[] multiplicandProviders = new Object[multiplicandDataVariablesLength];
        List<DataProviderInfo> multiplicandProviderInfos = new ArrayList<>();

        List<DataProviderInfo> dataProviderInfos = context.getCurrentFeature().getDataProviders();
        int j = multiplierDataVariablesLength;
        int j2 = multiplierDataVariablesLength + multiplicandDataVariablesLength;
        int k = 0;
        for (; j < j2; j++, k++) {
          multiplicandProviderInfos.add(dataProviderInfos.get(i + j));
          multiplicandProviders[k] = dataProviders[i + j];
        }

        return new DataProviderMultiplier(supervisor, context,
          Arrays.asList(dataVariableMultiplication.getDataVariables()),
          multiplicandProviderInfos, multiplierProvider, multiplicandProviders);
      } else {
        Object[] multiplierProviders = new Object[multiplierDataVariablesLength];
        Object[] multiplicandProviders = new Object[multiplicandDataVariablesLength];
        List<DataProviderInfo> multiplierProviderInfos = new ArrayList<>();
        List<DataProviderInfo> multiplicandProviderInfos = new ArrayList<>();

        List<DataProviderInfo> dataProviderInfos = context.getCurrentFeature().getDataProviders();
        int j = 0;
        int j2 = multiplierDataVariablesLength;
        for (; j < j2; j++) {
          multiplierProviderInfos.add(dataProviderInfos.get(i + j));
          multiplierProviders[j] = dataProviders[i + j];
        }
        int k = 0;
        for (j2 += multiplicandDataVariablesLength; j < j2; j++, k++) {
          multiplicandProviderInfos.add(dataProviderInfos.get(i + j));
          multiplicandProviders[k] = dataProviders[i + j];
        }

        return new DataProviderMultiplier(supervisor, context,
          Arrays.asList(dataVariableMultiplication.getDataVariables()),
          multiplierProviderInfos, multiplicandProviderInfos, multiplierProviders, multiplicandProviders);
      }
    }

    @NotNull
    private List<String> dataVariableNames() {
      return context.getCurrentFeature().getDataProviders()
        .stream()
        .map(DataProviderInfo::getDataVariables)
        .flatMap(List::stream)
        .collect(toList());
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
  }

  private static class DataProviderIterator extends BaseDataIterator {
    private final List<String> dataVariableNames;
    private final DataProviderInfo providerInfo;
    private final Object provider;
    private final Iterator<?> iterator;
    private final int estimatedNumIterations;

    public DataProviderIterator(IRunSupervisor supervisor, SpockExecutionContext context, String dataVariableName,
                                DataProviderInfo providerInfo, Object provider) {
      super(supervisor, context);
      this.dataVariableNames = singletonList(Objects.requireNonNull(dataVariableName));
      estimatedNumIterations = estimateNumIterations(Objects.requireNonNull(provider));
      this.providerInfo = Objects.requireNonNull(providerInfo);
      this.provider = provider;
      iterator = createIterator(provider, providerInfo);
    }

    @Override
    public void close() throws Exception {
      GroovyRuntimeUtil.invokeMethodQuietly(provider, "close");
    }

    @Override
    public boolean hasNext() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return false;
      }

      try {
        return iterator.hasNext();
      } catch (Throwable t) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(providerInfo.getDataProviderMethod(), t));
        return false;
      }
    }

    @Override
    public Object[] next() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return null;
      }

      try {
        return new Object[]{iterator.next()};
      } catch (Throwable t) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(providerInfo.getDataProviderMethod(), t));
        return null;
      }
    }

    @Override
    public int getEstimatedNumIterations() {
      return estimatedNumIterations;
    }

    @Override
    public List<String> getDataVariableNames() {
      return dataVariableNames;
    }
  }

  private static class DataProviderMultiplier extends BaseDataIterator {
    private final List<String> dataVariableNames;
    private final List<DataProviderInfo> multiplierProviderInfos;
    private final List<DataProviderInfo> multiplicandProviderInfos;
    private final DataProviderMultiplier multiplierProvider;
    private final Object[] multiplierProviders;
    private final DataProviderMultiplier multiplicandProvider;
    private final Object[] multiplicandProviders;
    private final Iterator<?>[] multiplierIterators;
    private final Iterator<?>[] multiplicandIterators;
    private boolean factorsSwapped = false;
    private Object[] currentMultiplierValues = null;
    private boolean collectMultiplicandValues = true;
    private final List<List<Object>> collectedMultiplicandValues;
    private final int estimatedNumIterations;

    public DataProviderMultiplier(IRunSupervisor supervisor, SpockExecutionContext context, List<String> dataVariableNames,
                                  List<DataProviderInfo> multiplierProviderInfos, List<DataProviderInfo> multiplicandProviderInfos,
                                  Object[] multiplierProviders, Object[] multiplicandProviders) {
      super(supervisor, context);
      this.dataVariableNames = Objects.requireNonNull(dataVariableNames);
      multiplierProvider = null;
      multiplicandProvider = null;

      int estimatedMultiplierIterations = estimateNumIterations(Objects.requireNonNull(multiplierProviders));
      int estimatedMultiplicandIterations = estimateNumIterations(Objects.requireNonNull(multiplicandProviders));
      estimatedNumIterations = (estimatedMultiplierIterations == -1) || (estimatedMultiplicandIterations == -1)
        ? -1
        : estimatedMultiplierIterations * estimatedMultiplicandIterations;

      if ((estimatedMultiplierIterations != -1)
        && ((estimatedMultiplicandIterations == -1) || (estimatedMultiplicandIterations > estimatedMultiplierIterations))) {
        // multiplier is not unknown and multiplicand is unknown or larger,
        // swap factors so that potentially fewer values need to be remembered in memory
        factorsSwapped = true;
        this.multiplierProviderInfos = multiplicandProviderInfos;
        this.multiplicandProviderInfos = multiplierProviderInfos;
        this.multiplierProviders = multiplicandProviders;
        this.multiplicandProviders = multiplierProviders;
      } else {
        this.multiplierProviderInfos = multiplierProviderInfos;
        this.multiplicandProviderInfos = multiplicandProviderInfos;
        this.multiplierProviders = multiplierProviders;
        this.multiplicandProviders = multiplicandProviders;
      }

      collectedMultiplicandValues = new ArrayList<>(this.multiplicandProviders.length);
      for (int i = 0; i < this.multiplicandProviders.length; i++) {
        collectedMultiplicandValues.add(new ArrayList<>());
      }
      multiplierIterators = createIterators(this.multiplierProviders, this.multiplierProviderInfos);
      multiplicandIterators = createIterators(this.multiplicandProviders, this.multiplicandProviderInfos);
    }

    public DataProviderMultiplier(IRunSupervisor supervisor, SpockExecutionContext context, List<String> dataVariableNames,
                                  List<DataProviderInfo> multiplicandProviderInfos,
                                  DataProviderMultiplier multiplierProvider, Object[] multiplicandProviders) {
      super(supervisor, context);
      this.dataVariableNames = Objects.requireNonNull(dataVariableNames);

      int estimatedMultiplierIterations = Objects.requireNonNull(multiplierProvider).getEstimatedNumIterations();
      int estimatedMultiplicandIterations = estimateNumIterations(Objects.requireNonNull(multiplicandProviders));
      estimatedNumIterations = (estimatedMultiplierIterations == -1) || (estimatedMultiplicandIterations == -1)
        ? -1
        : estimatedMultiplierIterations * estimatedMultiplicandIterations;

      if ((estimatedMultiplierIterations != -1)
        && ((estimatedMultiplicandIterations == -1) || (estimatedMultiplicandIterations > estimatedMultiplierIterations))) {
        // multiplier is not unknown and multiplicand is unknown or larger,
        // swap factors so that potentially fewer values need to be remembered in memory
        factorsSwapped = true;
        this.multiplierProviderInfos = multiplicandProviderInfos;
        this.multiplicandProviderInfos = null;
        this.multiplierProvider = null;
        this.multiplierProviders = multiplicandProviders;
        multiplicandProvider = multiplierProvider;
        this.multiplicandProviders = null;
        collectedMultiplicandValues = singletonList(new ArrayList<>());
        multiplierIterators = createIterators(this.multiplierProviders, this.multiplierProviderInfos);
        multiplicandIterators = new Iterator[]{multiplierProvider};
      } else {
        this.multiplierProviderInfos = null;
        this.multiplicandProviderInfos = multiplicandProviderInfos;
        this.multiplierProvider = multiplierProvider;
        this.multiplierProviders = null;
        multiplicandProvider = null;
        this.multiplicandProviders = multiplicandProviders;
        collectedMultiplicandValues = new ArrayList<>(multiplicandProviders.length);
        for (int i = 0; i < multiplicandProviders.length; i++) {
          collectedMultiplicandValues.add(new ArrayList<>());
        }
        multiplierIterators = new Iterator[]{multiplierProvider};
        multiplicandIterators = createIterators(this.multiplicandProviders, this.multiplicandProviderInfos);
      }
    }

    @Override
    public void close() throws Exception {
      if (multiplierProvider != null) {
        GroovyRuntimeUtil.invokeMethodQuietly(multiplierProvider, "close");
      }
      if (multiplierProviders != null) {
        for (Object provider : multiplierProviders) {
          GroovyRuntimeUtil.invokeMethodQuietly(provider, "close");
        }
      }
      if (multiplicandProvider != null) {
        GroovyRuntimeUtil.invokeMethodQuietly(multiplicandProvider, "close");
      }
      if (multiplicandProviders != null) {
        for (Object provider : multiplicandProviders) {
          GroovyRuntimeUtil.invokeMethodQuietly(provider, "close");
        }
      }
    }

    @Override
    public boolean hasNext() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return false;
      }
      return haveNext(multiplicandIterators, multiplicandProviderInfos)
        || haveNext(multiplierIterators, multiplierProviderInfos);
    }

    @Override
    public Object[] next() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return null;
      }

      // multiplicand is exhausted, start next round
      if (!haveNext(multiplicandIterators, multiplicandProviderInfos)) {
        // use the next set of multiplier values
        currentMultiplierValues = extractNextValues(multiplierIterators, multiplierProviderInfos);
        if (currentMultiplierValues == null) {
          return null;
        }
        if (multiplierProvider != null) {
          currentMultiplierValues = ((Object[]) currentMultiplierValues[0]);
        }

        // stop collecting multiplicand values if still collecting
        collectMultiplicandValues = false;

        // restart multiplicand values
        for (int i = 0; i < multiplicandIterators.length; i++) {
          try {
            multiplicandIterators[i] = collectedMultiplicandValues.get(i).iterator();
          } catch (Throwable t) {
            supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(multiplicandProviderInfos.get(i).getDataProviderMethod(), t));
            return null;
          }
        }
      }

      // first value overall, get the first set of multiplier values
      if (currentMultiplierValues == null) {
        currentMultiplierValues = extractNextValues(multiplierIterators, multiplierProviderInfos);
        if (currentMultiplierValues == null) {
          return null;
        }
        if (multiplierProvider != null) {
          currentMultiplierValues = ((Object[]) currentMultiplierValues[0]);
        }
      }

      // get the next set of multiplicand values
      Object[] nextMultiplicandValues = extractNextValues(multiplicandIterators, multiplicandProviderInfos);
      if (nextMultiplicandValues == null) {
        return null;
      }
      if (collectMultiplicandValues) {
        for (int i = 0; i < nextMultiplicandValues.length; i++) {
          collectedMultiplicandValues.get(i).add(nextMultiplicandValues[i]);
        }
      }
      if (multiplicandProvider != null) {
        nextMultiplicandValues = ((Object[]) nextMultiplicandValues[0]);
      }

      // prepare result
      Object[] nextMultiplierValues;
      if (factorsSwapped) {
        nextMultiplierValues = nextMultiplicandValues;
        nextMultiplicandValues = currentMultiplierValues;
      } else {
        nextMultiplierValues = currentMultiplierValues;
      }
      Object[] next = new Object[nextMultiplierValues.length + nextMultiplicandValues.length];
      System.arraycopy(nextMultiplierValues, 0, next, 0, nextMultiplierValues.length);
      System.arraycopy(nextMultiplicandValues, 0, next, nextMultiplierValues.length, nextMultiplicandValues.length);
      return next;
    }

    @Override
    public int getEstimatedNumIterations() {
      return estimatedNumIterations;
    }

    @Override
    public List<String> getDataVariableNames() {
      return dataVariableNames;
    }

    private Iterator<?>[] createIterators(Object[] dataProviders, List<DataProviderInfo> dataProviderInfos) {
      if (context.getErrorInfoCollector().hasErrors()) {
        return null;
      }

      Iterator<?>[] iterators = new Iterator<?>[dataProviders.length];
      for (int i = 0; i < dataProviders.length; i++) {
        Iterator<?> iter = createIterator(dataProviders[i], dataProviderInfos.get(i));
        if (iter == null) {
          return null;
        }
        iterators[i] = iter;
      }

      return iterators;
    }

    protected Object[] extractNextValues(Iterator<?>[] iterators, List<DataProviderInfo> providerInfos) {
      Object[] result = new Object[iterators.length];
      for (int i = 0; i < iterators.length; i++) {
        try {
          result[i] = iterators[i].next();
        } catch (Throwable t) {
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(providerInfos.get(i).getDataProviderMethod(), t));
          return null;
        }
      }
      return result;
    }
  }
}
