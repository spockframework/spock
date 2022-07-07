package org.spockframework.gentyref;

import java.lang.reflect.*;
import java.util.*;

import static java.util.Arrays.asList;

class CaptureTypeImpl implements CaptureType {
	private final WildcardType wildcard;
	private final TypeVariable<?> variable;
	private final Type[] lowerBounds;
	private Type[] upperBounds;

	/**
	 * Creates an uninitialized CaptureTypeImpl. Before using this type, {@link #init(VarMap)} must be called.
	 * @param wildcard The wildcard this is a capture of
	 * @param variable The type variable where the wildcard is a parameter for.
	 */
	public CaptureTypeImpl(WildcardType wildcard, TypeVariable<?> variable) {
		this.wildcard = wildcard;
		this.variable = variable;
		this.lowerBounds = wildcard.getLowerBounds();
	}

	/**
	 * Initialize this CaptureTypeImpl.
	 * This is needed for type variable bounds referring to each other: we need the capture of the argument.
	 */
	void init(VarMap varMap) {
		ArrayList<Type> upperBoundsList = new ArrayList<>(asList(varMap.map(variable.getBounds())));

    List<Type> wildcardUpperBounds = asList(wildcard.getUpperBounds());
		if (wildcardUpperBounds.size() > 0 && wildcardUpperBounds.get(0) == Object.class) {
			// skip the Object bound, we already have a first upper bound from 'variable'
			upperBoundsList.addAll(wildcardUpperBounds.subList(1, wildcardUpperBounds.size()));
		} else {
			upperBoundsList.addAll(wildcardUpperBounds);
		}
		upperBounds = new Type[upperBoundsList.size()];
		upperBoundsList.toArray(upperBounds);
	}

	/*
	 * @see com.googlecode.gentyref.CaptureType#getLowerBounds()
	 */
	@Override
  public Type[] getLowerBounds() {
		return lowerBounds.clone();
	}

	/*
	 * @see com.googlecode.gentyref.CaptureType#getUpperBounds()
	 */
	@Override
  public Type[] getUpperBounds() {
		assert upperBounds != null;
		return upperBounds.clone();
	}

	@Override
	public String toString() {
		return "capture of " + wildcard;
	}
}
