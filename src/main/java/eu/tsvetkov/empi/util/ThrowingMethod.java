package eu.tsvetkov.empi.util;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
@FunctionalInterface
public interface ThrowingMethod<T, E extends Exception> {
    T run() throws E;
}
