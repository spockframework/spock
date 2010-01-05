package org.spockframework.gentyref;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

class GenTyRefReflectionStrategy extends AbstractReflectionStrategy {
	public boolean isSupertype(Type superType, Type subType) {
		return GenericTypeReflector.isSuperType(superType, subType);
	}

	protected Type getExactSuperType(Type type, Class<?> searchClass) {
		return GenericTypeReflector.getExactSuperType(type, searchClass);
	}

	public Type getReturnType(Type type, Method m) {
		return GenericTypeReflector.getExactReturnType(m, type);
	}

	public Type getFieldType(Type type, Field f) {
		return GenericTypeReflector.getExactFieldType(f, type);
	}
}