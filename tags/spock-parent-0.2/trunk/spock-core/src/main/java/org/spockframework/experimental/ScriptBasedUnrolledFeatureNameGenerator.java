package org.spockframework.experimental;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;

import org.spockframework.runtime.model.FeatureInfo;
import org.codehaus.groovy.control.CompilationFailedException;
import spock.lang.*;
import groovy.lang.*;

/**
 * @author Peter Niederwieser
 */
public class ScriptBasedUnrolledFeatureNameGenerator {
  private static final Pattern PLACE_HOLDER = Pattern.compile("(.?)#");

  private final FeatureInfo feature;
  private final Unroll unroll;
  private final Script nameGenerator;
  private int iterationCount;

  public ScriptBasedUnrolledFeatureNameGenerator(GroovyShell shell, FeatureInfo feature, Unroll unroll) {
    this.feature = feature;
    this.unroll = unroll;
    nameGenerator = createNameGenerator(shell, unroll);
  }

  public String nameFor(Object[] args) {
    if (nameGenerator == null) return unroll.value();

    iterationCount++;
    nameGenerator.setBinding(createBindingFrom(args));

    try {
      return nameGenerator.run().toString();
    } catch (Throwable t) {
      // at the moment we can't throw an exception from here, because
      // this would terminate the speck execution 
      return unroll.value();
    }
  }

  private Script createNameGenerator(GroovyShell shell, final Unroll unroll) {
    String nameTemplate = convertToGString(unroll.value());

    try {
      // Note: the following GroovyShell invocation is quite slow
      // compiling the template during speck compilation might be faster,
      // e.g. because the compiler classes are already loaded and initialized
      return shell.parse("return \"" + nameTemplate + "\"");
    } catch (CompilationFailedException e) {
      // at the moment we can't throw an exception from here, because
      // this would terminate the speck execution
      return null;
    }
  }

  private String convertToGString(String template) {
    Matcher matcher = PLACE_HOLDER.matcher(template);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String group = matcher.group(1);
      boolean escaped = "\\".equals(group);
      matcher.appendReplacement(result, escaped ? "#" : group + "\\$");
    }
    matcher.appendTail(result);
    return result.toString();
  }

  private Binding createBindingFrom(Object[] args) {
    Map<String, Object> variables = new HashMap<String, Object>();
    for (int i = 0; i < feature.getParameterNames().size(); i++) {
      variables.put(feature.getParameterNames().get(i), args[i]);
    }
    variables.put("featureName", feature.getName());
    variables.put("iterationCount", iterationCount);
    return new Binding(variables);
  }
}