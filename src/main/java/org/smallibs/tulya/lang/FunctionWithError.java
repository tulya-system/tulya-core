package org.smallibs.tulya.lang;

@FunctionalInterface
public interface FunctionWithError<I, O> {
    O apply(I input) throws Throwable;
}
