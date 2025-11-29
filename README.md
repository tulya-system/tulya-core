# Tulya

[Q. verb. *to lead; to fetch, *to lead; to fetch; to bring, send](https://www.elfdict.com/w/tulya-)

Indirect and direct asynchronous programming style applied to Java and a minimal but effective actor system.

## Futures / Promise

### Introduction

The `Promise`[1] concept in programming languages was introduced by **Daniel P. Friedman** and **David S. Wise** in

1976. In this paper, lazy evaluation is introduced for suspending cons.

> "(...) in fact, because of the suspending cons, z is initially bound only to a "promise" of this result."

Later, the `Future`[2] concept was introduced by **Henry Baker** and **Carl Hewitt** in 1977 as part of their work
on the *Actor model* of concurrent programming at MIT. This concept was introduced in order to exhibit a new approach
for function evaluation with a call-by-future introducing fine grain parallelism.

> "This paper investigates some problems associated with an argument evaluation order that we call "future' order, which
> is different from both call-by-name and call-by-value. In call-by-future, each formal parameter of a function is bound
> to a separate process (called a "future") dedicated to the evaluation of the corresponding argument."

- [1] [The impact of applicative programming on multiprocessing](https://www.bitsavers.org/pdf/ieee/Conference_on_Parallel_Processing/1976_International_Conference_on_Parallel_Processing.pdf) -
  Daniel P. Friedman, David S. Wise, pages 263-272.
- [2] [The Incremental Garbage Collection of Processes](https://www.plover.com/misc/hbaker-archive/Futures.html) - Henry
  C. Baker, Jr. & Carl Hewitt.

### Future in Java Concurrent library

In Java, `Future<V>` is an interface that represents a task running asynchronously and that will produce a result of
type `V` or an error indicated by an exception. A `Future` represents the result of an asynchronous calculation. Methods
are provided to check whether the calculation is complete, to wait for it to finish, and to retrieve the result of the
calculation. This approach allows for a direct programming style, but its major drawback is that it blocks the
calculation while waiting for the result.

### Introducing Promise in Java

In the `Java Concurrent` library, a specific `Future` implementation called `CompletableFuture` provides both the direct
style and an indirect style based on the continuation passing style i.e. CSP. In order to achieve a clear separation
between the result receiver, i.e. `Future`, and the control, we decided to design `Promise` for this purpose.

Then, a `Future` mainly provides direct style while a `Promise` provides Indirect style and a bridge for a direct style.

#### Technical aspects

```Java
public interface Future<V> {
    boolean cancel(boolean mayInterruptIfRunning);
    boolean isCancelled();
    boolean isDone();
    V get() throws InterruptedException, ExecutionException;
}
```

```Java
public interface Promise<T> {
    <R> Promise<R> map(Function<? super T, ? extends R> mapper);
    <R> Promise<R> flatMap(Function<? super T, ? extends Promise<R>> flatMapper);
    Promise<T> onComplete(Consumer<? super Try<T>> fn);
}
```

#### Callback hell

> (programming, colloquial) (mostly of the JavaScript language) The situation where callbacks are nested within other
> callbacks several levels deep, potentially making it difficult to understand and maintain the code.

This approach can be solved by design, explained in Martin Fowler book Refactoring, extracting code for a better layered
code structuration. Unfortunately, it's only a matter of practice and programming experience which is not a de-facto idiomatic approach.   

## Async / Await

### Introduction

> In computer programming, the async/await pattern is a syntactic feature of many programming languages that allows an
> asynchronous, non-blocking function to be structured in a way similar to an ordinary synchronous function.

This approach allows for a direct style for asynchronous computation, but with one major key point, which is the ability
to have non-blocking functionality even in a direct style.

### Non-blocking computation in Java 21+

In Java 21+, thanks to the Loom project, the JVM is now equipped by virtual threads.

> A virtual thread still runs code on an OS thread. However, when code running in a virtual thread calls a blocking I/O
> operation, the Java runtime suspends the virtual thread until it can be resumed. The OS thread associated with the
> suspended virtual thread is now free to perform operations for other virtual threads.

Based on certain criteria, the JVM is able to suspend a virtual thread, thereby freeing up the platform thread that
ensures its execution. To this end, thread `park` and `unpark` capabilities have been revised to offer specialized
behavior depending on the nature of the thread, i.e., platform or virtual.

### Async / Await and virtual threads

### Introduction

TODO

### Promise revisited

```Java
public interface Promise<T> {

    // Indirect style section

    <R> Promise<R> map(Function<? super T, ? extends R> mapper);
    <R> Promise<R> flatMap(Function<? super T, ? extends Promise<R>> flatMapper);
    Promise<T> onComplete(Consumer<? super Try<T>> fn);

    // Direct style section

    T await() throws Throwable;
    T await(Duration duration) throws Throwable;
}
```

### Design consideration

TODO

### Taste of Tulya Async / Await

```Java
public void shouldAwaitFor_1_000_000_Tasks() {
    // Given
    var numberOfTasks = 1_000_000;
    var executor = Execution.ofVirtual();

    var runningTasks = new AtomicInteger(numberOfTasks);

    // When
    var barrier = new SolvablePromise<Unit>();

    for (var i = 0; i < numberOfTasks; i++) {
        executor.async(() -> {
            barrier.await();
            runningTasks.decrementAndGet();
        });
    }

    barrier.success(Unit.unit);

    // Then
    Awaitility.await().until(() -> runningTasks.get() == 0);
}
```

## Actor System

### Asynchronous style considerations

TODO

### Taste of Tulya Actors

#### Protocol

The protocol defines messages corresponding responses type. In this example, the actor supports only one kind of
message: `Fibonacci`. The message contains the value to compute and a `Solvable` response carrier.

```Java
record Fibonacci(int value, Solvable<Integer> response) {
    // Message factory
    static BehaviorCall<Fibonacci, Integer> build(int value) {
        return solvable -> new Fibonacci(value, solvable);
    }
}
```

#### Actor

In this example, the actor computes the Fibonacci number with a direct computation style using `await`.

```Java
record DirectComputation(ActorReference<Fibonacci> self) implements Behavior<Fibonacci> {
    @Override
    public void ask(Fibonacci message) {
        if (message.value() < 2) {
            message.response().success(message.value());
        } else {
            var result = Try.handle(() -> {
                var minus1 = self().ask(Fibonacci.build(message.value() - 1));
                var minus2 = self().ask(Fibonacci.build(message.value() - 2));

                return minus1.await() + minus2.await();
            });

            message.response().solve(result);
        }
    }
}
```

Of course, indirect and direct styles can be used together.

```Java
record MixedComputation(ActorReference<Fibonacci> self) implements Behavior<Fibonacci> {
    @Override
    public void ask(Fibonacci message) {
        if (message.value < 2) {
            message.response().success(message.value());
        } else {
            self().ask(fibonacci(message.value() - 1))
                    .flatMap(Promise.handle(i1 -> {
                        var i2 = self().ask(fibonacci(message.value() - 2));
                        return i1 + i2.await();
                    }))
                    .onComplete(e -> message.response().solve(e));
        }
    }
}
```

#### Execution

Now, let's compute `Fibonacci` thanks to the previous actor.

```Java
void shouldComputeDirectFibonacci() throws Throwable {
    // Given
    try (var coordinator = ActorCoordinator.Companion.build()) {
        var fibonacci = coordinator.register(address("fibonacci"), DirectComputation::new).orElseThrow();

        // When
        var result = fibonacci.ask(Fibonacci.build(19));

        // Then
        Assertions.assertEquals(4181, result.await());
    }
}
```

#### Basic benchmarks

> Configuration: Apple M2 Max, memory og 64GiB

The actor on this bench immediately returns `Unit.unit` as soon as it receives a request. The measurements were taken 
from 1_000 actors to 1_000_000 actors with a maximum of 10_000_000 requests.

##### With Platform threads

> Average throughput: 1_979 requests per milliseconds 

![platform-throughput.png](src/test/data/platform-throughput.png)

##### With Virtual threads

> Average throughput: 1_662 requests per milliseconds

![platform-throughput.png](src/test/data/platform-throughput.png)

## License

```
MIT License

Copyright (c) 2025 Didier Plaindoux

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
