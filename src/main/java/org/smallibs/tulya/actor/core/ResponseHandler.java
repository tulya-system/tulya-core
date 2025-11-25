package org.smallibs.tulya.actor.core;

import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.Solvable;

public interface ResponseHandler<T> {

    Promise<T> promise();

    Solvable<T> solvable();

}
