package org.smallibs.tulya.actor.core;

@FunctionalInterface
public interface Actor<Protocol> {
    boolean tell(Extended<Protocol> message);
}
