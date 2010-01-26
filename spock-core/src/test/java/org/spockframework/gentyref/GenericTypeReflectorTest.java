package org.spockframework.gentyref;

import java.lang.reflect.*;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;

/**
 * Test for reflection done in GenericTypeReflector.
 * This class inherits most of its tests from the superclass, and adds a few more.
 */
public class GenericTypeReflectorTest extends AbstractGenericsReflectorTest {
	public GenericTypeReflectorTest() {
		super(new GenTyRefReflectionStrategy());
	}
	
	public void testGetTypeParameter() {
		class StringList extends ArrayList<String> {}
		assertEquals(String.class, GenericTypeReflector.getTypeParameter(StringList.class, Collection.class.getTypeParameters()[0]));
	}
	
	public void testGetUpperBoundClassAndInterfaces() {
		class Foo<A extends Number & Iterable<A>, B extends A> {}
		TypeVariable<?> a = Foo.class.getTypeParameters()[0];
		TypeVariable<?> b = Foo.class.getTypeParameters()[1];
		assertEquals(Arrays.<Class<?>>asList(Number.class, Iterable.class),
			GenericTypeReflector.getUpperBoundClassAndInterfaces(a));
		assertEquals(Arrays.<Class<?>>asList(Number.class, Iterable.class),
				GenericTypeReflector.getUpperBoundClassAndInterfaces(b));
	}

  public void testGetExactParameterTypes() throws Exception {
    Method method = List.class.getMethod("set", int.class, Object.class);
    assertArrayEquals(new Type[] {int.class, String.class},
        GenericTypeReflector.getExactParameterTypes(method, LIST_OF_STRING.getType()));
  }

  public void testGetExactReturnType() throws Exception {
    Method method = List.class.getMethod("set", int.class, Object.class);
    assertEquals(String.class, GenericTypeReflector.getExactReturnType(method, LIST_OF_STRING.getType()));
  }
}
