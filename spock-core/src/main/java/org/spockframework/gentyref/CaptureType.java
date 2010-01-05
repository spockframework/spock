package org.spockframework.gentyref;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * CaptureType represents a wildcard that has gone through capture conversion.
 * It is a custom subinterface of Type, not part of the java builtin Type hierarchy.
 * 
 * @author Wouter Coekaerts <wouter@coekaerts.be>
 */
public interface CaptureType extends Type {
    /**
     * Returns an array of <tt>Type</tt> objects representing the upper
     * bound(s) of this capture. This includes both the upper bound of a <tt>? extends</tt> wildcard,
     * and the bounds declared with the type variable.
     * References to other (or the same) type variables in bounds coming from the type variable are
     * replaced by their matching capture.
     */
	Type[] getUpperBounds();
	
    /**
     * Returns an array of <tt>Type</tt> objects representing the 
     * lower bound(s) of this type variable. This is the bound of a <tt>? super</tt> wildcard.
     * This normally contains only one or no types; it is an array for consistency with {@link WildcardType#getLowerBounds()}.
     */
	Type[] getLowerBounds();
}
