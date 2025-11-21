package org.smallibs.tulya.lang;

@FunctionalInterface
public interface RunnableWithError {
    void run() throws Throwable;
}
