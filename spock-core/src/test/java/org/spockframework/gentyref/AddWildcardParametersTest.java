package org.spockframework.gentyref;

import java.util.Map;

import junit.framework.TestCase;

public class AddWildcardParametersTest extends TestCase {
	public void testNoParams() {
		assertEquals(String.class, GenericTypeReflector.addWildcardParameters(String.class));
	}
	
	public void testMap() {
		assertEquals(new TypeToken<Map<?,?>>(){}.getType(), GenericTypeReflector.addWildcardParameters(Map.class));
	}
	
	public void testInnerAndOuter() {
		class Outer<T> {
			class Inner<S> {
			}
		}
		assertEquals(new TypeToken<Outer<?>.Inner<?>>(){}.getType(), GenericTypeReflector.addWildcardParameters(Outer.Inner.class));
	}
	
	public void testInner() {
		class Outer {
			class Inner<S> {
			}
		}
		assertEquals(new TypeToken<Outer.Inner<?>>(){}.getType(), GenericTypeReflector.addWildcardParameters(Outer.Inner.class));
	}
	
	public void testOuter() {
		class Outer<T> {
			class Inner {
			}
		}
		assertEquals(new TypeToken<Outer<?>.Inner>(){}.getType(), GenericTypeReflector.addWildcardParameters(Outer.Inner.class));
	}
	
	public void testNoParamArray() {
		assertEquals(String[].class, GenericTypeReflector.addWildcardParameters(String[].class));
		assertEquals(String[][].class, GenericTypeReflector.addWildcardParameters(String[][].class));
	}
	
	public void testGenericArray() {
		assertEquals(new TypeToken<Map<?,?>[]>(){}.getType(), GenericTypeReflector.addWildcardParameters(Map[].class));
		assertEquals(new TypeToken<Map<?,?>[][]>(){}.getType(), GenericTypeReflector.addWildcardParameters(Map[][].class));
	}
	
}
