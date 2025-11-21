package org.smallibs.tulya.async;

import org.smallibs.tulya.standard.Try;

public interface Solvable<R> {
    boolean solve(Try<R> value);
}
