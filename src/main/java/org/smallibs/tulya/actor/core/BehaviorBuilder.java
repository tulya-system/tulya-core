package org.smallibs.tulya.actor.core;

import java.util.function.Function;

@FunctionalInterface
public interface BehaviorBuilder<Protocol> extends Function<ActorReference<Protocol>, Behavior<Protocol>> {
}
