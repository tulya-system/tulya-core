package org.smallibs.tulya.actor.core;

import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.Solvable;

public interface ResponseHandler<T> extends Promise<T> {
    Solvable<T> solvable();
}
