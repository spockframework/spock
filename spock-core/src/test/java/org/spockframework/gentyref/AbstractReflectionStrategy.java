package org.spockframework.gentyref;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ReflectionStrategy for implementations that provide a getExactSuperType implementation
 * 
 * @author Wouter Coekaerts <wouter@coekaerts.be>
 */
public abstract class AbstractReflectionStrategy implements ReflectionStrategy {
	
	public final void testExactSuperclass(Type expectedSuperclass, Type type) {
		// test if the supertype of the given class is as expected
		GenericTypeReflectorTest.assertEquals(expectedSuperclass, getExactSuperType(type, GenericTypeReflectorTest.getClassType(expectedSuperclass)));
	}
	
	public final void testInexactSupertype(Type superType, Type subType) {
		if (superType instanceof ParameterizedType || superType instanceof Class) {
			// test if it's not exact
			GenericTypeReflectorTest.assertFalse(superType.equals(getExactSuperType(subType, GenericTypeReflectorTest.getClassType(superType))));
		}
	}
	
	protected abstract Type getExactSuperType(Type type, Class<?> searchClass);
}
