package org.spockframework.runtime;

/**
 * A special iterator, that gives to the data produced by Spock's data providers.
 * <p>
 * The creator of the data iterator is responsible to close it.
 * <p>
 * {@link #next()} will return {@code null} when an error occurs during calculation of the values.
 * Consumers of the data iterator should check for {@code null} values, and skip the iteration if it is {@code null}.
 *
 * @author Leonard Brünings
 * @since 2.2
 */
public interface IDataIterator extends IBaseDataIterator<Object[]> {
}
