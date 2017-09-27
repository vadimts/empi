package eu.tsvetkov.empi.util;

import java.util.stream.Stream;

/**
* @author Vadim Tsvetkov (dev@tsvetkov.eu)
*/
@FunctionalInterface
public interface ThrowingStreamMethod<T, E extends Exception> {
    Stream<T> run(T param) throws E;
}
