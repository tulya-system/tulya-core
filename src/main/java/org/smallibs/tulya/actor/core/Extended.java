package org.smallibs.tulya.actor.core;

import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Unit;

public sealed interface Extended<Protocol> {

    record Activate<Protocol>() implements Extended<Protocol> {
    }

    record Deferred<Protocol>(Runnable runnable) implements Extended<Protocol> {
    }

    record Dispose<Protocol>(SolvablePromise<Unit> promise) implements Extended<Protocol> {
    }

    record Carried<Protocol>(Protocol message) implements Extended<Protocol> {
    }

}
