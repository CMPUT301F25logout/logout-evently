package com.example.evently.data.generic;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.annotations.concurrent.Background;

/**
 * A fluent API for working with firestore concurrent tasks.
 * @param <T> Type of value the promise resolves to.
 */
public class Promise<T> {
    private final Task<T> task;

    /**
     * Convert a firestore {@link Task} into a Promise, blessing it with a fluent API.
     * @param task The firestore task at hand.
     * @return A Promise object that inherits the semantics of a task: concurrency.
     * @param <T> Type parameter of the result the task yields.
     */
    public static <T> Promise<T> promise(Task<T> task) {
        return new Promise<>(task);
    }

    /**
     * Construct a concurrent task that immediately resolves to the given value.
     * @param pure Raw value to yield.
     * @return A promise that immediately resolves.
     * @param <T> Type of the contained value.
     * @apiNote For the functional programmers, this is the applicative pure function.
     */
    public static <T> Promise<T> of(T pure) {
        return promise(Tasks.forResult(pure));
    }

    public static <T> Promise<List<T>> all(Stream<Promise<T>> promises) {
        final var tasks = promises.map(x -> x.task).collect(Collectors.toList());
        return promise(Tasks.whenAllSuccess(tasks));
    }

    @SafeVarargs
    public static <T> Promise<List<T>> all(Promise<T>... promises) {
        return all(Arrays.stream(promises));
    }

    private Promise(Task<T> task) {
        this.task = task;
    }

    /**
     * Apply a non concurrent mapping function over the result of this promise, manipulating it at will.
     * @param func Mapper function to apply to the result of the promise.
     * @return The same promise, except it will now resolve to the result of the mapping.
     * @param <R> Type indicating the result of the map.
     */
    public <R> Promise<R> map(Function<T, R> func) {
        return promise(task.onSuccessTask(x -> Tasks.forResult(func.apply(x))));
    }

    /**
     * Chain another concurrent task (promise) to this one.
     * @param act Concurrent task to invoke once the current one is complete, with its result.
     * @return A new promise that will resolve successfully once both promises are completed.
     * @param <R> Return value of the second promise.
     * @apiNote For the functional programmers, indeed this is the monadic bind function.
     */
    public <R> Promise<R> then(Function<T, Promise<R>> act) {
        return promise(task.onSuccessTask(x -> act.apply(x).task));
    }

    /**
     * Compose one promise with another, using both of their results in the end.
     * @param other The second promise to compose with.
     * @param act The action to apply with both results.
     * @return A new promise that is a composition of the two.
     * @param <U> Type the second promise resolves to.
     * @param <R> Type the returned promise resolves to.
     */
    public <U, R> Promise<R> compose(Promise<U> other, BiFunction<T, U, Promise<R>> act) {
        // TODO (chase): This is technically not parallel.
        //  But using the parallel methods (whenAll) ruins type safety...
        return promise(
                task.onSuccessTask(x -> other.task.onSuccessTask(y -> act.apply(x, y).task)));
    }

    /**
     * Attach an exception handler to the promise.
     * @apiNote Meant to be called once at the end of a promise chain.
     *          Using this multiple times will attach several handlers that will all be fired.
     * @param handler Exception handler.
     */
    public void catchE(Consumer<Exception> handler) {
        task.addOnFailureListener(handler::accept);
    }

    @Background
    public T await() throws ExecutionException, InterruptedException {
        return Tasks.await(task);
    }
}
