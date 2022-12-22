package org.spockframework.runtime;

import org.spockframework.runtime.model.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.spockframework.util.Nullable;
import spock.config.RunnerConfiguration;

import static java.util.Collections.emptyIterator;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.spockframework.runtime.GroovyRuntimeUtil.closeQuietly;

public class DataIteratorFactory {
  public static final int UNKNOWN_ITERATIONS = -1;

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
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(method, throwable, getErrorContext()));
        return null;
      }
    }

    protected IErrorContext getErrorContext() {
      return ErrorContext.from(context.getCurrentInstance().getSpecificationContext());
    }

    protected int estimateNumIterations(@Nullable Object dataProvider) {
      if (context.getErrorInfoCollector().hasErrors()) {
        return UNKNOWN_ITERATIONS;
      }

      // an IDataIterator probably already has the estimated size
      // or knows better how to calculate it
      if (dataProvider instanceof IDataIterator) {
        return ((IDataIterator) dataProvider).getEstimatedNumIterations();
      }

      // unbelievably, DefaultGroovyMethods provides a size() method for Iterators,
      // although it is of course destructive (i.e. it exhausts the Iterator)
      if (dataProvider instanceof Iterator) {
        return UNKNOWN_ITERATIONS;
      }

      Object rawSize = GroovyRuntimeUtil.invokeMethodQuietly(dataProvider, "size");
      if (!(rawSize instanceof Number)) {
        return UNKNOWN_ITERATIONS;
      }

      int size = ((Number) rawSize).intValue();
      if (size < 0) {
        return UNKNOWN_ITERATIONS;
      }

      return size;
    }

    protected int estimateNumIterations(Object[] dataProviders) {
      if (context.getErrorInfoCollector().hasErrors()) {
        return UNKNOWN_ITERATIONS;
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

      return result == Integer.MAX_VALUE ? UNKNOWN_ITERATIONS : result;
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
              createDifferentNumberOfDataValuesException(provider, hasNext), getErrorContext()));
            return false;
          }

        } catch (Throwable t) {
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(dataProviderInfos.get(i).getDataProviderMethod(), t, getErrorContext()));
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
            new SpockExecutionException("Data provider's iterator() method returned null"), getErrorContext()));
          return null;
        }
        return iter;
      } catch (Throwable t) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(dataProviderInfo.getDataProviderMethod(), t, getErrorContext()));
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
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProcessorMethod(), t, getErrorContext()));
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
    private boolean logFilteredIterations = true;
    private IStackTraceFilter stackTraceFilter;

    public FeatureDataProviderIterator(IRunSupervisor supervisor, SpockExecutionContext context) {
      super(supervisor, context);
      // order is important as they rely on each other
      dataVariableNames = dataVariableNames();
      dataProviders = createDataProviders();
      dataProviderIterators = createDataProviderIterators();
      estimatedNumIterations = estimateNumIterations(dataProviderIterators);

      RunnerConfiguration runnerConfiguration = context
        .getRunContext()
        .getConfiguration(RunnerConfiguration.class);
      logFilteredIterations = runnerConfiguration.logFilteredIterations;
      if (logFilteredIterations) {
        stackTraceFilter = runnerConfiguration.filterStackTrace ? new StackTraceFilter(context.getSpec()) : new DummyStackTraceFilter();
      }
    }

    @Override
    public void close() {
      closeQuietly(dataProviders);
      closeQuietly((Object[]) dataProviderIterators);
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

      while (true) {
        // advances iterators and computes args
        Object[] next = new Object[dataProviders.length];
        for (int i = 0; i < dataProviders.length; ) {
          try {
            // if the filter block excluded an iteration
            // this might be called after the last iteration
            // so just return null if no further data is available
            // to just cause the iteration to be skipped
            if (!dataProviderIterators[i].hasNext()) {
              return null;
            }
            Object[] nextValues = dataProviderIterators[i].next();
            if (nextValues == null) {
              return null;
            }
            System.arraycopy(nextValues, 0, next, i, nextValues.length);
            i += nextValues.length;
          } catch (Throwable t) {
            supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProviders().get(i).getDataProviderMethod(), t, getErrorContext()));
            return null;
          }
        }

        MethodInfo filterMethod = context.getCurrentFeature().getFilterMethod();

        if (filterMethod == null) {
          return next;
        }

        try {
          // do not use invokeRaw here, as that would report Assertion Error to the supervisor
          filterMethod.invoke(null, next);
          return next;
        } catch (AssertionError ae) {
          if (logFilteredIterations) {
            StringJoiner stringJoiner = new StringJoiner(", ", "Filtered iteration [", "]:\n");
            for (int i = 0; i < dataVariableNames.size(); i++) {
              stringJoiner.add(dataVariableNames.get(i) + ": " + next[i]);
            }
            StringWriter sw = new StringWriter();
            sw.write(stringJoiner.toString());
            stackTraceFilter.filter(ae);
            try (PrintWriter pw = new PrintWriter(sw)) {
              ae.printStackTrace(pw);
            }
            System.err.println(sw);
          }
          // filter block does not like these values, try next ones if available
        } catch (Throwable t) {
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(filterMethod, t, getErrorContext()));
          return null;
        }
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
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(method, error, getErrorContext()));
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
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(dataVariableMultiplicationsMethod, t, getErrorContext()));
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

    /**
     * Creates a multiplier that is backed by data providers and on-the-fly multiplies them as they are processed.
     *
     * @param dataVariableMultiplication the multiplication for which to create the multiplier
     * @param i the index of the first data variable for the given multiplication
     * @return the data provider multiplier
     */
    private DataProviderMultiplier createDataProviderMultiplier(DataVariableMultiplication dataVariableMultiplication, int i) {
      DataVariableMultiplicationFactor multiplier = dataVariableMultiplication.getMultiplier();
      DataVariableMultiplicationFactor multiplicand = dataVariableMultiplication.getMultiplicand();

      int multiplierDataVariablesLength = multiplier.getDataVariables().length;
      int multiplicandDataVariablesLength = multiplicand.getDataVariables().length;

      if (multiplier instanceof DataVariableMultiplication) {
        // recursively dive into the multiplication depth-first
        // if you combined a with b with c with d, the multiplication is represented as
        // (((a * b) * c) * d), where each character can represent multiple data providers
        // this path handles all multiplications except (a * b) which is handled in the else path
        // through this depth-first recursion ultimately (a * b) is handled first,
        // then (ab * c), then (abc * d).
        //
        // here we first build the data provider multiplier for the multiplier
        // then we collect the data provider infos and data providers for the multiplicand
        // and then create the data provider multiplier for them
        DataProviderMultiplier multiplierProvider = createDataProviderMultiplier((DataVariableMultiplication) multiplier, i);
        List<DataProviderInfo> multiplicandProviderInfos = new ArrayList<>();
        Object[] multiplicandProviders = new Object[multiplicandDataVariablesLength];

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
        // this path handles the innermost multiplication (a * b)
        //
        // it collects the data provider infos and data providers for a and b
        // and then creates a data provider multiplier for them
        List<DataProviderInfo> multiplierProviderInfos = new ArrayList<>();
        List<DataProviderInfo> multiplicandProviderInfos = new ArrayList<>();
        Object[] multiplierProviders = new Object[multiplierDataVariablesLength];
        Object[] multiplicandProviders = new Object[multiplicandDataVariablesLength];

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
      closeQuietly(provider);
    }

    @Override
    public boolean hasNext() {
      if (context.getErrorInfoCollector().hasErrors()) {
        return false;
      }

      try {
        return iterator.hasNext();
      } catch (Throwable t) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(providerInfo.getDataProviderMethod(), t, getErrorContext()));
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
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(providerInfo.getDataProviderMethod(), t, getErrorContext()));
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
    /**
     * The names of the data variables for which values are produced
     * by this data provider multiplier.
     */
    private final List<String> dataVariableNames;

    /**
     * The provider infos for the multiplier data providers.
     * These are only used for constructing meaningful errors.
     * If {@link #multiplierProviders} is set, this will be set too,
     * if it is {@code null}, this will be {@code null} too.
     */
    private final List<DataProviderInfo> multiplierProviderInfos;

    /**
     * The provider infos for the multiplicand data providers.
     * These are only used for constructing meaningful errors.
     * If {@link #multiplicandProviders} is set, this will be set too,
     * if it is {@code null}, this will be {@code null} too.
     */
    private final List<DataProviderInfo> multiplicandProviderInfos;

    /**
     * The data provider multiplier that is used as multiplier
     * in this multiplication, if it represents in an
     * {@code ((a * b) * c)} multiplication the {@code (ab * c)} part
     * or otherwise {@code null}.
     * Outside the constructor it is only used as condition
     * and for the final cleanup,
     */
    private final DataProviderMultiplier multiplierProvider;

    /**
     * The data providers that are used as multiplier
     * in this multiplication, if it represents in an
     * {@code (a * (b * c))} multiplication the {@code (a * bc)}
     * or the {@code (b * c)} part or otherwise {@code null}.
     * Outside the constructor it is only used for the final cleanup,
     */
    private final Object[] multiplierProviders;

    /**
     * The data provider multiplier that is used as multiplicand
     * in this multiplication, if it represents in an
     * {@code (a * (b * c))} multiplication the {@code (a * bc)} part
     * or otherwise {@code null}.
     * Outside the constructor it is only used as condition
     * and for the final cleanup,
     */
    private final DataProviderMultiplier multiplicandProvider;

    /**
     * The data providers that are used as multiplicand
     * in this multiplication, if it represents in an
     * {@code ((a * b) * c)} multiplication the {@code (ab * b)}
     * or the {@code (a * b)} part or otherwise {@code null}.
     * Outside the constructor it is only used for the final cleanup,
     */
    private final Object[] multiplicandProviders;

    /**
     * The iterators that were built from the final multiplier providers,
     * which are used for providing the actual values.
     */
    private final Iterator<?>[] multiplierIterators;

    /**
     * During the first set of multiplier values, i.e. the first value of the {@link #multiplierIterators},
     * this contains the iterators that were built from the final multiplicand providers,
     * which are used for providing the actual values. These values are recorded and then replied for each
     * multiplier value by filling this array with iterators over the recorded values to achieve the multiplication.
     * To minimize the necessary caching and decrease the chance to get an {@code OutOfMemoryError},
     * the constructors try to determine whether the multiplier or multiplicand has fewer values, or whether the
     * size of one of them can be determined while the other can not and swaps around the factors if necessary to
     * have the smaller or at least the defined number in the multiplicand position.
     */
    private final Iterator<?>[] multiplicandIterators;

    /**
     * A flag that remembers whether the factors were swapped for optimization, so that the result values can be
     * swapped back for expected, consistent, and reproducible results.
     */
    private boolean factorsSwapped = false;

    /**
     * The current set of multiplier values that is combined with each set of multiplicand values,
     * before the next multiplier value is calculated.
     */
    private Object[] currentMultiplierValues = null;

    /**
     * A flag that is {@code true} during the processing of the first set of multiplier values
     * during which the multiplier values get recorded for subsequent replay for the remaining
     * sets of multiplier values.
     */
    private boolean collectMultiplicandValues = true;

    /**
     * The complete list of sets of multiplicand values, which were recorded while processing the first
     * set of multiplier values that is then replayed for each remaining set of multiplier values.
     */
    private final List<List<Object>> collectedMultiplicandValues;

    /**
     * The estimated amount of iterations coming out of this multiplication. If either of the factors size is
     * unknown, the size of the multiplication is unknown, otherwise it is the actual product of the sizes.
     */
    private final int estimatedNumIterations;

    /**
     * Creates a new data provider multiplier that handles two sets of plain data providers as factors.
     * If the sizes of the providers in at least one of the sets can be estimated,
     * the factors of the multiplication are possibly swapped around to store as few values as possible
     * in memory for the multiplication.
     *
     * @param supervisor the supervisor that is notified in case of errors
     * @param context the execution context that for example contains the error collector
     * @param dataVariableNames the names of the data variables for which values are produced by this multiplier
     * @param multiplierProviderInfos the provider infos for the multiplier for constructing meaningful errors
     * @param multiplicandProviderInfos the provider infos for the multiplicand for constructing meaningful errors
     * @param multiplierProviders the actual providers for the sets of multiplier values
     * @param multiplicandProviders the actual providers for the sets of multiplicand values
     */
    public DataProviderMultiplier(IRunSupervisor supervisor, SpockExecutionContext context, List<String> dataVariableNames,
                                  List<DataProviderInfo> multiplierProviderInfos, List<DataProviderInfo> multiplicandProviderInfos,
                                  Object[] multiplierProviders, Object[] multiplicandProviders) {
      super(supervisor, context);
      this.dataVariableNames = Objects.requireNonNull(dataVariableNames);
      multiplierProvider = null;
      multiplicandProvider = null;

      int estimatedMultiplierIterations = estimateNumIterations(Objects.requireNonNull(multiplierProviders));
      int estimatedMultiplicandIterations = estimateNumIterations(Objects.requireNonNull(multiplicandProviders));
      estimatedNumIterations =
        (estimatedMultiplierIterations == UNKNOWN_ITERATIONS) || (estimatedMultiplicandIterations == UNKNOWN_ITERATIONS)
          ? UNKNOWN_ITERATIONS
          : estimatedMultiplierIterations * estimatedMultiplicandIterations;

      if ((estimatedMultiplierIterations != UNKNOWN_ITERATIONS)
        && ((estimatedMultiplicandIterations == UNKNOWN_ITERATIONS) || (estimatedMultiplicandIterations > estimatedMultiplierIterations))) {
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

      collectedMultiplicandValues = createMultiplicandStorage();
      multiplierIterators = createIterators(this.multiplierProviders, this.multiplierProviderInfos);
      multiplicandIterators = createIterators(this.multiplicandProviders, this.multiplicandProviderInfos);
    }

    /**
     * Creates a new data provider multiplier that handles one data provider multiplier
     * and a set of plain data providers as factors. If the sizes of the providers in at least
     * one of these can be estimated, the factors of the multiplication are possibly swapped around
     * to store as few values as possible in memory for the multiplication.
     *
     * @param supervisor the supervisor that is notified in case of errors
     * @param context the execution context that for example contains the error collector
     * @param dataVariableNames the names of the data variables for which values are produced by this multiplier
     * @param multiplicandProviderInfos the provider infos for the multiplicand for constructing meaningful errors
     * @param multiplierProvider the data provider multiplier that produces the multiplier values
     * @param multiplicandProviders the actual providers for the sets of multiplicand values
     */
    public DataProviderMultiplier(IRunSupervisor supervisor, SpockExecutionContext context, List<String> dataVariableNames,
                                  List<DataProviderInfo> multiplicandProviderInfos,
                                  DataProviderMultiplier multiplierProvider, Object[] multiplicandProviders) {
      super(supervisor, context);
      this.dataVariableNames = Objects.requireNonNull(dataVariableNames);

      int estimatedMultiplierIterations = Objects.requireNonNull(multiplierProvider).getEstimatedNumIterations();
      int estimatedMultiplicandIterations = estimateNumIterations(Objects.requireNonNull(multiplicandProviders));
      estimatedNumIterations =
        (estimatedMultiplierIterations == UNKNOWN_ITERATIONS) || (estimatedMultiplicandIterations == UNKNOWN_ITERATIONS)
          ? UNKNOWN_ITERATIONS
          : estimatedMultiplierIterations * estimatedMultiplicandIterations;

      if ((estimatedMultiplierIterations != UNKNOWN_ITERATIONS)
        && ((estimatedMultiplicandIterations == UNKNOWN_ITERATIONS) || (estimatedMultiplicandIterations > estimatedMultiplierIterations))) {
        // multiplier is not unknown and multiplicand is unknown or larger,
        // swap factors so that potentially fewer values need to be remembered in memory
        factorsSwapped = true;
        multiplierProviderInfos = multiplicandProviderInfos;
        this.multiplicandProviderInfos = null;
        this.multiplierProvider = null;
        multiplierProviders = multiplicandProviders;
        multiplicandProvider = multiplierProvider;
        this.multiplicandProviders = null;
        collectedMultiplicandValues = singletonList(new ArrayList<>());
        multiplierIterators = createIterators(multiplierProviders, multiplierProviderInfos);
        multiplicandIterators = new Iterator[]{multiplierProvider};
      } else {
        multiplierProviderInfos = null;
        this.multiplicandProviderInfos = multiplicandProviderInfos;
        this.multiplierProvider = multiplierProvider;
        multiplierProviders = null;
        multiplicandProvider = null;
        this.multiplicandProviders = multiplicandProviders;
        collectedMultiplicandValues = createMultiplicandStorage();
        multiplierIterators = new Iterator[]{multiplierProvider};
        multiplicandIterators = createIterators(this.multiplicandProviders, this.multiplicandProviderInfos);
      }
    }

    private List<List<Object>> createMultiplicandStorage() {
      List<List<Object>> result = new ArrayList<>(this.multiplicandProviders.length);
      for (int i = 0; i < this.multiplicandProviders.length; i++) {
        result.add(new ArrayList<>());
      }
      return result;
    }

    @Override
    public void close() throws Exception {
      closeQuietly(multiplierProvider, multiplicandProvider);
      closeQuietly(multiplierProviders);
      closeQuietly(multiplicandProviders);
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
            supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(multiplicandProviderInfos.get(i).getDataProviderMethod(), t, getErrorContext()));
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
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(providerInfos.get(i).getDataProviderMethod(), t, getErrorContext()));
          return null;
        }
      }
      return result;
    }
  }
}
