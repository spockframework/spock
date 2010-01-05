package org.spockframework.gentyref;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * see http://code.google.com/p/gentyref/wiki/CaptureType
 * 
 * @author Wouter Coekaerts <wouter@coekaerts.be>
 */
public class CaptureSamplesTest extends TestCase {
		class Foo<T> {
			public List<? extends Number> listWildcard;
			public List<T> listT;
		}
	
	public void testFoo() throws NoSuchFieldException {
		Foo<? extends Number> foo = new Foo<Integer>();
		foo.listWildcard = new ArrayList<Long>();
		//foo.listT = new ArrayList<Long>(); // does not compile
		
		Type fooWildcard = new TypeToken<Foo<? extends Number>>(){}.getType();
		
		Type listWildcardFieldType = GenericTypeReflector.getExactFieldType(Foo.class.getField("listWildcard"), fooWildcard);
		Type listTFieldType = GenericTypeReflector.getExactFieldType(Foo.class.getField("listT"), fooWildcard);
		
		assertEquals(new TypeToken<List<? extends Number>>(){}.getType(), listWildcardFieldType);
		assertTrue(GenericTypeReflector.isSuperType(listWildcardFieldType, new TypeToken<ArrayList<Long>>(){}.getType()));
		assertFalse(GenericTypeReflector.isSuperType(listTFieldType, new TypeToken<ArrayList<Long>>(){}.getType()));
	}
}
