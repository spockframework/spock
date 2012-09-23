package org.spockframework.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the annotated element, a member of Spock's public API, is in a
 * trial phase. Until the element is promoted by removing this annotation, it may still undergo
 * incompatible changes. This allows us to incorporate valuable feedback from our users
 * before freezing the API for a new feature. In the unlikely event that the element isn't deemed fit for
 * purpose, it may be removed completely. Typically, elements are promoted within one or two releases.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface Beta {}