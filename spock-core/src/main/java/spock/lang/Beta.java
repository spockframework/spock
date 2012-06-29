package spock.lang;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the annotated element, part of Spock's public API, may change
 * incompatibly or even be removed in a future release. This allows us to collect
 * valuable feedback on a new feature before freezing its API.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface Beta {}