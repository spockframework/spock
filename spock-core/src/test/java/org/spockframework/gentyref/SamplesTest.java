package org.spockframework.gentyref;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

import junit.framework.TestCase;

/**
 * Simple samples of what gentyref does, in the form of tests.
 * See http://code.google.com/p/gentyref/wiki/ExampleUsage
 * 
 * @author Wouter Coekaerts <wouter@coekaerts.be>
 */
public class SamplesTest extends TestCase {
	interface Processor<T> {
		void process(T t);
	}
	
	class StringProcessor implements Processor<String> {
		public void process(String s) {
			System.out.println("processing " + s);
		}
	}
	
	class IntegerProcessor implements Processor<Integer> {
		public void process(Integer i) {
			System.out.println("processing " + i);
		}
	}
	
	/*
	 * Returns true if processorClass extends Processor<String>
	 */
	public boolean isStringProcessor(Class<? extends Processor<?>> processorClass) {
		// Use TypeToken to get an instanceof a specific Type
		Type type = new TypeToken<Processor<String>>(){}.getType();
		// Use GenericTypeReflector.isSuperType to check if a type is a supertype of another
		return GenericTypeReflector.isSuperType(type, processorClass);
	}
	
	public void testProsessor() {
		assertTrue(isStringProcessor(StringProcessor.class));
		assertFalse(isStringProcessor(IntegerProcessor.class));
	}
	
	abstract class Lister<T> {
		public List<T> list() {
			return null;
		}
	}
	
	class StringLister extends Lister<String> {
	}
	
	public void testFooBar() throws NoSuchMethodException {
		Method listMethod = StringLister.class.getMethod("list");
		
		// java returns List<T>
		Type returnType = listMethod.getGenericReturnType();
		assertTrue(returnType instanceof ParameterizedType);
		assertTrue(((ParameterizedType)returnType).getActualTypeArguments()[0] instanceof TypeVariable);
		
		// we get List<String>
		Type exactReturnType = GenericTypeReflector.getExactReturnType(listMethod, StringLister.class);
		assertEquals(new TypeToken<List<String>>(){}.getType(), exactReturnType);
	}
}
