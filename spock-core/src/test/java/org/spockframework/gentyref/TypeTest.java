package org.spockframework.gentyref;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test the implementation of our types: <ul>
 * <li>test that they're equal to the java implementation and vice-versa</li>
 * <li>test toString</li>
 * </ul>
 * 
 * @author Wouter Coekaerts <wouter@coekaerts.be>
 */
public class TypeTest extends TestCase {

	private void assertTypesEqual(TypeToken<?> expectedToken, Type type, String toString) {
		Type expected = expectedToken.getType();
		assertEquals(expected, type);
		assertEquals(type, expected);
		assertEquals(toString, type.toString());
//		if (! toString.equals(expected.toString()))
//			System.err.println("WARN: jdk gives different toString for " + toString + ":\n   " + expected.toString());
	}
	
	public void testParameterizedType() {
		assertTypesEqual(new TypeToken<List<String>>(){},
					new ParameterizedTypeImpl(List.class, new Type[]{String.class}, null),
					"java.util.List<java.lang.String>");
		assertTypesEqual(new TypeToken<Map<List<String>, Integer>>(){},
				new ParameterizedTypeImpl(Map.class, new Type[] {
						new ParameterizedTypeImpl(List.class, new Type[]{String.class}, null),
						Integer.class
				}, null),
				"java.util.Map<java.util.List<java.lang.String>, java.lang.Integer>");
	}
	
	class InnerWithParam<T> {
		class InnerInnerWithParam<U> {}
	}
	
	public void testNested() {
		assertTypesEqual(new TypeToken<TypeTest.InnerWithParam<String>>(){},
				new ParameterizedTypeImpl(TypeTest.InnerWithParam.class, new Type[]{String.class}, TypeTest.class),
				"org.spockframework.gentyref.TypeTest.InnerWithParam<java.lang.String>");
		assertTypesEqual(new TypeToken<TypeTest.InnerWithParam<String>.InnerInnerWithParam<Integer>>(){},
				new ParameterizedTypeImpl(TypeTest.InnerWithParam.InnerInnerWithParam.class, new Type[]{Integer.class},
						new ParameterizedTypeImpl(TypeTest.InnerWithParam.class, new Type[]{String.class}, TypeTest.class)
				),
				"org.spockframework.gentyref.TypeTest.InnerWithParam<java.lang.String>.InnerInnerWithParam<java.lang.Integer>");
	}
	
	public void testUnboundWildcard() {
		assertTypesEqual(new TypeToken<List<?>>(){},
				new ParameterizedTypeImpl(List.class, new Type[]{new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{})}, null),
				"java.util.List<?>");
		// upperbound on variable doesn't matter for the wildcard
		assertTypesEqual(new TypeToken<EnumSet<?>>(){},
				new ParameterizedTypeImpl(EnumSet.class, new Type[]{new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{})}, null),
				"java.util.EnumSet<?>");
	}
	
	public void testSuperWildcard() {
		assertTypesEqual(new TypeToken<List<? super Number>>(){},
				new ParameterizedTypeImpl(List.class, new Type[]{new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{Number.class})}, null),
				"java.util.List<? super java.lang.Number>");
	}
	
	public void testExtendsWildcard() {
		assertTypesEqual(new TypeToken<List<? extends Number>>(){},
				new ParameterizedTypeImpl(List.class, new Type[]{new WildcardTypeImpl(new Type[]{Number.class}, new Type[]{})}, null),
				"java.util.List<? extends java.lang.Number>");
	}
	
	public void testGenericArray()  {
		assertTypesEqual(new TypeToken<List<?>[]>(){},
				GenericArrayTypeImpl.createArrayType(new ParameterizedTypeImpl(List.class, new Type[]{new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{})}, null)),
				"java.util.List<?>[]");
	}
	
	public void testArray() {
		assertEquals("java.lang.String[]", GenericTypeReflector.getTypeName(String[].class));
	}
	
	public void testCapture() {
		// Note: there's no jdk counterpart for CaptureType
		assertEquals("java.util.List<capture of ?>", GenericTypeReflector.capture(new TypeToken<List<?>>(){}.getType()).toString());
	}
}
