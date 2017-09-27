package eu.tsvetkov.empi.util;

import java.util.function.Consumer;

/**
 * @author Vadim Tsvetkov (dev@tsvetkov.eu)
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> extends Consumer<T> {

    @Override
    default void accept(final T elem) {
        try {
            acceptThrows(elem);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    void acceptThrows(T elem) throws Exception;

}