package org.spockframework.gentyref;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Strategy to do reflection.
 * This can be our own implementation, but also something else if we want to reuse our tests to test another implementation.
 * We do this for gson for example.
 */
public interface ReflectionStrategy {
	boolean isSupertype(Type supertype, Type subtype);
	void testInexactSupertype(Type supertype, Type subtype);
	void testExactSuperclass(Type expectedSuperclass, Type type);
	Type getReturnType(Type type, Method m);
	Type getFieldType(Type type, Field f);
}