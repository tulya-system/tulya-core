package org.smallibs.tulya.lang;

@FunctionalInterface
public interface SupplierWithError<O> {
    O get() throws Throwable;
}
