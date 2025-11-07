package com.example.evently.data.generic;

import java.util.Optional;
import java.util.function.Consumer;

import com.google.android.gms.tasks.Task;

/**
 * Helper for working with {@link Promise}s that yield to an optional value.
 * @param <T> The value that may be optionally yielded.
 * @implNote Indeed, in better languages, these wouldn't need to be its own class.
 *           Rather, a simple typeclass constraint would be enough. Alas, this is Java.
 * @see Promise
 */
public final class PromiseOpt<T> extends Promise<Optional<T>> {
    PromiseOpt(Task<Optional<T>> task) {
        super(task);
    }

    /**
     * Construct from a plain old promise.
     * @param prom Promise that must yield to an optional value.
     * @return Constructed PromiseOpt.
     * @param <T> Type of the optional value.
     */
    public static <T> PromiseOpt<T> promiseOpt(Promise<Optional<T>> prom) {
        return new PromiseOpt<>(prom.task);
    }

    /**
     * Construct from a firestore task.
     * @param task Task that must yield to an optional value.
     * @return Constructed PromiseOpt.
     * @param <T> Type of the optional value.
     */
    public static <T> PromiseOpt<T> promiseOpt(Task<Optional<T>> task) {
        return new PromiseOpt<>(task);
    }

    /**
     * Perform an action with the yielded value if present.
     * This is meant to be used in conjunction with 'orElse', as this will not do anything if the value is not present.
     * @param consumer Consumer function that runs with the yielded value.
     * @return The same promise, but now with a success listener attached (that reacts to present optional).
     */
    public PromiseOpt<T> optionally(Consumer<T> consumer) {
        return promiseOpt(task.addOnSuccessListener(resOpt -> {
            resOpt.ifPresent(consumer);
        }));
    }

    /**
     * Perform an action when the yielded value is not present.
     * This is meant to be used in conjunction with 'optionally', as this will not do anything if the value is present.
     * @param runnable Action that runs with the yielded value.
     * @return The same promise, but now with a success listener attached (that reacts to empty optional).
     */
    public PromiseOpt<T> orElse(Runnable runnable) {
        return promiseOpt(task.addOnSuccessListener(resOpt -> {
            if (resOpt.isEmpty()) runnable.run();
        }));
    }
}
