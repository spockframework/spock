package org.spockframework.mock.runtime;

/**
 * The Object implementing this interface can be attached to and
 * detached from a {@link spock.lang.Specification}.
 *
 * @author Leonard Bruenings
 * @deprecated implement {@link spock.mock.SpecificationAttachable} instead;
 *             this internal alias remains only for binary compatibility.
 */
@Deprecated
public interface SpecificationAttachable extends spock.mock.SpecificationAttachable {
}
