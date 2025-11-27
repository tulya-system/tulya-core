# Tulya

[Q. verb. *to lead; to fetch, *to lead; to fetch; to bring, send](https://www.elfdict.com/w/tulya-)

Indirect and direct asynchronous programming style applied to Java and a minimal but effective actor system.

## Futures / Promises

## Async / Await

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
record IndirectComputation(ActorReference<Fibonacci> self) implements Behavior<Fibonacci> {
    @Override
    public void ask(Fibonacci message) {
        if (message.value < 2) {
            message.response().success(message.value());
        } else {
            self().ask(fibonacci(message.value() - 1))
                    .flatMap(i1 -> Promise.handle(() -> {
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