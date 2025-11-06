package com.example.evently.data.generic;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import android.util.Pair;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.annotations.concurrent.Background;

/**
 * A fluent API for working with firestore concurrent tasks.
 * @param <T> Type of value the promise resolves to.
 */
public sealed class Promise<T> permits PromiseOpt {
    protected final Task<T> task;

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

    protected Promise(Task<T> task) {
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
     * Compose one promise with another, in parallel, using both of their results in the end.
     * @param other The second promise to compose with.
     * @return A new promise that is a composition of the two.
     * @param <U> Type the second promise resolves to.
     * @apiNote For the functional programmers, indeed this is the liftA/liftM/ap function.
     */
    public <U> Promise<Pair<T, U>> with(Promise<U> other) {
        final var tasks = Tasks.whenAllSuccess(task, other.task);
        return promise(tasks.onSuccessTask(results -> {
            final var thisRes = (T) results.get(0);
            final var otherRes = (U) results.get(1);
            return Tasks.forResult(new Pair<>(thisRes, otherRes));
        }));
    }

    /**
     * Compose one promise with another, in parallel, using both of their results in the end.
     * @param other The second promise to compose with.
     * @param act An action to apply using both results.
     * @return A new promise that is a composition of the two with the action applied.
     * @param <U> Type the second promise resolves to.
     * @param <R> Type the action returned promise resolves to.
     * @apiNote For the functional programmers, indeed this is the liftA/liftM/ap function.
     */
    public <U, R> Promise<R> with(Promise<U> other, BiFunction<T, U, Promise<R>> act) {
        return this.with(other).then(pair -> act.apply(pair.first, pair.second));
    }

    /**
     * Compose one promise with another, in parallel, using only the second result.
     * @param other The second promise to compose with.
     * @return A new promise that is a biased composition of the two.
     * @param <R> Type the returned promise resolves to.
     */
    public <R> Promise<R> alongside(Promise<R> other) {
        final var tasks = Tasks.whenAllSuccess(task, other.task);
        return promise(tasks.onSuccessTask(results -> {
            // The second task's result is all that matters and its type is U.
            final var otherRes = (R) results.get(1);
            return Tasks.forResult(otherRes);
        }));
    }

    /**
     * Run an action once this promise succeeds.
     * @apiNote Meant to be called once at the end of a promise chain.
     *          Using this multiple times will attach several consumers that will all be fired.
     * @return The same promise we started with, but now there's a success listener.
     * @param act Consumer to invoke with result.
     */
    public Promise<T> thenRun(Consumer<T> act) {
        return promise(task.addOnSuccessListener(act::accept));
    }

    /**
     * Attach an exception handler to the promise.
     * @apiNote Meant to be called once at the end of a promise chain.
     *          Using this multiple times will attach several handlers that will all be fired.
     * @return The same promise we started with, but now there's a failure listener.
     * @param handler Exception handler.
     */
    public Promise<T> catchE(Consumer<Exception> handler) {
        return promise(task.addOnFailureListener(handler::accept));
    }

    /**
     * Block until a promise finishes and yields the result.
     * @apiNote NEVER use this on the UI thread.
     * @return The computed result.
     * @throws ExecutionException Exceptions thrown during execution of the promise.
     * @throws InterruptedException Exception thrown due to external interruption of the promise.
     */
    @Background
    public T await() throws ExecutionException, InterruptedException {
        return Tasks.await(task);
    }
}
