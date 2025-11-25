package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.ResponseHandler;
import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.impl.SolvablePromise;

public record ResponseHandlerImpl<T>(Promise<T> promise, SolvablePromise<T> solvable) implements ResponseHandler<T> {
}
