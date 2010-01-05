/**
 * 
 */
package org.spockframework.gentyref;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapping between type variables and actual parameters.
 * 
 * @author Wouter Coekaerts <wouter@coekaerts.be>
 */
class VarMap {
	private final Map<TypeVariable<?>, Type> map = new HashMap<TypeVariable<?>, Type>();
	
	/**
	 * Creates an empty VarMap
	 */
	VarMap() {
	}
	
	void add(TypeVariable<?> variable, Type value) {
		map.put(variable, value);
	}
	
	void addAll(TypeVariable<?>[] variables, Type[] values) {
		assert variables.length == values.length;
		for (int i = 0; i < variables.length; i++) {
			map.put(variables[i], values[i]);
		}
	}
	
	VarMap(TypeVariable<?>[] variables, Type[] values) {
		addAll(variables, values);
	}
	
	Type map(Type type) {
		if (type instanceof Class) {
			return type;
		} else if (type instanceof TypeVariable) {
			assert map.containsKey(type);
			return map.get(type);
		} else if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			return new ParameterizedTypeImpl((Class<?>)pType.getRawType(), map(pType.getActualTypeArguments()), pType.getOwnerType() == null ? pType.getOwnerType() : map(pType.getOwnerType()));
		} else if (type instanceof WildcardType) {
			WildcardType wType = (WildcardType) type;
			return new WildcardTypeImpl(map(wType.getUpperBounds()), map(wType.getLowerBounds()));
		} else if (type instanceof GenericArrayType) {
			return GenericArrayTypeImpl.createArrayType(map(((GenericArrayType)type).getGenericComponentType()));
		} else {
			throw new RuntimeException("not implemented: mapping " + type.getClass() + " (" + type + ")");
		}
	}
	
	Type[] map(Type[] types) {
		Type[] result = new Type[types.length];
		for (int i = 0; i < types.length; i++) {
			result[i] = map(types[i]);
		}
		return result;
	}
}